package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.dto.QueuedJobDTO;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.QueuedJob;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.QueuedJobRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueueManagementService {

    @Autowired
    private final QueuedJobRepository queuedJobRepository;

    @Autowired
    private final PrintJobRepository printJobRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public QueueManagementService(QueuedJobRepository queuedJobRepository, PrintJobRepository printJobRepository) {
        this.queuedJobRepository = queuedJobRepository;
        this.printJobRepository = printJobRepository;
    }

  /*  @Transactional
    public QueuedJobDTO addJobToQueue(Long jobId) {
        // Fetch the PrintJob based on jobId
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));

        // Validate the current status of the PrintJob
        if (printJob.getStatus() != PrintJobStatus.SUBMITTED && printJob.getStatus() != PrintJobStatus.READY) {
            throw new IllegalStateException("Only jobs in SUBMITTED or READY status can be added to the queue");
        }

        // Map PrintJob to QueuedJob
        QueuedJob queuedJob = mapFromPrintJob(printJob);

        // Calculate the next queue position
        Integer maxPosition = queuedJobRepository.findMaxQueuePosition();
        int nextPosition = (maxPosition != null) ? maxPosition + 1 : 1; // Start from 1 if no jobs exist
        queuedJob.setQueuePosition(nextPosition);

        // Save the QueuedJob
        QueuedJob savedJob = queuedJobRepository.save(queuedJob);

        // Update the PrintJob's status to QUEUED and set its queue position
        printJob.setStatus(PrintJobStatus.QUEUED);
        printJob.setQueuePosition(nextPosition);
        printJobRepository.save(printJob);

        
        // Return the DTO
        return mapToDTO(savedJob);
    }*/
    
    
    @Transactional
    public QueuedJobDTO addJobToQueue(Long jobId) {
        // Fetch the PrintJob entity
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));

        // Validate the current status
        if (printJob.getStatus() != PrintJobStatus.SUBMITTED && printJob.getStatus() != PrintJobStatus.READY) {
            throw new IllegalStateException("Only jobs in SUBMITTED or READY status can be added to the queue.");
        }

        // Map and save the QueuedJob entity
        QueuedJob queuedJob = mapFromPrintJob(printJob);
        queuedJob.setQueuePosition(determineQueuePosition());
        queuedJob.setStatus(PrintJobStatus.QUEUED);
        queuedJob.setPrintJob(printJob); // Ensure bi-directional reference
        queuedJobRepository.save(queuedJob);

        // Update the PrintJob entity
        printJob.setStatus(PrintJobStatus.QUEUED);
        printJob.setQueuePosition(queuedJob.getQueuePosition());
        printJob.setQueuedAt(LocalDateTime.now()); // Set the queuedAt timestamp
        printJobRepository.save(printJob);

        // Optional: Synchronize the persistence context
        entityManager.flush();

        // Log success
        //logger.info("Successfully added job ID: {} to queue. Queue position: {}", jobId, queuedJob.getQueuePosition());

        return mapToDTO(queuedJob);
    }


    public List<QueuedJobDTO> getAllQueuedJobs() {
        return queuedJobRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

   
    public String getJobOwner(Long jobId) {
        // Fetch job details from queued_jobs
        QueuedJob queuedJob = queuedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("QueuedJob", "jobId", jobId));
        
        // Convert Long to String before returning
        return queuedJob.getUserId().toString();
    }




    @Transactional
    public void removeJobFromQueue(Long jobId) {
        // Check if the queued job exists
        QueuedJob queuedJob = queuedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("QueuedJob", "jobId", jobId));

        // Remove the queued job
        queuedJobRepository.deleteById(jobId);

        // Update the status in the PrintJob table to READY
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
        printJob.setStatus(PrintJobStatus.READY);
        printJob.setReadyAt(LocalDateTime.now()); // Optionally set the ready timestamp
        printJobRepository.save(printJob);
    }

    QueuedJob mapFromPrintJob(PrintJob printJob) {
        QueuedJob queuedJob = new QueuedJob();
        queuedJob.setPrintJob(printJob);
        queuedJob.setDocumentName(printJob.getFileName());
        queuedJob.setUserId(printJob.getUser().getId());
        queuedJob.setPrinterId(printJob.getPrinter().getId());
        queuedJob.setPagesPrinted(printJob.getPagesPrinted() > 0 ? printJob.getPagesPrinted() : 1);
        queuedJob.setNumCopies(printJob.getNumCopies());
        queuedJob.setSubmissionTimestamp(LocalDateTime.now());
        queuedJob.setJobPriority(printJob.getPriority());
        queuedJob.setStatus(PrintJobStatus.QUEUED);
        queuedJob.setQueuePosition(determineQueuePosition());
        return queuedJob;
    }

    private QueuedJobDTO mapToDTO(QueuedJob job) {
        QueuedJobDTO dto = new QueuedJobDTO();
        dto.setJobId(job.getJobId());
        dto.setDocumentName(job.getDocumentName());
        dto.setUserId(job.getUserId());
        dto.setPrinterId(job.getPrinterId());
        dto.setPagesPrinted(job.getPagesPrinted() > 0 ? job.getPagesPrinted() : 1);
        dto.setNumCopies(job.getNumCopies());
        dto.setSubmissionTimestamp(job.getSubmissionTimestamp());
        dto.setJobPriority(job.getJobPriority().name());
        dto.setStatus(job.getStatus().name());
        dto.setQueuePosition(job.getQueuePosition() > 0 ? job.getQueuePosition() : 1);
        return dto;
    }

   /* Integer determineQueuePosition() {
        Integer maxQueuePositionInQueuedJobs = queuedJobRepository.findMaxQueuePosition();
        Integer maxQueuePositionInPrintJobs = printJobRepository.findMaxQueuePosition();

        int maxPosition = Math.max(
                (maxQueuePositionInQueuedJobs != null ? maxQueuePositionInQueuedJobs : 0),
                (maxQueuePositionInPrintJobs != null ? maxQueuePositionInPrintJobs : 0)
        );
        return maxPosition + 1;
    }
    */
    
    
    Integer determineQueuePosition() {
        Integer maxPosition = Math.max(
            queuedJobRepository.findMaxQueuePosition() != null ? queuedJobRepository.findMaxQueuePosition() : 0,
            printJobRepository.findMaxQueuePosition() != null ? printJobRepository.findMaxQueuePosition() : 0
        );
        return maxPosition + 1;
    }

    
    @Transactional
    public void updateQueuePosition(Long jobId, Integer queuePosition) {
        // Update the queue position in queued_jobs table
        QueuedJob queuedJob = queuedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("QueuedJob", "jobId", jobId));
        queuedJob.setQueuePosition(queuePosition);
        queuedJobRepository.save(queuedJob);

        // Update the queue position in print_jobs table
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
        printJob.setQueuePosition(queuePosition);
        printJobRepository.save(printJob);
    }


}
