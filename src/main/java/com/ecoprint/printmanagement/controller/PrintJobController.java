package com.ecoprint.printmanagement.controller;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.PrintHistoryMap;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobDTO;
import com.ecoprint.printmanagement.model.PrintJobRequest;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.Priority;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

import com.ecoprint.printmanagement.response.ReadyJobResponse;

import com.ecoprint.printmanagement.service.AuthService;

import com.ecoprint.printmanagement.service.NotificationService;
import com.ecoprint.printmanagement.service.PrintJobService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/print-jobs")
public class PrintJobController {

    private static final Logger logger = LoggerFactory.getLogger(PrintJobController.class);
    private static final double COST_PER_PAGE = 0.50;
    
    @Autowired
    private PrintJobService printJobService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private JobHistoryRepository jobHistoryRepository;
    
    @Autowired
    private PrintJobRepository printJobRepository;
    
    @Autowired
    private NotificationService emailNotificationService;

    @Autowired
    private NotificationService pushNotificationService;
    

    @Autowired
    private UserRepository userRepository;
    
    
    @Autowired
    private AuthService authService; // For getting the authenticated user


    

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "allows Admin to get the history of a particular print job")
    @GetMapping("/{jobId}/history")
    public ResponseEntity<List<JobHistory>> getJobHistory(@PathVariable Long jobId) {
        List<JobHistory> historyList = jobHistoryRepository.findByPrintJobIdOrderByTimestampAsc(jobId);
        return ResponseEntity.ok(historyList);
    }


    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Upload a print job file",
        description = "Allows a user to upload a file for a print job, calculates the cost based on pages printed, and sets the initial status to SUBMITTED."
    )
    public ResponseEntity<String> uploadPrintJob(
			@RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "userName", required = true) String userName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "pagesPrinted", required = true) int pagesPrinted) {
        try {
           
            printJobService.uploadFile(file, userName, description, pagesPrinted);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully and job submitted");
        } catch (IOException e) {
            logger.error("IOException during file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during file upload");
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error during file upload");
        }
    }
    
    
    
    

    @GetMapping("/download/{id}")
    @Operation(summary = "allows to download the submitted print jobs ")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            PrintJob printJob = printJobService.findPrintJobById(id);
            Resource resource = printJobService.downloadFile(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(printJob.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + printJob.getFileName() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

  /*  @PutMapping("/{jobId}/status")
    @Operation(summary = "Update print job status", description = "Allows authorized users to update the status of a print job.")
    public ResponseEntity<String> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam("status") PrintJobStatus status) {

        // Delegate the job status update to the service method, including logging and notifications
        try {
        	
        	System.out.println("This contains values"+jobId+"jobId:::");
            printJobService.updateJobStatus(jobId, status, "Status updated to " + status.name());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Print job not found");
        }

        // Return success response if everything was processed correctly
        return ResponseEntity.ok("Print job status updated to " + status);
    } */
    
    @PutMapping("/{jobId}/status")
    public ResponseEntity<String> updateJobStatus(@PathVariable Long jobId,
                                                  @RequestParam("status") PrintJobStatus status) {
        logger.info("Received request to update status for job ID: {}, New Status: {}", jobId, status);
 
        try {
            printJobService.updateJobStatus(jobId, status, "Status updated to " + status.name());
            logger.info("Job status updated successfully for ID: {}", jobId);
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found for job ID: {}", jobId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Print job not found");
        } catch (Exception e) {
            logger.error("Unexpected error while updating job status for ID: {}", jobId, e);
 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating job status.");
        }
 
        return ResponseEntity.ok("Print job status updated to " + status);
    }
 


    
    

    
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "allows Admin and job owners to remove the print jobs from the queue ")
    public ResponseEntity<String> removeJob(@PathVariable Long jobId) {
        printJobService.removeJob(jobId);
        return ResponseEntity.ok("Print job removed from queue");
    }



    @PutMapping("/{jobId}/pause")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Pause print job", description = "Allows a user or admin to pause a print job.")
    public ResponseEntity<String> pauseJob(@PathVariable Long jobId ) {
    	return updateJobStatus(jobId, PrintJobStatus.PAUSED);
    }

    @PutMapping("/{jobId}/cancel")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Cancel print job", description = "Allows a user or admin to cancel a print job.")
    public ResponseEntity<String> cancelJob(@PathVariable Long jobId, @RequestParam Long cancelledByUserId) {
        try {
            printJobService.cancelJob(jobId);
            printJobService.notifyJobCancellation(jobId, cancelledByUserId);
            return ResponseEntity.ok("Print job canceled successfully and marked as FAILED");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Print job not found");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during cancel operation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    
    

    
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    //@Operation(summary = "allows ADMIN to get the list of all the jobs ")
    //@GetMapping("/admin")
    //public ResponseEntity<List<PrintJobDTO>> getAllJobs() {
    //	List<PrintJobDTO> jobs = printJobService.getAllJobs();
      //  return ResponseEntity.ok(jobs);
    //}
    
   

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @Operation(summary = "Allows ADMIN to view all jobs and regular users to view their own jobs")
    public ResponseEntity<List<PrintJobDTO>> getAllJobs() {
        User currentUser = authService.getAuthenticatedUser(); // Fetch the logged-in user
        List<PrintJobDTO> jobs;

        if (currentUser.getRoles().contains("ROLE_ADMIN")) {
            // Admin gets all jobs
            jobs = printJobService.getAllJobs();
        } else {
            // Regular user gets their own jobs
            jobs = printJobService.getJobsForUser(currentUser.getId());
        }

        return ResponseEntity.ok(jobs);
    }
    /*@GetMapping
    public List<PrintJob> getAllJobs() {
        User currentUser = authService.getAuthenticatedUser();
        if (currentUser.getRoles().contains("ROLE_ADMIN")) {
            return printJobService.getAllJobs();
        } else {
            return printJobService.getJobsForUser(currentUser.getId());
        }
    }*/
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/{jobId}")
    @Operation(summary = "allows Admin to get the history of a job based on job id provided ")
    public ResponseEntity<PrintHistoryMap> getJobDetails(@PathVariable Long jobId) {
        PrintHistoryMap printHistory = printJobService.getPrintHistoryById(jobId);

        return ResponseEntity.ok(printHistory);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/queued")
    @Operation(summary = "allows Admin to get the list of all the queued jobs by priority ")
    public ResponseEntity<List<PrintJobDTO>> getQueuedJobsOrderedByPriority() {
        // Retrieve all jobs with QUEUED status, ordered by priority
        List<PrintJobDTO> queuedJobs = printJobService.getPrintJobsByStatus(PrintJobStatus.QUEUED);
        return ResponseEntity.ok(queuedJobs);
    }
    
    @PutMapping("/set-priority/{jobId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @printJobService.isOwner(#jobId, authentication.name)")
    @Operation(summary = "Set priority of the print jobs", description = "Allows job owner or admin to set the priority of a print job")
    public ResponseEntity<String> setJobPriority(
            @PathVariable Long jobId,
            @RequestBody Priority priority) {

        if (priority == null) {
            return ResponseEntity.badRequest().body("Priority cannot be null");
        }

        // Log the received priority
        System.out.println("Setting priority for jobId: " + jobId + " to " + priority);

        try {
            // Call the service method to update priority
            printJobService.setJobPriority(jobId, priority);
            return ResponseEntity.ok("Print job priority updated successfully");
        } catch (ResourceNotFoundException ex) {
            // Handle case where the job does not exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Handle validation issues
            return ResponseEntity.badRequest().body("Invalid request: " + ex.getMessage());
        } catch (Exception ex) {
            // Catch any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the priority");
        }
    }

    




    
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Export Print Job Logs to Excel", description = "Exports the print job logs to an Excel file for admins.")
    @GetMapping("/export/excel")
    public void exportLogsToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; file=print_job_logs.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Logs");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Job ID");
        headerRow.createCell(1).setCellValue("Previous Status");
        headerRow.createCell(2).setCellValue("Updated Status");
        headerRow.createCell(3).setCellValue("Timestamp");

        int rowIdx = 1;
        List<JobHistory> jobHistory = printJobService.getAllLogs();

        // Populate rows with job history data
        for (JobHistory log : jobHistory) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(log.getPrintJobId());
            row.createCell(1).setCellValue(log.getPreviousStatus() != null ? log.getPreviousStatus().toString() : "N/A");  // Safe null check
            row.createCell(2).setCellValue(log.getUpdatedStatus() != null ? log.getUpdatedStatus().toString() : "N/A");  // Safe null check
            row.createCell(3).setCellValue(log.getTimestamp() != null ? log.getTimestamp().toString() : "N/A");  // Safe null check
        }

        // Write the data to the response output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    
                          
        @GetMapping("/dashboard/filter")
        @Operation(summary = "Get Dashboard Print Jobs by Filter",
        	    description = "Retrieve a list of print jobs based on optional filters such as job status and username. Allows filtering by print job status and user who initiated the job.")
        @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
        public ResponseEntity<List<JobHistory>> getDashboardDataByFilters(
                @RequestParam(required = false) PrintJobStatus status,
                @RequestParam(required = false) String userName) {    	        	       	
            if (status != null && !EnumSet.allOf(PrintJobStatus.class).contains(status)) {
                return ResponseEntity.badRequest().body(Collections.emptyList()); // Return an empty list
            }
            List<JobHistory> jobs = printJobService.getFilteredPrintJobs(status,userName);
            return ResponseEntity.ok(jobs);
        }
    
       
        @GetMapping("/dashboard/sort")
        @Operation(summary = "Get Dashboard Print Jobs by Sorting",
        	    description = "Retrieve a sorted list of print jobs based on optional parameters such as sort field and order. Sorts print jobs for the logged-in user by a specified attribute and order.")
        @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
        public ResponseEntity<List<JobHistory>> getDashboardDataBySort(
                @RequestParam(defaultValue = "createdAt") String sortBy,
        	    @RequestParam(defaultValue = "false") boolean sortByTime,
        	    @RequestParam(required = false) String sortOrder,
        	    Authentication authentication) {    	       	        	       	
           // Get the current logged-in username
           String currentUsername = authentication.getName();
           // Fetch sorted jobs for the specific user
           List<JobHistory> jobs = printJobService.getSortedPrintJobs(sortBy, sortByTime, sortOrder, currentUsername);

            return ResponseEntity.ok(jobs);
        }

        @PostMapping("/addjob")
        @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
        @Operation(summary = "allows users to send submitted documents to print job lifecycle ")
        public ResponseEntity<String> addJob(@Valid @RequestBody PrintJobRequest jobRequest) {
            try {
                // Log the received job request for debugging
                logger.debug("Received job request: {}", jobRequest);

                // Validate file name or any other important fields
                if (jobRequest.getFileName() == null || jobRequest.getFileName().isEmpty()) {
                    logger.error("Invalid request: fileName is required");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: fileName must not be null or empty");
                }

                // Delegate the job creation to the service layer
                printJobService.addJob(jobRequest);

                // Return success response
                return ResponseEntity.status(HttpStatus.CREATED).body("Print job added to Ready");

            } catch (IllegalArgumentException e) {
                // Handle invalid input
                logger.error("Invalid input error during job addition", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                // Handle unexpected errors
                logger.error("Error during job addition", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding the print job");
            }
        }
        
      
    

    @PutMapping("/jobs/{jobId}/resume")
    @Operation(summary = "allows to resume the paused print jobs ")
    public ResponseEntity<String> resumeJob(@PathVariable Long jobId) {
        printJobService.resumeJob(jobId);  
        
        // Send notifications
        String message = "Print job with ID " + jobId + " has been canceled and marked as FAILED.";
        

        return ResponseEntity.ok("Job resumed successfully");
    }
    
    @PutMapping("/{jobId}/reorder")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "allows to reorder the print jobs ", description = "allows users to reorder the print jobs if the user is an admin or the job owner")
    public ResponseEntity<String> reorderJob(@PathVariable Long jobId, @RequestParam int newPosition, @RequestParam Long reorderedByUserId) {
        printJobService.reorderJob(jobId, newPosition);
        printJobService.notifyJobReorder(jobId, reorderedByUserId, newPosition);
        return ResponseEntity.ok("Print job reordered");
    }


   
    @GetMapping("/ready-jobs")
    @Operation(summary = "Get Ready Jobs",
               description = "Retrieve a list of jobs that are ready to print with estimated wait times.")
    public ResponseEntity<List<ReadyJobResponse>> getReadyJobs() {
        List<ReadyJobResponse> readyJobs = printJobService.getReadyJobs();
        return ResponseEntity.ok(readyJobs);
    }

 /*

    @PostMapping("/retry-failed-jobs/{jobId}")

    public ResponseEntity<String> retryFailedJobById(@PathVariable Long jobId) {

    	boolean retrySuccess = printJobService.retryFailedJobById(jobId);

    

    @PutMapping("/{jobId}/favorite")
    @PreAuthorize("hasRole('ADMIN') or @printJobService.isOwner(#jobId, authentication.name)")
    public ResponseEntity<?> markAsFavorite(@PathVariable Long jobId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        printJobService.markAsFavorite(jobId, username);
        return ResponseEntity.ok("Job marked as favorite.");
    }
  
    @PutMapping("/{jobId}/unfavorite")
    @PreAuthorize("hasRole('ADMIN') or @printJobService.isOwner(#jobId, authentication.name)")
    public ResponseEntity<?> removeFromFavorite(@PathVariable Long jobId) {
        String userId = getCurrentUserId();
        printJobService.removeFromFavorite(jobId, userId);
        return ResponseEntity.ok("Job removed from favorite.");
    }
    @GetMapping("/favorites")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<PrintJob>> getFavoriteJobs() {
        String userId = getCurrentUserId(); // Utility to fetch logged-in user ID
        List<PrintJob> favorites = hasRoleAdmin()
                ? printJobService.getAllFavoriteJobs()  // Admin sees all
                : printJobService.getFavoriteJobs(userId); // User sees their own
        return ResponseEntity.ok(favorites);
    }

    // Helper method for checking admin role
    private boolean hasRoleAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Assumes username is the user ID or can be mapped to it
    }



    if (retrySuccess) {

        return ResponseEntity.ok("Retry process triggered for job ID: " + jobId);

    } else {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job ID " + jobId + " not found or not eligible for retry.");

    }

    }
  

*/
}