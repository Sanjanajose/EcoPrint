package com.ecoprint.printmanagement.controller;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobRequest;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;

import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.JobStatusMessage;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.service.NotificationService;
import com.ecoprint.printmanagement.service.PrintJobService;
import org.springframework.security.core.Authentication;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;

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

    @GetMapping("/history")
    public List<JobHistory> getJobHistory(@RequestParam Long jobId) {
        return jobHistoryRepository.findByPrintJobIdOrderByTimestampAsc(jobId);
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
            double cost = pagesPrinted * COST_PER_PAGE;
            printJobService.uploadFile(file, userName, description, pagesPrinted, cost);
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

    @PutMapping("/{jobId}/status")
    @Operation(summary = "Update print job status", description = "Allows authorized users to update the status of a print job.")
    public ResponseEntity<String> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam("status") PrintJobStatus status) {

        // Delegate the job status update to the service method, including logging and notifications
        try {
            printJobService.updateJobStatus(jobId, status, "Status updated to " + status.name());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Print job not found");
        }

        // Return success response if everything was processed correctly
        return ResponseEntity.ok("Print job status updated to " + status);
    }
    
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> removeJob(@PathVariable Long jobId) {
        printJobService.removeJob(jobId);
        return ResponseEntity.ok("Print job removed from queue");
    }



    @PutMapping("/{jobId}/pause")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Pause print job", description = "Allows a user or admin to pause a print job.")
    public ResponseEntity<String> pauseJob(@PathVariable Long jobId) {
        return updateJobStatus(jobId, PrintJobStatus.PAUSED);
    }

    @PutMapping("/{jobId}/cancel")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Cancel print job", description = "Allows a user or admin to cancel a print job.")
    public ResponseEntity<String> cancelJob(@PathVariable Long jobId) {
        return updateJobStatus(jobId, PrintJobStatus.FAILED);
    }

    @PutMapping("/{jobId}/favorite")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Favorite print job", description = "Allows a user or admin to mark a print job as favorite.")
    public ResponseEntity<String> favoriteJob(@PathVariable Long jobId) {
        return updateJobStatus(jobId, PrintJobStatus.FAVORITE);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/admin/{jobId}/prioritize")
    public ResponseEntity<String> prioritizeJob(@PathVariable Long jobId) {
        try {
            printJobService.prioritizeJob(jobId);
            return ResponseEntity.ok("Job prioritized successfully");
        } catch (Exception e) {
            logger.error("Error prioritizing job", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to prioritize job");
        }
    }

    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<PrintJob>> getAllJobs() {
        List<PrintJob> jobs = printJobRepository.findAllByOrderByStatusAscPriorityAsc();
        return ResponseEntity.ok(jobs);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobDetails(@PathVariable Long jobId) {
        PrintJob job = printJobService.findPrintJobById(jobId);
        List<JobHistory> history = jobHistoryRepository.findByPrintJobIdOrderByTimestampAsc(jobId);

        Map<String, Object> response = new HashMap<>();
        response.put("job", job);
        response.put("history", history);

        return ResponseEntity.ok(response);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/queued")
    public ResponseEntity<List<PrintJob>> getQueuedJobsOrderedByPriority() {
        // Retrieve all jobs with QUEUED status, ordered by priority
        List<PrintJob> queuedJobs = printJobRepository.findByStatusOrderByPriorityAsc(PrintJobStatus.QUEUED);
        return ResponseEntity.ok(queuedJobs);
    }
    

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Export Print Job Logs to Excel", description = "Exports the print job logs to an Excel file for admins.")
    @GetMapping("/export/excel")
    public void exportLogsToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; file=print_job_logs.xlsx");
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Logs");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Job ID");
        headerRow.createCell(1).setCellValue("Status");
        headerRow.createCell(2).setCellValue("Timestamp");

        int rowIdx = 1;
        List<JobHistory> jobHistory = printJobService.getAllLogs();

        for (JobHistory log : jobHistory) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(log.getPrintJobId());
            row.createCell(1).setCellValue(log.getStatus().toString());
            row.createCell(2).setCellValue(log.getTimestamp().toString());
        }

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


    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> addJob(@RequestBody PrintJobRequest jobRequest) {
        printJobService.addJob(jobRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("Print job added to queue");
    }
    
    
    @PutMapping("/jobs/{jobId}/resume")
    public ResponseEntity<String> resumeJob(@PathVariable Long jobId) {
        printJobService.resumeJob(jobId);
        return ResponseEntity.ok("Job resumed successfully");
    }
    
    @PutMapping("/{jobId}/reorder")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> reorderJob(@PathVariable Long jobId, @RequestParam int newPosition) {
        printJobService.reorderJob(jobId, newPosition);
        return ResponseEntity.ok("Print job reordered");
    }
  


}
