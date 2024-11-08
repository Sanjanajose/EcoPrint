package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;

@Service
public class PrintJobService {

    private static final Logger logger = LoggerFactory.getLogger(PrintJobService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final SimpMessagingTemplate messagingTemplate;

    private final PrintJobRepository printJobRepository;
    private final JobHistoryRepository jobHistoryRepository;
    private final Tika tika = new Tika();

    public PrintJobService(PrintJobRepository printJobRepository, JobHistoryRepository jobHistoryRepository, SimpMessagingTemplate messagingTemplate) {
        this.printJobRepository = printJobRepository;
        this.jobHistoryRepository = jobHistoryRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Method to upload file and create a new job
    public void uploadFile(MultipartFile file, String userName, String description, int pagesPrinted, double cost) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty. Please upload a valid file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
        }

        String fileType = tika.detect(file.getInputStream());
        validateFileType(fileType);

        byte[] fileData = file.getBytes();
        PrintJob printJob = new PrintJob();
        printJob.setFileName(file.getOriginalFilename());
        printJob.setFileType(fileType);
        printJob.setFileSize(file.getSize());
        printJob.setUserName(userName);
        printJob.setUploadTimestamp(LocalDateTime.now());
        printJob.setFileData(fileData);
        printJob.setDescription(description);
        printJob.setPagesPrinted(pagesPrinted);
        printJob.setCost(cost);
        printJob.setStatus(PrintJobStatus.SUBMITTED);
        printJob.setSubmittedAt(LocalDateTime.now());

        printJobRepository.save(printJob);
        
        // Log job submission
        logJobAction(printJob.getId(), PrintJobStatus.SUBMITTED, "Job submitted by user");
    }

    // Method to validate file type
    private void validateFileType(String fileType) {
        List<String> allowedFileTypes = Arrays.asList(
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "image/jpeg", "image/png", "image/tiff", "image/bmp",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv"
        );

        if (!allowedFileTypes.contains(fileType)) {
            throw new IllegalArgumentException("File type '" + fileType + "' is not supported.");
        }
    }

    // Method to download file
    public Resource downloadFile(Long id) {
        PrintJob printJob = findPrintJobById(id);
        return new ByteArrayResource(printJob.getFileData());
    }

    // Find PrintJob by ID
    public PrintJob findPrintJobById(Long id) {
        return printJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PrintJob not found with id: " + id));
    }

    // Update job status with logging
    public void updateJobStatus(Long jobId, PrintJobStatus status, String comments) {
        PrintJob printJob = findPrintJobById(jobId);
        printJob.setStatus(status);
        LocalDateTime now = LocalDateTime.now();

        switch (status) {
            case QUEUED -> printJob.setQueuedAt(now);
            case PAUSED -> printJob.setPausedAt(now);
            case READY -> printJob.setReadyAt(now);
            case PRINTING -> printJob.setPrintingAt(now);
            case COMPLETED -> printJob.setCompletedAt(now);
            case FAILED -> printJob.setFailedAt(now);
            case DELETED -> printJob.setDeletedAt(now);
            case FAVORITE -> printJob.setFavoriteAt(now);
        }

        savePrintJob(printJob);
        logJobAction(jobId, status, comments);
    }

    public void savePrintJob(PrintJob printJob) {
        printJobRepository.save(printJob);
    }

    public void prioritizeJob(Long jobId) {
        PrintJob jobToPrioritize = findPrintJobById(jobId);

        // If priority is null, set a default
        if (jobToPrioritize.getPriority() == null) {
            jobToPrioritize.setPriority(1);
        } else {
            jobToPrioritize.setPriority(1); // Set highest priority
        }
        savePrintJob(jobToPrioritize);

        // Retrieve and reorder other jobs in the queue
        List<PrintJob> jobs = printJobRepository.findByStatusOrderByPriorityAsc(PrintJobStatus.QUEUED);
        int currentPriority = 2;
        for (PrintJob job : jobs) {
            if (!job.getId().equals(jobId) && (job.getPriority() == null || job.getPriority() != currentPriority)) {
                job.setPriority(currentPriority++);
            }
        }

        printJobRepository.saveAll(jobs); // Save updated jobs in batch
        logJobAction(jobId, PrintJobStatus.QUEUED, "Job prioritized by Admin");
        messagingTemplate.convertAndSend("/topic/job-queue", jobs); // Broadcast updated queue
    }

    // Method to cancel a job
    public void cancelJob(Long jobId) {
        updateJobStatus(jobId, PrintJobStatus.DELETED, "Job canceled by user");
    }

    // Method to pause a job
    public void pauseJob(Long jobId) {
        updateJobStatus(jobId, PrintJobStatus.PAUSED, "Job paused by user");
    }

    // Method to mark a job as favorite
    public void markAsFavorite(Long jobId) {
        updateJobStatus(jobId, PrintJobStatus.FAVORITE, "Job marked as favorite by user");
    }

    
    
    
    public void logJobAction(Long jobId, PrintJobStatus status, String actionDescription) {
        JobHistory history = new JobHistory();
        history.setPrintJobId(jobId);
        history.setStatus(status);
        history.setComments(actionDescription);
        history.setTimestamp(LocalDateTime.now());

        jobHistoryRepository.save(history); // Save the log entry
    }

    // Method to log status change
    public void logStatusChange(Long jobId, PrintJobStatus status, String comments) {
        JobHistory history = new JobHistory();
        history.setPrintJobId(jobId);
        history.setStatus(status);
        history.setComments(comments);
        history.setTimestamp(LocalDateTime.now());

        jobHistoryRepository.save(history);
    }
}
