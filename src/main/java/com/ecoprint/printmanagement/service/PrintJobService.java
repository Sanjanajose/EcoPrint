package com.ecoprint.printmanagement.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.ecoprint.printmanagement.model.Priority;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.RoleRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class PrintJobService {
	
	
	

    private static final Logger logger = LoggerFactory.getLogger(PrintJobService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final SimpMessagingTemplate messagingTemplate;

    private final JobHistoryRepository jobHistoryRepository;
    
    private final UserRepository userrepository;
    
   
    @Autowired
    private NotificationLogRepository notificationLogRepository;
    
    @Autowired
    private NotificationService emailNotificationService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PrintJobRepository printJobRepository;
    
   
    @Autowired
    private UserService userService;  // To get current user info

   


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
        
        String fileName = file.getOriginalFilename(); // Extract file name
        
        PrintJob printJob = new PrintJob();
        printJob.setFileName(fileName);  // Set file name
        printJob.setFileType(fileType);  // Set file type
        printJob.setFileSize(file.getSize());  // Set file size
        printJob.setUser(user);  // Set the User entity
        printJob.setUserName(user.getUsername());  // Populate the userName field
        printJob.setUploadTimestamp(LocalDateTime.now());  // Timestamp for upload
        printJob.setFileData(fileData);  // Set the file data
        printJob.setDescription(description);  // Set description
        printJob.setPagesPrinted(pagesPrinted);  // Set pages printed
        printJob.setCost(cost);  // Set cost
        printJob.setStatus(PrintJobStatus.SUBMITTED);  // Set job status as SUBMITTED
        printJob.setSubmittedAt(LocalDateTime.now());  // Set submission timestamp
        printJobRepository.save(printJob);

        // Retrieve current user ID for logging
        Long currentUserId = getCurrentUserId();

        // Log job submission
        
        // Step 8: Log the job submission action
        logJobAction(
            printJob.getId(),  // Job ID
            PrintJobStatus.SUBMITTED,  // Previous status (if any)
            PrintJobStatus.SUBMITTED,  // Updated status (after file upload)
            currentUserId,  // Current user ID (assuming a method to get it)
            "Job submitted with file upload",  // Log message
            Optional.of(printJob.getUserName()),  // User name (optional)
            Optional.empty(),  // No position info (optional)
            Optional.empty(),  // No position info (optional)
            "file_upload",  // Action type (e.g., file upload)
            Optional.of(fileName),  // File name (passed as Optional)
            Optional.of(file.getSize())  // File size (passed as Optional)
        );

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

    public Resource downloadFile(Long id) {
        // Step 1: Retrieve the print job based on the provided ID
        PrintJob printJob = findPrintJobById(id);  // Assuming this method exists and finds the PrintJob by ID
        if (printJob == null) {
            throw new ResourceNotFoundException("PrintJob", "id", id.toString());
        }

        // Step 2: Retrieve the current user and their username
        Long currentUserId = getCurrentUserId();  // Assuming a method to fetch the current logged-in user ID
        User currentUser = userrepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId.toString()));
        String userName = currentUser.getUsername();  // Get the username for logging

        // Step 3: Log the download action for auditing purposes
        logJobAction(printJob.getId(), printJob.getStatus(), printJob.getStatus(), currentUserId, 
                     "File downloaded by user", Optional.of(userName), 
                     Optional.empty(), Optional.empty(), "file_download", 
                     Optional.of(printJob.getFileName()), Optional.of((long) printJob.getFileData().length));

        // Step 4: Return the file as a ByteArrayResource for download
        return new ByteArrayResource(printJob.getFileData());  // Assuming 'getFileData()' returns the byte data of the file
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
        logJobAction(jobId, previousStatus, status, currentUserId, comments, Optional.of(printJob.getUserName()), Optional.empty(), Optional.empty(), "status_update", Optional.empty(), Optional.empty());


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
    
    
    public void setJobPriority(Long jobId, Priority priority) {
        // Get the current user ID
        Long currentUserId = getCurrentUserId();

        // Fetch the job, ensuring it's authorized for the current user
        PrintJob job = findJobIfAuthorized(jobId);

        // Retrieve the previous priority
        Priority previousPriority = job.getPriority();
        

        // Check if priority is null before proceeding
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }

        // Log the current and new priorities before updating
        logger.debug("Job ID: {}, Previous Priority: {}, New Priority: {}", jobId, previousPriority, priority);

        // Update the job's priority
        job.setPriority(priority);

        // Save the updated job
        printJobRepository.save(job);
        
     // Log the priority update action
        logJobAction(jobId, job.getStatus(), job.getStatus(), currentUserId, 
                     "Job priority updated to " + priority, 
                     Optional.of(job.getUserName()), // User who owns the print job
                     Optional.empty(), 
                     Optional.empty(), 
                     "priority_update", // Action type
                     Optional.of(priority.name()), // Updated priority
                     Optional.empty()); // No file size or name needed
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
       
        logJobAction(jobId, previousStatus, PrintJobStatus.DELETED, currentUserId, "Job canceled by user", 
                     Optional.of(printJob.getUserName()), Optional.empty(), Optional.empty(), "job_cancellation", Optional.empty(), Optional.empty());
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
            logJobAction(
                    jobId,                                 // Job ID
                    previousStatus,                        // Previous status (before update)
                    PrintJobStatus.PAUSED,                 // Updated status (PAUSED in this case)
                    currentUserId,                         // User ID performing the action
                    "Job paused by user",                  // Action description
                    Optional.ofNullable(job.getUserName()), // User's username (wrapped in Optional)
                    Optional.empty(),                      // No previous position (if not needed, use Optional.empty())
                    Optional.empty(),                      // No new position (if not needed, use Optional.empty())
                    "pause",                               // Action type (this can be "pause" for the pausing action)
                    Optional.empty(),                      // No file name involved
                    Optional.empty()                       // No file size involved
                );


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
        logJobAction(
        	    jobId,                                  // Job ID
        	    previousStatus,                         // Previous status (before update)
        	    PrintJobStatus.PAUSED,                  // Updated status (PAUSED in this case)
        	    currentUserId,                          // User ID performing the action
        	    "Job paused by user",                   // Action description
        	    Optional.ofNullable(job.getUserName()), // User's username (wrapped in Optional)
        	    Optional.empty(),                       // No previous position (if not needed, use Optional.empty())
        	    Optional.empty(),                       // No new position (if not needed, use Optional.empty())
        	    "pause",                                // Action type (this can be "pause" for the pausing action)
        	    Optional.empty(),                       // No file name involved
        	    Optional.empty()                        // No file size involved
        	);
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
        logJobAction(
                jobId,                                      // Job ID
                previousStatus,                             // Previous status (before update)
                PrintJobStatus.READY,                       // Updated status (READY in this case)
                currentUserId,                              // User ID performing the action
                "Job resumed by user",                      // Action description
                Optional.empty(),                           // No userName (if not available)
                Optional.empty(),                           // No previous position (if not relevant)
                Optional.empty(),                           // No new position (if not relevant)
                "resume",                                   // Action type (this can be "resume" for the resuming action)
                Optional.empty(),                           // No file name involved
                Optional.empty()                            // No file size involved
            );
    }


     
    
    /**public void logJobAction(Long jobId,  PrintJobStatus previousStatus, PrintJobStatus updatedStatus, Long userId,  String comments,  Optional<String> userName) {
    		JobHistory history = new JobHistory();
    		history.setPrintJobId(jobId);
    		history.setPreviousStatus(previousStatus);
    		history.setUpdatedStatus(updatedStatus);
    		history.setUserId(userId);
    		history.setTimestamp(LocalDateTime.now());
    		userName.ifPresent(history::setUserName);
    		history.setComments(comments);

    		jobHistoryRepository.save(history);  // Save the log entry
    } **/
    
    public void logJobAction(Long jobId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus, 
            Long userId, String comments, Optional<String> userName, 
            Optional<Integer> previousPosition, Optional<Integer> newPosition, 
            String actionType, Optional<String> fileName, Optional<Long> fileSize) {

    		if (jobId == null || previousStatus == null || updatedStatus == null || userId == null || comments == null) {
    			throw new IllegalArgumentException("Job ID, previous status, updated status, user ID, and comments cannot be null");
    		}

    		// Create the JobHistory entry
    		JobHistory history = new JobHistory();
    		history.setPrintJobId(jobId);
    		history.setPreviousStatus(previousStatus);
    		history.setUpdatedStatus(updatedStatus);
    		history.setUserId(userId);
    		history.setTimestamp(LocalDateTime.now());
    		userName.ifPresent(history::setUserName);  // Optional userName
    		history.setComments(comments);

    		// Set file-related fields if they are present
    		fileName.ifPresent(history::setUserName);  // Set file name if present
    		fileSize.ifPresent(size -> history.setFileSize(size));
    		// Set file size if present

    		// Set position changes if available
    		previousPosition.ifPresent(history::setPreviousPosition);
    		newPosition.ifPresent(history::setNewPosition);

    		try {
    			jobHistoryRepository.save(history);  // Save the log entry
    		} catch (Exception e) {
    			System.err.println("Error logging job action: " + e.getMessage());
    		}
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
    
    @Transactional
    public void addJob(PrintJobRequest jobRequest) {
        // Retrieve the authenticated user's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        // Find the user by email
        User user = userrepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", currentEmail));

        // Validate that fileName is not null or empty
        if (jobRequest.getFileName() == null || jobRequest.getFileName().isEmpty()) {
            throw new IllegalArgumentException("fileName must not be null or empty");
        }

        // Retrieve the PrintJob using the provided fileName from the previously uploaded jobs 
        // Ensure that the same file name is not re-used by different jobs
        PrintJob existingJob = printJobRepository.findByFileName(jobRequest.getFileName())
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "fileName", jobRequest.getFileName()));

        // Create a new PrintJob object to add to the queue
        PrintJob job = new PrintJob();
        job.setStatus(PrintJobStatus.READY);  // Set the status to 'QUEUED'
        job.setUser(user);  // Associate the job with the authenticated user
        job.setDescription(jobRequest.getDescription());  // Set the description from jobRequest
        job.setQueuePosition(assignNextQueuePosition());  // Get the next queue position from the service
        job.setFileName(jobRequest.getFileName());  // Reusing the uploaded file name
        job.setPagesPrinted(jobRequest.getPages());  // Set pages from jobRequest
        job.setFileData(existingJob.getFileData());  // Re-use the existing file data from previously uploaded job

        // Save the new job to the database
        printJobRepository.save(job);

        // Log the action with job ID, previous status, and new status
        logJobAction(
        	    job.getId(),                          // Job ID (current job ID)
        	    existingJob.getStatus(),              // Previous status of the job (status before update)
        	    PrintJobStatus.READY,                // New status of the job (Ready status as it's being added to the Ready list)
        	    user.getId(),                         // User ID (ID of the user performing the action)
        	    "Job added to queue",                 // Action description (provides context for the action)
        	    Optional.of(user.getUsername()),      // User name for logging (using Optional to handle null values)
        	    Optional.empty(),                     // Optional field for previous position (if needed for logging)
        	    Optional.empty(),                     // Optional field for new position (if needed for logging)
        	    "ready_action",                       // Action type description (custom action type such as "queue_action")
        	    Optional.empty(),                     // Optional field for file name (if you want to log the file name as well)
        	    Optional.empty()                      // Optional field for file size (if you want to log the file size)
        	);

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

    @Transactional
    public void reorderJob(Long jobId, int newPosition) {
        // Retrieve the job with authorization check
        PrintJob job = findJobIfAuthorized(jobId);

        // Log current and new position before comparison
        logger.debug("Reordering Job - Current Position: {}, Requested Position: {}", job.getQueuePosition(), newPosition);

        // Check if the job status is QUEUED; if not, throw an exception
        if (job.getStatus() != PrintJobStatus.QUEUED) {
            throw new IllegalStateException("Only jobs with status QUEUED can be reordered.");
        }

        // Ensure that the new position is valid
        if (newPosition == job.getQueuePosition()) {
            logger.info("The job is already at the requested position: {}", newPosition);
            return; // Return early
        }

        // Capture the previous position for logging
        int previousPosition = job.getQueuePosition();

        // Adjust positions of other jobs
        adjustQueuePositions(job, newPosition);

        // Update the job’s position and save it
        job.setQueuePosition(newPosition);
        printJobRepository.save(job);

        // Create a new JobHistory entry
        JobHistory jobHistory = new JobHistory();
        jobHistory.setActionType("REORDERED"); // Ensure this is set properly
        jobHistory.setComments("Reordered the job in the queue");
        jobHistory.setFileSize(job.getFileSize());
        jobHistory.setNewPosition(newPosition);
        jobHistory.setPreviousPosition(previousPosition);
        jobHistory.setPreviousStatus(job.getStatus());
        jobHistory.setPrintJobId(job.getId());
        jobHistory.setTimestamp(LocalDateTime.now());
        jobHistory.setUpdatedStatus(job.getStatus());
        Long currentUserId = getCurrentUserId();
        jobHistory.setUserId(currentUserId);
     
        // Get the current user ID for logging purposes
       
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
        logJobAction(
        	    jobId,                             // Job ID
        	    previousStatus,                    // Previous job status before deletion
        	    PrintJobStatus.DELETED,            // New status after deletion (DELETED)
        	    currentUserId,                     // User ID of the user performing the action
        	    "Job permanently removed from the database", // Description of the action
        	    Optional.empty(),                  // Optional: No need to log user name here (Empty Optional if no user name to log)
        	    Optional.empty(),                  // Optional: No additional integer fields for this action
        	    Optional.empty(),                  // Optional: No additional integer fields for this action
        	    "delete_job",                      // Action type for this event (describes the type of operation performed)
        	    Optional.empty(),                  // Optional: No file name to log for job deletion
        	    Optional.empty()                   // Optional: No file size to log for job deletion
        	);

    }


    // Method to save the print job
    public void save(PrintJob printJob) {
        printJobRepository.save(printJob);  // Saves the updated print job to the database
    }
    
    private PrintJob getJob(Long jobId) {
        return printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
    }

 // Logic to find the last queue position
    public int assignNextQueuePosition() {
        // Find the maximum queue position and add 1 to get the next position
        Integer maxPosition = printJobRepository.findMaxQueuePosition();
        return (maxPosition == null ? 1 : maxPosition + 1);
    }

    // Fetch print job by file name
    public Optional<PrintJob> findPrintJobByFileName(String fileName) {
        return printJobRepository.findByFileName(fileName); // Assuming repository has this method
    }

    
    }
    

