package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.context.annotation.Lazy;


import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.dto.QueuedJobDTO;
import com.ecoprint.printmanagement.exception.NetworkException;
import com.ecoprint.printmanagement.exception.PrinterException;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CustomUserDetails;

import com.ecoprint.printmanagement.model.FailedJob;

import com.ecoprint.printmanagement.model.FailureReason;

import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.NotificationLog;
import com.ecoprint.printmanagement.model.PrintHistoryDTO;
import com.ecoprint.printmanagement.model.PrintHistoryMap;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobDTO;
import com.ecoprint.printmanagement.model.PrintJobRequest;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.Printer;
import com.ecoprint.printmanagement.model.Priority;
import com.ecoprint.printmanagement.model.QueuedJob;
import com.ecoprint.printmanagement.model.Role;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.SubmittedJobs;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.model.UserNotificationPreferences;
import com.ecoprint.printmanagement.repository.FailedJobRepository;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.NotificationLogRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.PrinterRepository;
import com.ecoprint.printmanagement.repository.QueuedJobRepository;
import com.ecoprint.printmanagement.repository.RoleRepository;
import com.ecoprint.printmanagement.repository.SubmitJobRepository;
import com.ecoprint.printmanagement.repository.UserDeviceRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.response.ReadyJobResponse;
import com.google.api.client.util.Objects;

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

	private final JobHistoryRepository jobHistoryRepository;

	private final UserRepository userrepository;

    private final SubmitJobRepository submitJobRepository;

	@Autowired
	private NotificationLogRepository notificationLogRepository;

	@Autowired
	private QueuedJobRepository queuedJobRepository;
	
	@Autowired
	private QueueManagementService queueManagementService;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PrintJobRepository printJobRepository;
	
    @Autowired
    private UserNotificationPreferencesService userNotificationPreferencesService;

    
    @Autowired
	private PrinterRepository printerRepository;
	

    @Autowired
    private FailedJobRepository failedJobRepository;
    
    @Autowired
    private UserDeviceRepository userdevicerepository;


    @Autowired
    private AuthService authservice;

    
    @Qualifier("emailNotificationService") // Specify the bean you want to inject
    private NotificationService emailNotificationService;

    
    @Qualifier("pushNotificationService") // Specify the bean you want to inject
    private NotificationService pushNotificationService;


    // Use notificationServices.get("emailNotificationService") or notificationServices.get("pushNotificationService")


	@Autowired
	private UserService userService; // To get current user info
	
    @Lazy
    @Autowired
    private FailedJobService failedJobService;

	

	private final Tika tika = new Tika();

	public PrintJobService(PrintJobRepository printJobRepository, 
            JobHistoryRepository jobHistoryRepository,
            UserRepository userrepository, 
            SubmitJobRepository submitJobRepository,
            SimpMessagingTemplate messagingTemplate, 
            @Qualifier("emailNotificationService") NotificationService emailNotificationService,  
            @Qualifier("pushNotificationService") NotificationService pushNotificationService) {
this.printJobRepository = printJobRepository;
this.jobHistoryRepository = jobHistoryRepository;
this.userrepository = userrepository;
this.submitJobRepository = submitJobRepository;
this.messagingTemplate = messagingTemplate;
this.emailNotificationService = emailNotificationService;
this.pushNotificationService = pushNotificationService;
}

	// Method to upload file and create a new job
	//@Transactional
	// Method to upload file and create a new job
	/**
	 * public void uploadFile(MultipartFile file, String userName, String
	 * description, int pagesPrinted, double cost) throws IOException { if (file ==
	 * null || file.isEmpty()) { throw new IllegalArgumentException("File cannot be
	 * null or empty. Please upload a valid file."); } if (file.getSize() >
	 * MAX_FILE_SIZE) { throw new IllegalArgumentException("File size exceeds the
	 * maximum limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + " MB."); }
	 * 
	 * String fileType = tika.detect(file.getInputStream());
	 * validateFileType(fileType);
	 * 
	 * // Retrieve the User based on userName User user =
	 * userrepository.findByUsername(userName) .orElseThrow(() -> new
	 * ResourceNotFoundException("User", "username", userName));
	 * 
	 * byte[] fileData = file.getBytes(); PrintJob printJob = new PrintJob();
	 * printJob.setFileName(file.getOriginalFilename());
	 * printJob.setFileType(fileType); printJob.setFileSize(file.getSize());
	 * printJob.setUser(user); // Set the user object, not just the username
	 * printJob.setUploadTimestamp(LocalDateTime.now());
	 * printJob.setFileData(fileData); printJob.setDescription(description);
	 * printJob.setPagesPrinted(pagesPrinted); printJob.setCost(cost);
	 * printJob.setStatus(PrintJobStatus.SUBMITTED);
	 * printJob.setSubmittedAt(LocalDateTime.now());
	 * 
	 * printJobRepository.save(printJob);
	 * 
	 * // Retrieve current user ID Long currentUserId = getCurrentUserId();
	 * 
	 * // Log job submission logJobAction(printJob.getId(), null,
	 * PrintJobStatus.SUBMITTED, currentUserId, "Job submitted by user"); }
	 **/

	// Method to upload file and create a new job
    @Transactional
    public void uploadFile(MultipartFile file, String userName, String description, int pagesPrinted) throws IOException {
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

        if (!userrepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User does not exist");
        }
        


        byte[] fileData = file.getBytes();
        
        String fileName = file.getOriginalFilename(); // Extract file name        
        SubmittedJobs submittedJob  = new SubmittedJobs();
        submittedJob.setFileName(file.getOriginalFilename());
        submittedJob.setFileType(fileType);
        submittedJob.setFileSize(file.getSize());
        submittedJob.setUser(user); // Set the User entity
        submittedJob.setUserName(user.getUsername()); // Populate the userName field
        submittedJob.setUploadTimestamp(LocalDateTime.now());
        submittedJob.setFileData(fileData);
        submittedJob.setDescription(description);
        submittedJob.setStatus(PrintJobStatus.SUBMITTED);
        submittedJob.setPagesPrinted(pagesPrinted);
        Assert.notNull(submittedJob.getFileName(), "File name must not be null");
        Assert.notNull(submittedJob.getStatus(), "Job status must not be null");
        submitJobRepository.save(submittedJob);
        Long currentUserId = getCurrentUserId();
		PrintJob printJob = new PrintJob();
		printJob.setFileName(fileName); // Set file name
		printJob.setFileType(fileType); // Set file type
		printJob.setFileSize(file.getSize()); // Set file size
		printJob.setUser(user); // Set the User entity
		printJob.setUserName(user.getUsername()); // Populate the userName field
		printJob.setUploadTimestamp(LocalDateTime.now()); // Timestamp for upload
		printJob.setFileData(fileData); // Set the file data
		printJob.setDescription(description); // Set description
		printJob.setPagesPrinted(pagesPrinted); // Set pages printed
		//printJob.setCost(cost); // Set cost
		printJob.setStatus(PrintJobStatus.SUBMITTED); // Set job status as SUBMITTED
		printJob.setSubmittedAt(LocalDateTime.now()); // Set submission timestamp
		printJobRepository.save(printJob);
		// Log job submission

		// Step 8: Log the job submission action
		logJobAction(printJob.getId(), // Job ID
				PrintJobStatus.SUBMITTED, // Previous status (if any)
				PrintJobStatus.SUBMITTED, // Updated status (after file upload)
				currentUserId, // Current user ID (assuming a method to get it)
				"Job submitted with file upload", // Log message
				Optional.of(printJob.getUserName()), // User name (optional)
				Optional.empty(), // No position info (optional)
				Optional.empty(), // No position info (optional)
				"file_upload", // Action type (e.g., file upload)
				Optional.of(fileName), // File name (passed as Optional)
				Optional.of(file.getSize()) // File size (passed as Optional)
		);


    }

	// Method to validate file type
	private void validateFileType(String fileType) {
		List<String> allowedFileTypes = Arrays.asList("application/pdf", "application/msword",
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
				"application/vnd.ms-powerpoint",
				"application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain", "image/jpeg",
				"image/png", "image/tiff", "image/bmp", "application/vnd.ms-excel",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv");

		if (!allowedFileTypes.contains(fileType)) {
			throw new IllegalArgumentException("File type '" + fileType + "' is not supported.");
		}
	}

	public Resource downloadFile(Long id) {
		// Step 1: Retrieve the print job based on the provided ID
		PrintJob printJob = findPrintJobById(id); // Assuming this method exists and finds the PrintJob by ID
		if (printJob == null) {
			throw new ResourceNotFoundException("PrintJob", "id", id.toString());
		}

		// Step 2: Retrieve the current user and their username
		Long currentUserId = getCurrentUserId(); // Assuming a method to fetch the current logged-in user ID
		User currentUser = userrepository.findById(currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId.toString()));
		String userName = currentUser.getUsername(); // Get the username for logging

		// Step 3: Log the download action for auditing purposes
		logJobAction(printJob.getId(), printJob.getStatus(), printJob.getStatus(), currentUserId,
				"File downloaded by user", Optional.of(userName), Optional.empty(), Optional.empty(), "file_download",
				Optional.of(printJob.getFileName()), Optional.of((long) printJob.getFileData().length));

		// Step 4: Return the file as a ByteArrayResource for download
		return new ByteArrayResource(printJob.getFileData()); // Assuming 'getFileData()' returns the byte data of the
																// file
	}

	// Find PrintJob by ID
	public PrintJob findPrintJobById(Long id) {
		return printJobRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("PrintJob not found with id: " + id));
	}
/*
	public void updateJobStatus(Long jobId, PrintJobStatus status, String comments) {
		// Retrieve the job with authorization check (only job owner or admin can
		// access)
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
		logJobAction(jobId, previousStatus, status, currentUserId, comments, Optional.of(printJob.getUserName()),
				Optional.empty(), Optional.empty(), "status_update", Optional.empty(), Optional.empty());

		// Check if the job status is FAILED or PAUSED and trigger notifications
		if (status == PrintJobStatus.FAILED || status == PrintJobStatus.PAUSED) {
			String message = "Alert: Job ID " + jobId + " has been " + status.toString().toLowerCase();
			emailNotificationService.sendEmailNotification("Job Status Alert", message);
			pushNotificationService.sendPushNotification("Job Status Alert", message);
			logNotification("sanjanajose97@gmail.com", message);
		}
	}*/
	
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
	    validateStatusTransition(previousStatus, status);
 
	    // Update the job's status and corresponding timestamp
	    updateJobTimestamps(printJob, status);
 
	    // Save the updated print job
	    savePrintJob(printJob);
 
	    // Log job action with updated status and user info
	    logJobAction(jobId, previousStatus, status, getCurrentUserId(), comments,
	            Optional.ofNullable(printJob.getUser().getUsername()), Optional.empty(),
	            Optional.empty(), "status_update", Optional.empty(), Optional.empty());
 
	    // Send notifications to job owner and admins
	    sendStatusChangeNotifications(jobId, status, printJob);
	    
	    
	    if (status == PrintJobStatus.QUEUED) {
            queueManagementService.addJobToQueue(printJob);	    }
	}
	
	
	public void addJobToQueue(PrintJob printJob) {
	    // Validate the PrintJob object to ensure required fields are not null
	    if (printJob == null || printJob.getId() == null || printJob.getFileName() == null ||
	        printJob.getUser() == null || printJob.getPrinter() == null || printJob.getPriority() == null ||
	        printJob.getStatus() == null) {
	        throw new IllegalArgumentException("Invalid PrintJob data. Ensure all required fields are set.");
	    }

	    // Map values from PrintJob to QueuedJob entity
	    QueuedJob queuedJob = new QueuedJob();
	    queuedJob.setJobId(printJob.getId()); // Ensure getId is the correct method for PrintJob
	    queuedJob.setDocumentName(printJob.getFileName());
	    queuedJob.setUserId(printJob.getUser().getId());
	    queuedJob.setPrinterId(printJob.getPrinter().getId());
	    queuedJob.setPagesPrinted(printJob.getPagesPrinted());
	    queuedJob.setNumCopies(printJob.getNumCopies());
	    queuedJob.setSubmissionTimestamp(LocalDateTime.now()); // Set the current timestamp
	    queuedJob.setJobPriority(printJob.getPriority()); // Priority from PrintJob
	    queuedJob.setStatus(PrintJobStatus.QUEUED); // Force status to QUEUED for queued jobs

	    // Save to the queued_jobs table
	    queuedJobRepository.save(queuedJob); // Ensure queuedJobRepository is properly injected and used
	}


	
	
	private boolean hasRole(String role) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null) {
	        throw new IllegalStateException("User is not authenticated.");
	    }
	    return auth.getAuthorities().stream()
	            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
	}


	private void validateStatusTransition(PrintJobStatus previousStatus, PrintJobStatus newStatus) {
	    switch (newStatus) {
	        case PAUSED:
	            if (previousStatus != PrintJobStatus.READY || previousStatus != PrintJobStatus.QUEUED) {
	                throw new IllegalStateException("Only jobs in PRINTING or QUEUED status can be paused.");
	            }
	            break;
	        case READY:
	            if (previousStatus != PrintJobStatus.PAUSED || previousStatus != PrintJobStatus.FAILED || previousStatus != PrintJobStatus.SUBMITTED ) {
	                throw new IllegalStateException("Only paused jobs , failed jobs AND Submitted jobs can be marked as READY.");
	            }
	            break;
	        case PRINTING:
	            if (previousStatus != PrintJobStatus.READY || previousStatus != PrintJobStatus.QUEUED) {
	                throw new IllegalStateException("Jobs must be in READY or QUEUED status to start printing.");
	            }
	            break;
	        case COMPLETED:
	            if (previousStatus != PrintJobStatus.PRINTING) {
	                throw new IllegalStateException("Only jobs in PRINTING status can be marked as COMPLETED.");
	            }
	            break;
	        default:
	            // Add other transitions if needed
	            break;
	    }
	}

	private void updateJobTimestamps(PrintJob printJob, PrintJobStatus status) {
	    LocalDateTime now = LocalDateTime.now();
	    printJob.setStatus(status);

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
	        default:
	            // Handle other statuses if necessary
	            break;
	    }
	}

	private void sendStatusChangeNotifications(Long jobId, PrintJobStatus status, PrintJob printJob) {
	    String message = "Job ID " + jobId + " status has been updated to " + status;

	    // Notify job owner
	    if (printJob.getUser() == null || printJob.getUser().getId() == null) {
	        throw new IllegalStateException("Job owner ID not found for Job ID: " + jobId);
	    }
	    Long userId = printJob.getUser().getId();
	    sendNotification("Job Status Updated", message, userId);

	    // Notify admins
	    List<Long> adminIds = getAdminIds(); // Ensure this method retrieves admin IDs as Long
	    if (adminIds != null && !adminIds.isEmpty()) {
	        adminIds.forEach(adminId -> sendNotification("Job Status Updated", message, adminId));
	    }

	    // Log notifications
	    logNotification(userId.toString(), message);
	    if (adminIds != null) {
	        adminIds.forEach(adminId -> logNotification(adminId.toString(), message));
	    }
	}
	
	
	private List<Long> getAdminIds() {
	    return userService.getUsersByRole("ROLE_ADMIN") // Fetch users with the "ROLE_ADMIN" role
	        .stream()
	        .map(User::getId) // Replace `User` with your actual user entity class
	        .collect(Collectors.toList()); // Collect the IDs into a list
	}





	private void sendNotification(String subject, String message, Long recipientId) {
	    // Retrieve notification preferences for the recipient
	    UserNotificationPreferences preferences = userNotificationPreferencesService
	            .findByUserId(recipientId) // Assuming this method finds preferences by userId (Long)
	            .orElseThrow(() -> new ResourceNotFoundException("User notification preferences not found for user ID: " + recipientId));

	    // Send email notification if the user prefers email
	    if (preferences.isPreferEmail()) {
	        emailNotificationService.sendEmailNotification(preferences.getUser().getEmail(), subject, message);
	    }

	    // Send push notification if the user prefers in-app notifications
	    if (preferences.isPreferInApp()) {
	        pushNotificationService.sendPushNotification(recipientId, subject, message);
	    }
	}
	


	private boolean isStatusAllowedForUser(PrintJobStatus status) {
		    return status == PrintJobStatus.FAILED || 
		           status == PrintJobStatus.DELETED||
		           status  == PrintJobStatus.PAUSED;
		}


	
	private List<String> getAdminEmails() {
	    return userService.getUsersByRole("ROLE_ADMIN") // Replace with your method to fetch admin users
	        .stream()
	        .map(User::getEmail) // Replace `User` with your actual user entity class
	        .collect(Collectors.toList());
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
		logJobAction(jobId, job.getStatus(), job.getStatus(), currentUserId, "Job priority updated to " + priority,
				Optional.of(job.getUserName()), // User who owns the print job
				Optional.empty(), Optional.empty(), "priority_update", // Action type
				Optional.of(priority.name()), // Updated priority
				Optional.empty()); // No file size or name needed
	}
/*
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
				Optional.of(printJob.getUserName()), Optional.empty(), Optional.empty(), "job_cancellation",
				Optional.empty(), Optional.empty());
	}
*/
	
	public void cancelJob(Long jobId) {
	    // Retrieve the job with authorization check
	    PrintJob printJob = findJobIfAuthorized(jobId);

	    // Ensure only jobs that are not COMPLETED or DELETED can be canceled
	    if (printJob.getStatus() == PrintJobStatus.COMPLETED || printJob.getStatus() == PrintJobStatus.DELETED) {
	        throw new IllegalStateException("Cannot cancel a job that is already completed or deleted.");
	    }

	    // Capture the previous status for logging
	    PrintJobStatus previousStatus = printJob.getStatus();

	    // Update the job's status to FAILED
	    printJob.setStatus(PrintJobStatus.FAILED);
	    printJob.setFailedAt(LocalDateTime.now());

	    // Save the updated job
	    savePrintJob(printJob);

	    // Log the cancellation action
	    Long currentUserId = getCurrentUserId();
	    logJobAction(jobId, previousStatus, PrintJobStatus.FAILED, currentUserId, "Job canceled by user",
	                 Optional.ofNullable(printJob.getUserName()), Optional.empty(), Optional.empty(), "cancel_job",
	                 Optional.empty(), Optional.empty());

	    // Save the failed job into FailedJob table
	    FailedJob failedJob = new FailedJob();
	    failedJob.setFailedAt(LocalDateTime.now());
	    failedJob.setErrorDetails("Job canceled by user."); // You can replace this with actual error details
	    failedJob.setFailedBy(currentUserId != null ? currentUserId.toString() : "Unknown User");
	    failedJob.setRetryCount(0); // Initial retry count
	    failedJob.setPrintJob(printJob);
	    failedJob.setPrinter(printJob.getPrinter()); // If PrintJob has a printer reference
	    
	    
	    
	    saveFailedJob(failedJob); // Method to save failed job in FailedJob table
	}

	
	
	public void notifyJobCancellation(Long jobId, Long cancelledByUserId) {
	    PrintJob job = printJobRepository.findById(jobId)
	            .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "ID", jobId));

	    User jobOwner = job.getUser();
	    if (jobOwner == null) {
	        throw new RuntimeException("Job owner not found for the print job");
	    }

	    User cancelledByUser = userrepository.findById(cancelledByUserId)
	            .orElseThrow(() -> new ResourceNotFoundException("User", "ID", cancelledByUserId));

	    String messageForOwner = String.format(
	        "Dear %s, your print job with ID %d (%s) has been canceled by %s.",
	        jobOwner.getUsername(), job.getId(), job.getDescription(), cancelledByUser.getUsername()
	    );

	    String messageForAdmin = String.format(
	        "Print job with ID %d (%s), owned by %s, was canceled by %s.",
	        job.getId(), job.getDescription(), jobOwner.getUsername(), cancelledByUser.getUsername()
	    );

	    // Notify job owner
	    emailNotificationService.sendEmailNotification(
	        jobOwner.getEmail(), "Notification: Print Job Canceled", messageForOwner
	    );
	    pushNotificationService.sendPushNotification(jobOwner.getId(), "Print Job Canceled", messageForOwner);

	    // Notify all admins
	    List<User> admins = userrepository.findAllAdmins(RoleName.ROLE_ADMIN);

	    admins.forEach(admin -> {
	        emailNotificationService.sendEmailNotification(
	            admin.getEmail(), "Notification: Print Job Canceled", messageForAdmin
	        );
	        pushNotificationService.sendPushNotification(admin.getId(), "Print Job Canceled", messageForAdmin);
	    });
	}

	
	private void saveFailedJob(FailedJob failedJob) {
	    failedJobRepository.save(failedJob);
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
			logJobAction(jobId, // Job ID
					previousStatus, // Previous status (before update)
					PrintJobStatus.PAUSED, // Updated status (PAUSED in this case)
					currentUserId, // User ID performing the action
					"Job paused by user", // Action description
					Optional.ofNullable(job.getUserName()), // User's username (wrapped in Optional)
					Optional.empty(), // No previous position (if not needed, use Optional.empty())
					Optional.empty(), // No new position (if not needed, use Optional.empty())
					"pause", // Action type (this can be "pause" for the pausing action)
					Optional.empty(), // No file name involved
					Optional.empty() // No file size involved
			);

		} else {
			throw new IllegalStateException("Job can only be paused if it is in PRINTING or QUEUED status.");
		}
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
		logJobAction(jobId, // Job ID
				previousStatus, // Previous status (before update)
				PrintJobStatus.READY, // Updated status (READY in this case)
				currentUserId, // User ID performing the action
				"Job resumed by user", // Action description
				Optional.empty(), // No userName (if not available)
				Optional.empty(), // No previous position (if not relevant)
				Optional.empty(), // No new position (if not relevant)
				"resume", // Action type (this can be "resume" for the resuming action)
				Optional.empty(), // No file name involved
				Optional.empty() // No file size involved
		);
	}

	/**
	 * public void logJobAction(Long jobId, PrintJobStatus previousStatus,
	 * PrintJobStatus updatedStatus, Long userId, String comments, Optional<String>
	 * userName) { JobHistory history = new JobHistory();
	 * history.setPrintJobId(jobId); history.setPreviousStatus(previousStatus);
	 * history.setUpdatedStatus(updatedStatus); history.setUserId(userId);
	 * history.setTimestamp(LocalDateTime.now());
	 * userName.ifPresent(history::setUserName); history.setComments(comments);
	 * 
	 * jobHistoryRepository.save(history); // Save the log entry }
	 **/

	public void logJobAction(Long jobId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus, Long userId,
			String comments, Optional<String> userName, Optional<Integer> previousPosition,
			Optional<Integer> newPosition, String actionType, Optional<String> fileName, Optional<Long> fileSize) {

		if (jobId == null || previousStatus == null || updatedStatus == null || userId == null || comments == null) {
			throw new IllegalArgumentException(
					"Job ID, previous status, updated status, user ID, and comments cannot be null");
		}

		// Create the JobHistory entry
		JobHistory history = new JobHistory();
		history.setPrintJobId(jobId);
		history.setPreviousStatus(previousStatus);
		history.setUpdatedStatus(updatedStatus);
		history.setUserId(userId);
		history.setTimestamp(LocalDateTime.now());
		userName.ifPresent(history::setUserName); // Optional userName
		history.setComments(comments);
		history.setActionType("SAVE");

		// Set file-related fields if they are present
		fileName.ifPresent(history::setUserName); // Set file name if present
		fileSize.ifPresent(size -> history.setFileSize(size));
		// Set file size if present

		// Set position changes if available
		previousPosition.ifPresent(history::setPreviousPosition);
		newPosition.ifPresent(history::setNewPosition);

		try {
			jobHistoryRepository.save(history); // Save the log entry
		} catch (Exception e) {
			System.err.println("Error logging job action: " + e.getMessage());
		}
	}

	// Method to log status change
	public void logStatusChange(Long jobId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus,
			String comments) {
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
	public List<JobHistory> getSortedPrintJobs(String sortBy, boolean sortByTime, String sortOrder, String userName) {
		List<JobHistory> jobs = jobHistoryRepository.findAll(); // Fetch all records

		if (sortByTime) {
			jobs.sort((job1, job2) -> {
				JobHistory latestHistory1 = jobHistoryRepository
						.findTopByPrintJobIdOrderByTimestampDesc(job1.getPrintJobId());
				JobHistory latestHistory2 = jobHistoryRepository
						.findTopByPrintJobIdOrderByTimestampDesc(job2.getPrintJobId());

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
		System.out.println("jobId::in findJobIfAuthorized"+jobId);
	/*PrintJob job = printJobRepository.findById(jobId)

	public PrintJob findJobIfAuthorized(Long jobId) {
		PrintJob job = printJobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found"));
	
	*/
    PrintJob job = printJobRepository.findJobById(jobId)
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
	
	
	


	/*private Long getCurrentUserId() {
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
	}*/
	
	private Long getCurrentUserId() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null) {
	        logger.error("No authentication context available.");
	        throw new IllegalStateException("User is not authenticated.");
	    }

	    if (!(auth.getPrincipal() instanceof CustomUserDetails)) {
	        logger.error("Invalid principal type: {}", auth.getPrincipal());
	        throw new IllegalStateException("Invalid principal type.");
	    }

	    CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
	    logger.info("Authenticated user ID: {}", userDetails.getId());
	    return userDetails.getId();
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

		// Retrieve the PrintJob using the provided fileName from the previously
		// uploaded jobs
		// Ensure that the same file name is not re-used by different jobs
		PrintJob existingJob = printJobRepository.findByFileName(jobRequest.getFileName())
				.orElseThrow(() -> new ResourceNotFoundException("PrintJob", "fileName", jobRequest.getFileName()));

		// Create a new PrintJob object to add to the queue
		PrintJob job = new PrintJob();
		job.setStatus(PrintJobStatus.READY); // Set the status to 'READY'
		job.setUser(user); // Associate the job with the authenticated user
		job.setUserName(user.getUsername()); // Explicitly setting the username
		job.setDescription(jobRequest.getDescription()); // Set the description from jobRequest
		job.setQueuePosition(assignNextQueuePosition()); // Get the next queue position from the service
		job.setFileName(jobRequest.getFileName()); // Reusing the uploaded file name
		job.setPagesPrinted(jobRequest.getPages()); // Set pages from jobRequest
		job.setFileData(existingJob.getFileData()); // Re-use the existing file data from previously uploaded job
job.setFileType(existingJob.getFileType());
job.setUploadTimestamp(LocalDateTime.now());
		// Save the new job to the database
		printJobRepository.save(job);

		// Log the action with job ID, previous status, and new status
		logJobAction(job.getId(), // Job ID (current job ID)
				existingJob.getStatus(), // Previous status of the job (status before update)
				PrintJobStatus.READY, // New status of the job (Ready status as it's being added to the Ready list)
				user.getId(), // User ID (ID of the user performing the action)
				"Job added to queue", // Action description (provides context for the action)
				Optional.of(user.getUsername()), // User name for logging (using Optional to handle null values)
				Optional.empty(), // Optional field for previous position (if needed for logging)
				Optional.empty(), // Optional field for new position (if needed for logging)
				"ready_action", // Action type description (custom action type such as "queue_action")
				Optional.empty(), // Optional field for file name (if you want to log the file name as well)
				Optional.empty() // Optional field for file size (if you want to log the file size)
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
				return 1; // Highest priority
			} else if (role.getRole() == RoleName.ROLE_USER) {
				return 3; // Lowest priority
			}
		}
		return 2; // Default priority if no matching role is found
	}

	public void logQueuePositionChange(Long jobId, int previousPosition, int newPosition, Long userId,
			String actionDescription) {
		
		PrintJob job = printJobRepository.findById(jobId)
		        .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "jobId", jobId));

		JobHistory history = new JobHistory();
		history.setPrintJobId(jobId);
		history.setPreviousPosition(previousPosition); // Add this field in JobHistory if needed
		history.setNewPosition(newPosition); // Add this field in JobHistory if needed
		history.setUserId(userId);
		history.setComments(actionDescription);
		history.setTimestamp(LocalDateTime.now());
		history.setUpdatedStatus(job.getStatus()); // Replace SOME_DEFAULT_STATUS with an appropriate enum value
		history.setActionType("Position Updated");
		jobHistoryRepository.save(history);
	}

	@Transactional
	public void reorderJob(Long jobId, int newPosition) {
		// Retrieve the job with authorization check
		PrintJob job = findJobIfAuthorized(jobId);

		// Log current and new position before comparison
		logger.debug("Reordering Job - Current Position: {}, Requested Position: {}", job.getQueuePosition(),
				newPosition);

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
	
	public void notifyJobReorder(Long jobId, Long reorderedByUserId, int newPosition) {
	    PrintJob job = printJobRepository.findById(jobId)
	            .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "ID", jobId));

	    User jobOwner = job.getUser();
	    if (jobOwner == null) {
	        throw new RuntimeException("Job owner not found for the print job");
	    }

	    User reorderedByUser = userrepository.findById(reorderedByUserId)
	            .orElseThrow(() -> new ResourceNotFoundException("User", "ID", reorderedByUserId));

	    String messageForOwner = String.format(
	        "Dear %s, your print job with ID %d (%s) has been reordered by %s to position %d.",
	        jobOwner.getUsername(), job.getId(), job.getDescription(), reorderedByUser.getUsername(), newPosition
	    );

	    String messageForAdmin = String.format(
	        "Print job with ID %d (%s), owned by %s, was reordered by %s to position %d.",
	        job.getId(), job.getDescription(), jobOwner.getUsername(), reorderedByUser.getUsername(), newPosition
	    );

	    // Notify job owner
	    emailNotificationService.sendEmailNotification(
	        jobOwner.getEmail(), "Notification: Print Job Reordered", messageForOwner
	    );
	    pushNotificationService.sendPushNotification(jobOwner.getId(), "Print Job Reordered", messageForOwner);

	    // Notify all admins
	    List<User> admins = userrepository.findAllAdmins(RoleName.ROLE_ADMIN);

	    admins.forEach(admin -> {
	        emailNotificationService.sendEmailNotification(
	            admin.getEmail(), "Notification: Print Job Reordered", messageForAdmin
	        );
	        pushNotificationService.sendPushNotification(admin.getId(), "Print Job Reordered", messageForAdmin);
	    });
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
			List<PrintJob> jobsToShiftDown = printJobRepository.findByQueuePositionBetween(newPosition,
					oldPosition - 1);
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
		logJobAction(jobId, // Job ID
				previousStatus, // Previous job status before deletion
				PrintJobStatus.DELETED, // New status after deletion (DELETED)
				currentUserId, // User ID of the user performing the action
				"Job permanently removed from the database", // Description of the action
				Optional.empty(), // Optional: No need to log user name here (Empty Optional if no user name to
									// log)
				Optional.empty(), // Optional: No additional integer fields for this action
				Optional.empty(), // Optional: No additional integer fields for this action
				"delete_job", // Action type for this event (describes the type of operation performed)
				Optional.empty(), // Optional: No file name to log for job deletion
				Optional.empty() // Optional: No file size to log for job deletion
		);

	}

	// Method to save the print job
	public void save(PrintJob printJob) {
		printJobRepository.save(printJob); // Saves the updated print job to the database
	}

	private PrintJob getJob(Long jobId) {
		return printJobRepository.findById(jobId)
				.orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
	}

	
    

    // Regular User: Get jobs for the user
    public List<PrintJobDTO> getJobsForUser(Long userId) {
        List<PrintJob> jobs = printJobRepository.findByUserIdOrderByStatusAscPriorityAsc(userId);
        return jobs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Convert entity to DTO
    private PrintJobDTO convertToDTO(PrintJob job) {
        PrintJobDTO dto = new PrintJobDTO();
        BeanUtils.copyProperties(job, dto);
        return dto;
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

	public List<PrintJobDTO> getAllJobs() {
		List<PrintJobDTO> printJobList = new ArrayList<PrintJobDTO>();
		List<PrintJob> jobs = printJobRepository.findAllByOrderByStatusAscPriorityAsc();
		for (PrintJob printJob : jobs) {
			PrintJobDTO printObj = new PrintJobDTO();
			BeanUtils.copyProperties(printJob, printObj);
			printObj.setUserId(printJob.getUserId());
			printObj.setPriority(printJob.getPriority());
			printJobList.add(printObj);
		}

		return printJobList;
	}
	
	/**
     * Check if the authenticated user has access to a specific job
     */
    public boolean hasAccess(Long jobId, User authenticatedUser) {
        PrintJob job = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
        return authenticatedUser.getRoles().contains("ROLE_ADMIN") || job.getUser().getId().equals(authenticatedUser.getId());
    }

	public List<PrintJobDTO> getPrintJobsByStatus(PrintJobStatus queued) {
		List<PrintJob> queuedJobs = printJobRepository.findByStatusOrderByPriorityAsc(PrintJobStatus.QUEUED);
		List<PrintJobDTO> printList = new ArrayList();
		for (PrintJob printJob : queuedJobs) {
			PrintJobDTO printObj = new PrintJobDTO();
			BeanUtils.copyProperties(printJob, printObj);
			printObj.setUserId(printJob.getUserId());
			printObj.setPriority(printJob.getPriority());
			printList.add(printObj);
		}
		return printList;
	}

	public PrintHistoryMap getPrintHistoryById(Long jobId) {
		PrintJob job = findPrintJobById(jobId);
		List<JobHistory> history = jobHistoryRepository.findByPrintJobIdOrderByTimestampAsc(jobId);

		PrintJobDTO printObj = new PrintJobDTO();
		BeanUtils.copyProperties(job, printObj);
		printObj.setUserId(job.getUserId());
		printObj.setPriority(job.getPriority());

		List<PrintHistoryDTO> printHistoryDTOList = new ArrayList<PrintHistoryDTO>();
		for (JobHistory printHistory : history) {
			PrintHistoryDTO printHistoryDTO = new PrintHistoryDTO();
			BeanUtils.copyProperties(printHistory, printHistoryDTO);
			printHistoryDTOList.add(printHistoryDTO);
		}
		PrintHistoryMap printHistoryMap = new PrintHistoryMap(printObj, printHistoryDTOList);

		return printHistoryMap;
	}
	

    public void handlePrintJobFailure(PrintJob printJob, String errorDetails) {
        // Log the failure
    	failedJobService.logFailedJob(printJob, errorDetails, "System");

        // Update the print job status
        printJob.setStatus(PrintJobStatus.FAILED);
        // Save the print job (use your existing repository)
    }
    
    
    public void retryPrintJob(PrintJob printJob) {
        try {
            // Add logic to retry the print job
            printJob.setStatus(PrintJobStatus.READY); // Update status
            printJobRepository.save(printJob); // Save the updated print job
            System.out.println("Print job retried successfully: " + printJob.getFileName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retry print job: " + e.getMessage());
        }
    }
/*
    public void processPrintJob(PrintJob printJob) {
        // Validate the print job
        if (printJob == null || printJob.getFileData() == null || printJob.getPrinterId() == null) {
            throw new IllegalArgumentException("Invalid print job data.");
        }

        try {
            // Simulate communication with the printer
           Printer printer = printerRepository.findById(printJob.getPrinterId())
                    .orElseThrow(() -> new RuntimeException("Printer not found"));

            // Send the job to the printer (placeholder for actual printing logic)
           boolean success = printerService.sendPrintJob(printer, printJob);

            if (success) {
                // Update job status to COMPLETED
                printJob.setStatus(PrintJobStatus.COMPLETED);
                printJob.setCompletedAt(LocalDateTime.now());
                printJobRepository.save(printJob);
            } else {
                // Update job status to FAILED
                throw new RuntimeException("Print job failed at printer level.");
            }
        } catch (Exception e) {
            // Handle failure
            printJob.setStatus(PrintJobStatus.FAILED);
            printJob.setFailedAt(LocalDateTime.now());
            printJobRepository.save(printJob);
            throw new RuntimeException("Failed to process print job: " + e.getMessage(), e);
        }
    }*/
    
    
    public void processPrintJob(FailedJob failedJob) {
        if (failedJob == null || failedJob.getPrintJob() == null) {
            throw new IllegalArgumentException("Invalid failed job data.");
        }

        PrintJob failedPrintJob = failedJob.getPrintJob();

        // Prepare a PrintJobRequest object from the failed PrintJob
        PrintJobRequest jobRequest = new PrintJobRequest();
        jobRequest.setFileName(failedPrintJob.getFileName());
        jobRequest.setDescription(failedPrintJob.getDescription());
        jobRequest.setPages(failedPrintJob.getPagesPrinted());
        //jobRequest.setCost(failedPrintJob.getCost());

        try {
            // Call addJob to retry the print job
            addJob(jobRequest);

            // If successful, mark the failed job as resolved
            failedJobRepository.delete(failedJob);
        } catch (Exception e) {
            // Handle failure to re-add job
            throw new RuntimeException("Failed to retry print job: " + e.getMessage(), e);
        }
    }


	public List<ReadyJobResponse> getReadyJobs() {
	    // Fetch the list of print jobs with status READY
	    List<PrintJob> printJobs = printJobRepository.findByStatus(PrintJobStatus.READY);

	    // Map each PrintJob entity to a PrintJobResponse DTO
	    return printJobs.stream().map(job -> {
	    	ReadyJobResponse response = new ReadyJobResponse();
	        response.setId(job.getId());
	        response.setFileName(job.getFileName());
	        response.setEstimatedWaitTime(calculateEstimatedWaitTime(job)); // Optional: Add logic for wait time

	        // Add logic to handle User entity gracefully
	        if (job.getUser() != null && job.getUser().getId() != null && job.getUser().getId() > 0) {
	            response.setUserName(job.getUser().getUsername());
	        } else {
	            response.setUserName(null); // Handle missing or invalid User
	        }

	        return response; // Return the transformed DTO
	    }).collect(Collectors.toList()); // Collect into a list
	}

	
	private int calculateEstimatedWaitTime(PrintJob job) {
	    int jobsAhead = printJobRepository.countByStatusAndIdLessThan(PrintJobStatus.READY, job.getId());
	    return jobsAhead * 5; // Each job takes 5 minutes
	}
/*
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
	    
/*
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


	
*/


	public void markAsFavorite(Long jobId, String username) {
	    PrintJob job = printJobRepository.findById(jobId)
	        .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "jobId", jobId));

	    // Validate ownership or admin role
	    if (!job.getUser().getEmail().equals(username) && !isAdmin()) {
	        throw new AccessDeniedException("You are not authorized to mark this job as favorite.");
	    }

	    if (job.isFavorite()) {
	        logger.info("Job {} is already marked as favorite by user {}", jobId, username);
	        return;
	    }

	    job.setFavorite(true);
	    printJobRepository.save(job);

	    logger.info("Job {} marked as favorite by user {}", jobId, username);
	}


	private boolean isAdmin() {
	    return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
	        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
	}

	
	public void removeFromFavorite(Long jobId, String userId) {
	    PrintJob job = printJobRepository.findById(jobId)
	        .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "jobId", jobId));

	    if (!job.getUser().getId().toString().equals(userId)) {
	    	throw new AccessDeniedException("You are not authorized to mark this job as favorite.");

	    }

	    job.setFavorite(false);
	    printJobRepository.save(job);
	}

	
	public Boolean isOwner(Long jobId, String username) {
		Long isValid = printJobRepository.existsByIdAndUser_Username(jobId, username);
		if (isValid == 1) {
			return true;
		}
	    return false;
	}

	
	@Transactional
	public List<PrintJob> getAllFavoriteJobs() {
	    List<PrintJob> jobs = printJobRepository.findAllFavoritesWithUsers();
	    jobs.forEach(job -> Hibernate.initialize(job.getUser()));
	    return jobs;
	}

	@Transactional
	public List<PrintJob> getFavoriteJobs(String userId) {
	    List<PrintJob> jobs = printJobRepository.findFavoritesByUser(Long.valueOf(userId));
	    jobs.forEach(job -> Hibernate.initialize(job.getUser()));
	    return jobs;
	}





}

