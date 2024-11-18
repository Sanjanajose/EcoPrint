package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.exception.NetworkException;
import com.ecoprint.printmanagement.exception.PrinterException;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.FailureReason;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobRequest;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.RoleRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;


import jakarta.persistence.EntityNotFoundException;

@Service
public class PrintJobService {
    @Value("${retry.maxRetryCount:3}")
    private int maxRetryCount;

    @Value("${retry.interval.network:PT1M}") // Default 1 minute for network issues
    private String retryNetworkInterval;

    @Value("${retry.interval.printer:PT5M}") // Default 5 minutes for printer issues
    private String retryPrinterInterval;

    @Value("${retry.interval.default:PT2M}") // Default interval for other cases
    private String defaultRetryInterval;

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
    private RoleRepository roleRepository;
    
   


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
    
    
    
 // Method to upload file and create a new job

   
    public void uploadFile(MultipartFile file, String userName, String description, int pagesPrinted, double cost,String color,Boolean duplex,String paperSize) 
            throws IOException {

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
        printJob.setColor(color);
        printJob.setDuplex(duplex);
        printJob.setPaperSize(paperSize);
        printJob.setStatus(PrintJobStatus.SUBMITTED);
        printJob.setSubmittedAt(LocalDateTime.now());
        printJobRepository.save(printJob);



        // Retrieve current user ID for logging

        Long currentUserId = getCurrentUserId();

        // Log job submission
        

        logJobAction(printJob.getId(), PrintJobStatus.SUBMITTED, PrintJobStatus.SUBMITTED, currentUserId, 
                     "Job submitted by user", Optional.of(printJob.getUserName()));
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
        // Retrieve the print job based on the provided ID
        PrintJob printJob = findPrintJobById(id);

        // Log the download action for auditing purposes
        Long currentUserId = getCurrentUserId();
        logJobAction(printJob.getId(), printJob.getStatus(), printJob.getStatus(), currentUserId, "File downloaded by user", Optional.empty());

        // Return the file as a ByteArrayResource for download
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
            case QUEUED:
                printJob.setQueuedAt(now);
                break;
            case PAUSED:
                printJob.setPausedAt(now);
                break;
            case READY:
                printJob.setReadyAt(now);
                break;
            case PRINTING:
                printJob.setPrintingAt(now);
                break;
            case COMPLETED:
                printJob.setCompletedAt(now);
                break;
            case FAILED:
                printJob.setFailedAt(now);
                break;
            case DELETED:
                printJob.setDeletedAt(now);
                break;
            case FAVORITE:
                printJob.setFavoriteAt(now);
                break;
        }

        // Save the updated print job
        savePrintJob(printJob);

        // Log job action with updated status and user info
        Long currentUserId = getCurrentUserId();
        logJobAction(jobId, previousStatus, status, currentUserId, comments, Optional.of(printJob.getUserName()));

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
        // Retrieve the job to be prioritized
        PrintJob jobToPrioritize = findPrintJobById(jobId);

        // Capture the previous priority for logging
        Integer previousPriority = jobToPrioritize.getPriority();

        // Set the highest priority (1)
        jobToPrioritize.setPriority(1);
        savePrintJob(jobToPrioritize);  // Save the prioritized job immediately

        // Retrieve and reorder other jobs in the queue, excluding the one just prioritized
        List<PrintJob> jobs = printJobRepository.findByStatusOrderByPriorityAsc(PrintJobStatus.QUEUED);
        int currentPriority = 2;  // Start from the next priority after the highest

        // Reorder jobs in the queue to ensure no conflicts
        for (PrintJob job : jobs) {
            if (!job.getId().equals(jobId) && (job.getPriority() == null || job.getPriority() != currentPriority)) {
                job.setPriority(currentPriority++);
            }
        }

        // Save the reordered jobs in batch (only once)
        printJobRepository.saveAll(jobs);  // Save all updated jobs

        // Log the prioritization action with previous and new priority details
        Long currentUserId = getCurrentUserId();  // Get the current user ID
        String description = "Job prioritized by Admin. Previous priority: " + 
                             (previousPriority != null ? previousPriority : "none") + 
                             ", New priority: 1";
        
        logJobAction(jobId, PrintJobStatus.QUEUED, PrintJobStatus.QUEUED, currentUserId, description, Optional.of(jobToPrioritize.getUserName()));

        // Broadcast the updated job queue via WebSocket (only once)
        messagingTemplate.convertAndSend("/topic/job-queue", jobs); // Broadcast updated queue
    }


    
    

    public void cancelJob(Long jobId) {
        // Retrieve the job and ensure the user has authorization
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
        logJobAction(jobId, previousStatus, PrintJobStatus.DELETED, currentUserId, "Job canceled by user", Optional.empty());
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
            logJobAction(jobId, previousStatus, PrintJobStatus.PAUSED, currentUserId, "Job paused by user", Optional.ofNullable(job.getUserName()));

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
        logJobAction(jobId, previousStatus, PrintJobStatus.FAVORITE, currentUserId, "Job marked as favorite", Optional.ofNullable(job.getUserName()));
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
        logJobAction(jobId, previousStatus, PrintJobStatus.READY, currentUserId, "Job resumed by user", Optional.empty());
    }


     
    
    public void logJobAction(Long jobId,  PrintJobStatus previousStatus, PrintJobStatus updatedStatus, Long userId,  String comments,  Optional<String> userName) {
    		JobHistory history = new JobHistory();
    		history.setPrintJobId(jobId);
    		history.setPreviousStatus(previousStatus);
    		history.setUpdatedStatus(updatedStatus);
    		history.setUserId(userId);
    		history.setTimestamp(LocalDateTime.now());
    		userName.ifPresent(history::setUserName);
    		history.setComments(comments);

    		jobHistoryRepository.save(history);  // Save the log entry
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
    public List<JobHistory> getFilteredPrintJobs(PrintJobStatus status, String userName) {
        List<JobHistory> jobs = null;

        // Filter by updatedStatus if status is provided
        if (status != null) {
            jobs = jobHistoryRepository.findByUpdatedStatus(status);
        } 
        // Filter by userName if provided
        else if (userName != null) {
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
        // Retrieve the authenticated user's email from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        // Find the user by email
        User user = userrepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        // Ensure fileName is provided in the request
        if (jobRequest.getFileName() == null || jobRequest.getFileName().isEmpty()) {
            throw new IllegalArgumentException("fileName must not be null or empty");
        }

        // Retrieve the file associated with the provided fileName
        PrintJob uploadedFile = printJobRepository.findByFileName(jobRequest.getFileName())
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileName", jobRequest.getFileName()));

        // Proceed with creating the PrintJob
        PrintJob job = new PrintJob();
        job.setStatus(PrintJobStatus.QUEUED);
        job.setUser(user);
        job.setDescription(jobRequest.getDescription());
        job.setQueuePosition(assignNextQueuePosition());
        job.setFileData(uploadedFile.getFileData());  // Set fileData from the previously uploaded file

        // Save the job
        printJobRepository.save(job);

        // Log the action
        logJobAction(job.getId(), null, PrintJobStatus.QUEUED, user.getId(), "Job added to queue", Optional.of(user.getUsername()));
    }



    // Method to calculate the next available queue position
    private int getNextQueuePosition() {
        Integer maxPosition = printJobRepository.findMaxQueuePosition();
        return (maxPosition == null ? 0 : maxPosition + 1);
    }

 // Method to determine the priority of a job based on the user's roles
    public int determinePriority(Long userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId); // Fetch roles based on userId
        for (Role role : roles) {
            // Use the getRole() method to get the RoleName
            if (role.getRole() == RoleName.ROLE_ADMIN) {
                return 1;  // Highest priority
            } else if (role.getRole() == RoleName.ROLE_USER) {
                return 3;  // Lowest priority
            }
        }
        return 2;  // Default priority if no matching role is found
    }

    
    public void logQueuePositionChange(Long jobId, int previousPosition, int newPosition, Long userId, String actionDescription) {
        JobHistory history = new JobHistory();
        history.setPrintJobId(jobId);
        history.setPreviousPosition(previousPosition); // Add this field in JobHistory if needed
        history.setNewPosition(newPosition);           // Add this field in JobHistory if needed
        history.setUserId(userId);
        history.setComments(actionDescription);
        history.setTimestamp(LocalDateTime.now());
        history.setUpdatedStatus(PrintJobStatus.UNKNOWN); // Replace SOME_DEFAULT_STATUS with an appropriate enum value

        jobHistoryRepository.save(history);
    }

    
    public void reorderJob(Long jobId, int newPosition) {
        // Retrieve the job with authorization check (ensure the user is authorized to update the job)
        PrintJob job = findJobIfAuthorized(jobId);

        // Check if the job status is QUEUED; if not, throw an exception
        if (job.getStatus() != PrintJobStatus.QUEUED) {
            throw new IllegalStateException("Only jobs with status QUEUED can be reordered.");
        }

        // Ensure that the new position is valid (it should not be the same as the current position)
        if (newPosition == job.getQueuePosition()) {
            throw new IllegalArgumentException("The job is already at the requested position.");
        }

        // Capture the previous position for logging
        int previousPosition = job.getQueuePosition();

        // Adjust positions of other jobs in the queue to make room for the new position
        adjustQueuePositions(job, newPosition);

        // Update the job’s position and save it
        job.setQueuePosition(newPosition);
        printJobRepository.save(job); // Save the updated job with new position

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

        jobHistory.setPrintJobId(jobId);
        jobHistory.setPreviousStatus(job.getStatus());


        // Set the updated status and timestamp
        jobHistory.setUpdatedStatus(job.getStatus() != null ? job.getStatus() : PrintJobStatus.UNKNOWN);
        jobHistory.setTimestamp(LocalDateTime.now());
        jobHistory.setComments("Job reordered in the queue");
        jobHistory.setPreviousPosition(previousPosition);
        jobHistory.setNewPosition(newPosition);

        // Get the current user ID for logging purposes
        Long currentUserId = getCurrentUserId();
        jobHistory.setUserId(currentUserId);

        // Save the JobHistory entry
        jobHistoryRepository.save(jobHistory);

        // Log the queue position change
        logQueuePositionChange(jobId, previousPosition, newPosition, currentUserId, "Job reordered in the queue");
    }

    
    private void adjustQueuePositions(PrintJob job, int newPosition) {
        int oldPosition = job.getQueuePosition();

        if (newPosition > oldPosition) {
            // Moving the job down in the queue - shift jobs up by 1
            List<PrintJob> jobsToShiftUp = printJobRepository.findByQueuePositionBetween(oldPosition + 1, newPosition);
            for (PrintJob j : jobsToShiftUp) {
                j.setQueuePosition(j.getQueuePosition() - 1);
            }
            printJobRepository.saveAll(jobsToShiftUp);
        } else if (newPosition < oldPosition) {
            // Moving the job up in the queue - shift jobs down by 1
            List<PrintJob> jobsToShiftDown = printJobRepository.findByQueuePositionBetween(newPosition, oldPosition - 1);
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
        Long currentUserId = getCurrentUserId();
        logJobAction(jobId, previousStatus, PrintJobStatus.DELETED, currentUserId, "Job permanently removed from the database", Optional.empty());
    }


    
    
   private PrintJob getJob(Long jobId) {
        return printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
    }

    public int assignNextQueuePosition() {
        Integer maxPosition = printJobRepository.findMaxQueuePosition();
        return (maxPosition == null ? 1 : maxPosition + 1);
    }

   public List<PrintJob> getReadyJobs() {
	    List<PrintJob> readyJobs = printJobRepository.findByStatus(PrintJobStatus.READY);
	    List<PrintJob> responseList = new ArrayList<>();
	    for (PrintJob job : readyJobs) {
	        if (job.getId() == null || job.getId() == 0) {
	            throw new IllegalArgumentException("Invalid job ID: " + job.getId());
	        }	        
	        PrintJob printJob = new PrintJob();
	        printJob.setId(job.getId());
	        printJob.setFileName(job.getFileName());	        
	        responseList.add(printJob);
	    }
	    return responseList;
	}
   
 
      
   
   
   public void processPrintJob(PrintJob job) {
	    try {
	        updateJobStatus(job.getId(), PrintJobStatus.QUEUED, "Print job added to queue");
	             
	        if (job.hasNetworkConnectivityIssues()) {
	            throw new NetworkException("Network connectivity issue detected");
	        } else if (job.hasPrinterHardwareIssues()) {
	            throw new PrinterException("Printer hardware issue detected");
	        }


	        // Additional job processing logic

	    } catch (NetworkException e) {
	        handleJobFailure(job, FailureReason.NETWORK_ISSUE);
	    } catch (PrinterException e) {
	        handleJobFailure(job, FailureReason.PRINTER_ERROR);
	    } catch (Exception e) {
	        handleJobFailure(job, FailureReason.UNKNOWN_ERROR);
	    }
	}


   
 
  
   
   public boolean retryFailedJobById(Long jobId) {
	    // Find the job by its ID
	    Optional<PrintJob> optionalJob = printJobRepository.findById(jobId);
	    
	    if (optionalJob.isPresent()) {
	        PrintJob job = optionalJob.get();
	        
	        // Check if the job is in a FAILED state and is eligible for retry
	        if (job.getStatus() == PrintJobStatus.FAILED && job.getRetryCount() < maxRetryCount) {
	            // Increment retry count and set status to RETRYING
	            job.incrementRetryCount();
	            job.setStatus(PrintJobStatus.QUEUED);
	            printJobRepository.save(job);         
	            // Attempt to process the job again
	            processPrintJob(job);
	            return true; // Retry was successfully triggered
	        }
	    }
	    
	    return false; // Job not found or not eligible for retry
	}


   public void handleJobFailure(PrintJob job, FailureReason failureReason) {
	    // Increment retry count
	    job.setRetryCount(job.getRetryCount() + 1);
	    
	    // Set job status to FAILED if retry limit reached, otherwise RETRYING
	    if (job.getRetryCount() >= maxRetryCount) {
	        job.setStatus(PrintJobStatus.FAILED);
	       // sendAlert(job);  // Optional: Send alert if max retries reached
	    } else {
	        job.setStatus(PrintJobStatus.QUEUED);	        
	        // Set failure reason
	        job.setFailureReason(failureReason);	        
	        // Calculate the next retry time based on failure reason
	        Duration retryInterval;
	        switch (failureReason) {
	            case NETWORK_ISSUE:
	                retryInterval = Duration.parse(retryNetworkInterval);
	                break;
	            case PRINTER_ERROR:
	                retryInterval = Duration.parse(retryPrinterInterval);
	                break;
	            default:
	                retryInterval = Duration.parse(defaultRetryInterval);
	        }
	        job.setNextRetryTime(LocalDateTime.now().plus(retryInterval));
	    }	    
	    // Save the job's updated state to the database
	   // jobRepository.save(job);
	}
    
    }
    

