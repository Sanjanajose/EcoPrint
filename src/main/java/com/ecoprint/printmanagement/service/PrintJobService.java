package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobRequest;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;


import jakarta.persistence.EntityNotFoundException;

@Service
public class PrintJobService {

    private static final Logger logger = LoggerFactory.getLogger(PrintJobService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final SimpMessagingTemplate messagingTemplate;

    private final PrintJobRepository printJobRepository;
    private final JobHistoryRepository jobHistoryRepository;
    
    private final UserRepository userrepository;
    
   
    @Autowired
    private NotificationLogRepository notificationLogRepository;
    
    @Autowired
    private NotificationService emailNotificationService;

    @Autowired
    private NotificationService pushNotificationService;
    private final Tika tika = new Tika();

    public PrintJobService(PrintJobRepository printJobRepository, JobHistoryRepository jobHistoryRepository,UserRepository userrepository, SimpMessagingTemplate messagingTemplate) {
        this.printJobRepository = printJobRepository;
        this.jobHistoryRepository = jobHistoryRepository;
        this.userrepository = userrepository;
        this.messagingTemplate = messagingTemplate;
        
    }


    // Method to upload file and create a new job
    @Transactional
 // Method to upload file and create a new job
  /**  public void uploadFile(MultipartFile file, String userName, String description, int pagesPrinted, double cost) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty. Please upload a valid file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
        }

        String fileType = tika.detect(file.getInputStream());
        validateFileType(fileType);

        // Retrieve the User based on userName
        User user = userrepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));

        byte[] fileData = file.getBytes();
        PrintJob printJob = new PrintJob();
        printJob.setFileName(file.getOriginalFilename());
        printJob.setFileType(fileType);
        printJob.setFileSize(file.getSize());
        printJob.setUser(user);  // Set the user object, not just the username
        printJob.setUploadTimestamp(LocalDateTime.now());
        printJob.setFileData(fileData);
        printJob.setDescription(description);
        printJob.setPagesPrinted(pagesPrinted);
        printJob.setCost(cost);
        printJob.setStatus(PrintJobStatus.SUBMITTED);
        printJob.setSubmittedAt(LocalDateTime.now());

        printJobRepository.save(printJob);

        // Retrieve current user ID
        Long currentUserId = getCurrentUserId();

        // Log job submission
        logJobAction(printJob.getId(), null, PrintJobStatus.SUBMITTED, currentUserId, "Job submitted by user");
    }  **/
    
    
    

    public void uploadFile(MultipartFile file, String userName, String description, int pagesPrinted, double cost) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty. Please upload a valid file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + " MB.");
        }

        String fileType = tika.detect(file.getInputStream());
        validateFileType(fileType);

        // Retrieve the User entity based on userName
        User user = userrepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));

        byte[] fileData = file.getBytes();
        PrintJob printJob = new PrintJob();
        printJob.setFileName(file.getOriginalFilename());
        printJob.setFileType(fileType);
        printJob.setFileSize(file.getSize());
        printJob.setUser(user);  // Set the User entity
        printJob.setUserName(user.getUsername()); // Populate the userName field
        printJob.setUploadTimestamp(LocalDateTime.now());
        printJob.setFileData(fileData);
        printJob.setDescription(description);
        printJob.setPagesPrinted(pagesPrinted);
        printJob.setCost(cost);
        printJob.setStatus(PrintJobStatus.SUBMITTED);
        printJob.setSubmittedAt(LocalDateTime.now());


        printJobRepository.save(printJob);

      // Log job submission
       logJobAction(printJob.getId(), PrintJobStatus.SUBMITTED, "Job submitted by user",Optional.of(printJob.getUserName()));


        // Retrieve current user ID for logging
        Long currentUserId = getCurrentUserId();

        // Log job submission
        logJobAction(printJob.getId(), null, PrintJobStatus.SUBMITTED, currentUserId, "Job submitted by user");

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
        logJobAction(printJob.getId(), printJob.getStatus(), printJob.getStatus(), getCurrentUserId(), "File downloaded by user");
        return new ByteArrayResource(printJob.getFileData()); 
    }

    // Find PrintJob by ID
    public PrintJob findPrintJobById(Long id) {
        return printJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PrintJob not found with id: " + id));
    }

    public void updateJobStatus(Long jobId, PrintJobStatus status, String comments) {
        // Retrieve the job with authorization check (only job owner or admin can access)
        PrintJob printJob = findJobIfAuthorized(jobId);

        // Determine if the current user is an admin
        boolean isAdmin = hasRole("ROLE_ADMIN");

        // Restrict status updates for regular users to specific actions
        if (!isAdmin && !isStatusAllowedForUser(status)) {
            throw new AccessDeniedException("Only admins can change the job status to " + status);
        }

        // Save the current status as the previous status before updating
        PrintJobStatus previousStatus = printJob.getStatus();

        // Enforce valid transitions based on the target status
        switch (status) {
            case PAUSED:
                if (previousStatus != PrintJobStatus.PRINTING && previousStatus != PrintJobStatus.QUEUED) {
                    throw new IllegalStateException("Only jobs in PRINTING or QUEUED status can be paused.");
                }
                break;
            case READY:
                if (previousStatus != PrintJobStatus.PAUSED) {
                    throw new IllegalStateException("Only paused jobs can be marked as READY.");
                }
                break;
            case PRINTING:
                if (previousStatus != PrintJobStatus.READY && previousStatus != PrintJobStatus.QUEUED) {
                    throw new IllegalStateException("Jobs must be in READY or QUEUED status to start printing.");
                }
                break;
            case COMPLETED:
                if (previousStatus != PrintJobStatus.PRINTING) {
                    throw new IllegalStateException("Only jobs in PRINTING status can be marked as COMPLETED.");
                }
                break;
            // Other status transitions as needed
        }

        // Update the job's status and corresponding timestamp
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

        // Save the updated print job
        savePrintJob(printJob);

        logJobAction(jobId, status, comments,Optional.of(printJob.getUserName()));


        // Get the current user ID
        Long currentUserId = getCurrentUserId();

        // Log the action for the job update with previous and current statuses
        logJobAction(jobId, previousStatus, status, currentUserId, comments);

        // Check if the job status is FAILED or PAUSED and trigger notifications
        if (status == PrintJobStatus.FAILED || status == PrintJobStatus.PAUSED) {
            String message = "Alert: Job ID " + jobId + " has been " + status.toString().toLowerCase();
            emailNotificationService.sendEmailNotification("Job Status Alert", message);
            pushNotificationService.sendPushNotification("Job Status Alert", message);
            logNotification("sanjanajose97@gmail.com", message);
        }

    }


    
    
    private boolean isStatusAllowedForUser(PrintJobStatus status) {
        return status == PrintJobStatus.PAUSED || 
               status == PrintJobStatus.FAVORITE;
    }


    // This method logs the notification (for auditing purposes)
    private void logNotification(String recipient, String message) {
        NotificationLog log = new NotificationLog(recipient, message, "ALERT", LocalDateTime.now());
        // Assuming you have a NotificationLogRepository to save the log
        notificationLogRepository.save(log);
    }


    public void savePrintJob(PrintJob printJob) {
        printJobRepository.save(printJob);
    }

    public void prioritizeJob(Long jobId) {
        PrintJob jobToPrioritize = findPrintJobById(jobId);

        // Capture the previous priority for logging
        Integer previousPriority = jobToPrioritize.getPriority();

        // Set the highest priority (1)
        jobToPrioritize.setPriority(1);
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
        logJobAction(jobId, PrintJobStatus.QUEUED, "Job prioritized by Admin",Optional.empty());
        messagingTemplate.convertAndSend("/topic/job-queue", jobs); // Broadcast updated queue

        // Save the reordered jobs
        printJobRepository.saveAll(jobs);

        // Get the current user ID
        Long currentUserId = getCurrentUserId();

        // Log the prioritization action with priority details in the description
        String description = "Job prioritized by Admin. Previous priority: " + 
                             (previousPriority != null ? previousPriority : "none") +
                             ", New priority: 1";
        logJobAction(jobId, null, null, currentUserId, description);

        // Broadcast the updated job queue via WebSocket
        messagingTemplate.convertAndSend("/topic/job-queue", jobs);

    }

    
    

    public void cancelJob(Long jobId) {
    PrintJob printJob = findJobIfAuthorized(jobId);

    // Ensure only jobs that are not COMPLETED or DELETED can be canceled
    if (printJob.getStatus() == PrintJobStatus.COMPLETED || printJob.getStatus() == PrintJobStatus.DELETED) {
        throw new IllegalStateException("Cannot cancel a job that is already completed or deleted.");
    }

    // Capture the previous status before updating
    PrintJobStatus previousStatus = printJob.getStatus();

    // Update the job status to DELETED
    printJob.setStatus(PrintJobStatus.DELETED);
    savePrintJob(printJob); // Save the job status change

    // Log the job cancellation action with previous and new status
    Long currentUserId = getCurrentUserId(); // Get the ID of the user canceling the job
    logJobAction(jobId, previousStatus, PrintJobStatus.DELETED, currentUserId, "Job canceled by user");

    
}

    

    public void pauseJob(Long jobId) {
        PrintJob job = getJob(jobId);

        // Check if the job is in a status that allows pausing
        if (job.getStatus() == PrintJobStatus.PRINTING || job.getStatus() == PrintJobStatus.QUEUED) {
            // Capture the previous status before updating
            PrintJobStatus previousStatus = job.getStatus();

            // Update the job status to PAUSED
            updateJobStatus(jobId, PrintJobStatus.PAUSED, "Job paused by user");

            // Log the job pause action with previous and new status
            Long currentUserId = getCurrentUserId(); // Get the ID of the user pausing the job
            logJobAction(jobId, previousStatus, PrintJobStatus.PAUSED, currentUserId, "Job paused by user");

        } else {
            throw new IllegalStateException("Job can only be paused if it is in PRINTING or QUEUED status.");
        }
    }


    public void markAsFavorite(Long jobId) {
        PrintJob job = getJob(jobId);

        // Capture the previous status for logging
        PrintJobStatus previousStatus = job.getStatus();

        // Update the job status to FAVORITE
        updateJobStatus(jobId, PrintJobStatus.FAVORITE, "Job marked as favorite by user");

        // Log the action with previous and new status details
        Long currentUserId = getCurrentUserId();
        logJobAction(jobId, previousStatus, PrintJobStatus.FAVORITE, currentUserId, "Job marked as favorite by user");
    }


    public void resumeJob(Long jobId) {
        // Retrieve the job with authorization check (owner or admin only)
        PrintJob job = findJobIfAuthorized(jobId);

        // Ensure the job is currently paused before resuming
        if (job.getStatus() != PrintJobStatus.PAUSED) {
            throw new IllegalStateException("Only paused jobs can be resumed.");
        }

        // Capture the previous status for logging
        PrintJobStatus previousStatus = job.getStatus();

        // Update the job status to READY
        job.setStatus(PrintJobStatus.READY);
        job.setReadyAt(LocalDateTime.now()); // Update timestamp for READY status

        // Save the updated job status
        printJobRepository.save(job);

        // Log the resume action with previous and new status details
        Long currentUserId = getCurrentUserId();
        logJobAction(jobId, previousStatus, PrintJobStatus.READY, currentUserId, "Job resumed by user");
    }

    
    
   
    

    public void logJobAction(Long jobId, PrintJobStatus status, String actionDescription,Optional<String> userName) {


    private void logJobAction(Long jobId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus, Long userId, String comments) {

        JobHistory history = new JobHistory();
        history.setPrintJobId(jobId);
        history.setPreviousStatus(previousStatus);  // Ensure JobHistory has fields for previous and updated status
        history.setUpdatedStatus(updatedStatus);
        history.setUserId(userId);  // Store the ID of the user making the change
        history.setTimestamp(LocalDateTime.now());

       // history.setUserName(userName);
        userName.ifPresent(history::setUserName);

    	jobHistoryRepository.save(history); // Save the log entry

        history.setComments(comments);
        jobHistoryRepository.save(history); // Save the log entry

    }

    // Method to log status change
    public void logStatusChange(Long jobId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus, String comments) {
        JobHistory history = new JobHistory();
        history.setPrintJobId(jobId);
        history.setPreviousStatus(previousStatus);
        history.setUpdatedStatus(updatedStatus);
        history.setComments(comments);
        history.setTimestamp(LocalDateTime.now());

        jobHistoryRepository.save(history);
    }
    

    public List<JobHistory> getAllLogs() {
        return jobHistoryRepository.findAll();
    }
    
    

    @Transactional
    public List<JobHistory> getFilteredPrintJobs(PrintJobStatus status,String userName){
    	 List<JobHistory> jobs = null;

         if (status != null) {
             jobs = jobHistoryRepository.findByStatus(status);
         } else if (userName != null) {
             jobs = jobHistoryRepository.findByUserName(userName);
         }

		return jobs;
    	
    }
    
    

    
    @Transactional
    public List<JobHistory> getSortedPrintJobs(String sortBy, boolean sortByTime, String sortOrder,String userName) {
        List<JobHistory> jobs = jobHistoryRepository.findAll(); // Fetch all records

        if (sortByTime) {
            jobs.sort((job1, job2) -> {
                JobHistory latestHistory1 = jobHistoryRepository.findTopByPrintJobIdOrderByTimestampDesc(job1.getPrintJobId());
                JobHistory latestHistory2 = jobHistoryRepository.findTopByPrintJobIdOrderByTimestampDesc(job2.getPrintJobId());

                // Check for null values to avoid NullPointerException
                if (latestHistory1 == null && latestHistory2 == null) {
                    return 0;
                } else if (latestHistory1 == null) {
                    return 1; // Treat null as greater to place it at the end
                } else if (latestHistory2 == null) {
                    return -1; // Treat null as greater to place it at the end
                } else {
                    int comparison = latestHistory1.getTimestamp().compareTo(latestHistory2.getTimestamp());
                    // Reverse the order if descending
                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                }
            });
        } else {
            // Optional: Sort by another field if sortByTime is false
            jobs.sort((job1, job2) -> {
                int comparison = job1.getTimestamp().compareTo(job2.getTimestamp());
                return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
            });
        }

        return jobs;
    }

    
    
}
  
    public PrintJob findJobIfAuthorized(Long jobId) {
        PrintJob job = printJobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Get the authenticated user’s ID and roles
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ROLE_ADMIN");

        // Check if the user is an admin or the job owner
        if (isAdmin || job.getUserId().equals(currentUserId)) {
            return job;
        }

        throw new AccessDeniedException("User is not authorized to manage this job");
    }

    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            logger.error("User is not authenticated.");
            throw new IllegalStateException("User is not authenticated.");
        }

        if (!(auth.getPrincipal() instanceof CustomUserDetails)) {
            logger.error("Invalid principal type: " + auth.getPrincipal());
            throw new IllegalStateException("Invalid principal type.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        logger.debug("Authenticated user ID: " + userDetails.getId());
        return userDetails.getId();
    }

    

    // Helper method to check if the current user has a specified role
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                   .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
    
    public void addJob(PrintJobRequest jobRequest) {
        PrintJob job = new PrintJob();
        job.setStatus(PrintJobStatus.QUEUED);
        job.setUserId(jobRequest.getUserId());
        job.setDescription(jobRequest.getDescription());
        job.setQueuePosition(getNextQueuePosition());

        // Save the new job in the repository
        printJobRepository.save(job);

        // Log the job addition action with relevant details
        Long currentUserId = getCurrentUserId();
        logJobAction(job.getId(), null, PrintJobStatus.QUEUED, currentUserId, "Job added to the queue");
    }


    private int getNextQueuePosition() {
        Integer maxPosition = printJobRepository.findMaxQueuePosition();
        return (maxPosition == null ? 0 : maxPosition + 1);
    }

    
    public void logQueuePositionChange(Long jobId, int previousPosition, int newPosition, Long userId, String actionDescription) {
        JobHistory history = new JobHistory();
        history.setPrintJobId(jobId);
        history.setPreviousPosition(previousPosition); // Add this field in JobHistory if needed
        history.setNewPosition(newPosition);           // Add this field in JobHistory if needed
        history.setUserId(userId);
        history.setComments(actionDescription);
        history.setTimestamp(LocalDateTime.now());

        jobHistoryRepository.save(history);
    }

    
    public void reorderJob(Long jobId, int newPosition) {
        // Retrieve the job with authorization check
        PrintJob job = findJobIfAuthorized(jobId);

        // Capture the previous position for logging
        int previousPosition = job.getQueuePosition();

        // Adjust positions of other jobs in the queue
        adjustQueuePositions(job, newPosition);

        // Update the job’s position and save it
        job.setQueuePosition(newPosition);
        printJobRepository.save(job);

        // Create a new JobHistory entry to track the change
        JobHistory jobHistory = new JobHistory();
        jobHistory.setPrintJobId(jobId);  // Set the job ID
        jobHistory.setPreviousStatus(job.getStatus());  // Set the previous status of the print job

        // Ensure updatedStatus is never null
        PrintJobStatus updatedStatus = job.getStatus();  // Assuming job.getStatus() returns PrintJobStatus

        if (updatedStatus == null) {
            updatedStatus = PrintJobStatus.UNKNOWN;  // Set to a default enum value
        }

        jobHistory.setUpdatedStatus(updatedStatus);  // Set the status as PrintJobStatus enum
  // Ensure updatedStatus is always set

        jobHistory.setTimestamp(LocalDateTime.now());  // Set the timestamp of the change
        jobHistory.setComments("Job reordered in the queue");  // Set the comment for the action

        // Set position change information
        jobHistory.setPreviousPosition(previousPosition);  // Set the previous position
        jobHistory.setNewPosition(newPosition);  // Set the new position

        // Get the current user ID for logging
        Long currentUserId = getCurrentUserId();
        jobHistory.setUserId(currentUserId);  // Set the user who performed the action

        // Save the JobHistory entry
        jobHistoryRepository.save(jobHistory);  // Save the job history

        // Optionally, log the action for debugging or auditing purposes
        logQueuePositionChange(jobId, previousPosition, newPosition, currentUserId, "Job reordered in the queue");
    }


    private void adjustQueuePositions(PrintJob job, int newPosition) {
        int oldPosition = job.getQueuePosition();
        
        if (newPosition > oldPosition) {
            // Moving down in the queue - shift jobs between oldPosition and newPosition up by 1
            List<PrintJob> jobsToShiftUp = printJobRepository
                .findByQueuePositionBetween(oldPosition + 1, newPosition);
            for (PrintJob j : jobsToShiftUp) {
                j.setQueuePosition(j.getQueuePosition() - 1);
            }
            printJobRepository.saveAll(jobsToShiftUp);
            
        } else if (newPosition < oldPosition) {
            // Moving up in the queue - shift jobs between newPosition and oldPosition down by 1
            List<PrintJob> jobsToShiftDown = printJobRepository
                .findByQueuePositionBetween(newPosition, oldPosition - 1);
            for (PrintJob j : jobsToShiftDown) {
                j.setQueuePosition(j.getQueuePosition() + 1);
            }
            printJobRepository.saveAll(jobsToShiftDown);
        }
        
        
        
    }
      
    
    public void removeJob(Long jobId) {
        PrintJob printJob = findJobIfAuthorized(jobId);

        // Check if the user is authorized to delete the job (admin or job owner)
        boolean isAdmin = hasRole("ROLE_ADMIN");
        
        if (!isAdmin && !printJob.getUserId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("Only admins or job owners can delete the job.");
        }

        // Capture the previous status before deletion
        PrintJobStatus previousStatus = printJob.getStatus();

        // Delete the job from the repository
        printJobRepository.delete(printJob);

        // Log the job removal with previous and new status
        logJobAction(jobId, previousStatus, PrintJobStatus.DELETED, getCurrentUserId(), "Job permanently removed from the database");
    }

    
    
    private PrintJob getJob(Long jobId) {
        return printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
    }


    
    
}

