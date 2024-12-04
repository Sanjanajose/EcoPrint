package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.dto.QueuedJobDTO;
import com.ecoprint.printmanagement.model.QueuedJob;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.Priority;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.QueuedJobRepository;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Constructor injection for better testability
    public QueueManagementService(QueuedJobRepository queuedJobRepository, PrintJobRepository printJobRepository) {
        this.queuedJobRepository = queuedJobRepository;
        this.printJobRepository = printJobRepository;
    }

    /**
     * Adds a job to the queue using DTO.
     *
     * @param queuedJobDTO the DTO containing job details
     * @return QueuedJobDTO of the saved job
     */
    @Transactional
    public QueuedJobDTO addJobToQueue(QueuedJobDTO queuedJobDTO) {
        PrintJob printJob = printJobRepository.findById(queuedJobDTO.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", queuedJobDTO.getJobId()));

        QueuedJob queuedJob = mapFromPrintJob(printJob);
        QueuedJob savedJob = queuedJobRepository.save(queuedJob);

        return mapToDTO(savedJob);
    }

    /**
     * Adds a job to the queue using a PrintJob entity.
     *
     * @param printJob the PrintJob entity
     */
    @Transactional
    public void addJobToQueue(PrintJob printJob) {
        PrintJob managedPrintJob = entityManager.find(PrintJob.class, printJob.getId());
        if (managedPrintJob == null) {
            throw new IllegalArgumentException("PrintJob not found with ID: " + printJob.getId());
        }

        QueuedJob queuedJob = mapFromPrintJob(managedPrintJob);
        queuedJobRepository.save(queuedJob);
    }

    /**
     * Retrieves all queued jobs as DTOs.
     *
     * @return List of QueuedJobDTOs
     */
    public List<QueuedJobDTO> getAllQueuedJobs() {
        List<QueuedJob> queuedJobs = queuedJobRepository.findAll();
        return queuedJobs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Updates the status of a queued job.
     *
     * @param jobId  the ID of the job to update
     * @param status the new status
     * @return QueuedJobDTO of the updated job
     */
    @Transactional
    public QueuedJobDTO updateJobStatus(Long jobId, PrintJobStatus status) {
        QueuedJob queuedJob = queuedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("QueuedJob", "jobId", jobId));

        queuedJob.setStatus(status);
        QueuedJob updatedJob = queuedJobRepository.save(queuedJob);
        return mapToDTO(updatedJob);
    }

    /**
     * Removes a job from the queue.
     *
     * @param jobId the ID of the job to remove
     */
    @Transactional
    public void removeJobFromQueue(Long jobId) {
        if (!queuedJobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("QueuedJob", "jobId", jobId);
        }
        queuedJobRepository.deleteById(jobId);
    }

    // --- Helper Methods ---

    /**
     * Maps a QueuedJobDTO to a QueuedJob entity.
     *
     * @param dto the DTO
     * @return QueuedJob entity
     */
    private QueuedJob mapToEntity(QueuedJobDTO dto) {
        QueuedJob job = new QueuedJob();
        job.setJobId(dto.getJobId());
        job.setDocumentName(dto.getDocumentName());
        job.setUserId(dto.getUserId());
        job.setPrinterId(dto.getPrinterId());
        job.setPagesPrinted(dto.getPagesPrinted());
        job.setNumCopies(dto.getNumCopies());
        job.setSubmissionTimestamp(dto.getSubmissionTimestamp());
        job.setJobPriority(Enum.valueOf(Priority.class, dto.getJobPriority()));
        job.setStatus(Enum.valueOf(PrintJobStatus.class, dto.getStatus()));
        job.setQueuePosition(dto.getQueuePosition()); // Ensure DTO has this field
        return job;
    }

    private QueuedJobDTO mapToDTO(QueuedJob job) {
        QueuedJobDTO dto = new QueuedJobDTO();
        dto.setJobId(job.getJobId());
        dto.setDocumentName(job.getDocumentName());
        dto.setUserId(job.getUserId());
        dto.setPrinterId(job.getPrinterId());
        dto.setPagesPrinted(job.getPagesPrinted());
        dto.setNumCopies(job.getNumCopies());
        dto.setSubmissionTimestamp(job.getSubmissionTimestamp());
        dto.setJobPriority(job.getJobPriority().name());
        dto.setStatus(job.getStatus().name());
        dto.setQueuePosition(job.getQueuePosition()); // Ensure DTO has this field
        return dto;
    }


    /**
     * Maps a PrintJob entity to a QueuedJob entity.
     *
     * @param printJob the PrintJob entity
     * @return QueuedJob entity
     */
   
    
    private QueuedJob mapFromPrintJob(PrintJob printJob) {
        System.out.println("Mapping from PrintJob: pagesPrinted = " + printJob.getPagesPrinted());
        QueuedJob queuedJob = new QueuedJob();
        queuedJob.setPrintJob(printJob);
        queuedJob.setDocumentName(printJob.getFileName());
        queuedJob.setUserId(printJob.getUser().getId());
        queuedJob.setPrinterId(printJob.getPrinter().getId());
        System.out.println("Mapping from PrintJob: pagesPrinted = " + printJob.getPagesPrinted());
        queuedJob.setPagesPrinted(printJob.getPagesPrinted());
        queuedJob.setNumCopies(printJob.getNumCopies());
        queuedJob.setSubmissionTimestamp(printJob.getUploadTimestamp());
        queuedJob.setJobPriority(printJob.getPriority());
        queuedJob.setStatus(PrintJobStatus.QUEUED);
        return queuedJob;
    }

}
