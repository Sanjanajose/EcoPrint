package com.ecoprint.printmanagement.controller;

import java.io.IOException;
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

import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.JobStatusMessage;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.service.PrintJobService;

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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update print job status", description = "Allows an admin to update the status of a print job.")
    public ResponseEntity<String> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam("status") PrintJobStatus status) {
        PrintJob job = printJobService.findPrintJobById(jobId);
        if (job == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Print job not found");
        }

        job.setStatus(status);
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case QUEUED -> job.setQueuedAt(now);
            case PAUSED -> job.setPausedAt(now);
            case READY -> job.setReadyAt(now);
            case PRINTING -> job.setPrintingAt(now);
            case COMPLETED -> job.setCompletedAt(now);
            case FAILED -> job.setFailedAt(now);
            case DELETED -> job.setDeletedAt(now);
            case FAVORITE -> job.setFavoriteAt(now);
        }

        printJobService.savePrintJob(job);
        printJobService.logStatusChange(jobId, status, "Status updated to " + status.name());

        messagingTemplate.convertAndSend("/topic/status", new JobStatusMessage(jobId, status, "Status updated"));
        return ResponseEntity.ok("Print job status updated to " + status);
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
        @Operation(summary = "Get Dashboard Print Jobs",
        description = "Retrieve a list of print jobs with optional filters and sorting options for the dashboard.")
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
        @Operation(summary = "Get Dashboard Print Jobs",
        description = "Retrieve a list of print jobs with optional filters and sorting options for the dashboard.")
        public ResponseEntity<List<JobHistory>> getDashboardDataBySort(
                @RequestParam(defaultValue = "createdAt") String sortBy,
        	    @RequestParam(defaultValue = "false") boolean sortByTime,
        	    @RequestParam(required = false) String sortOrder) {    	       	        	       	
            //String sortBy, boolean sortByTime, String sortOrder
            List<JobHistory> jobs = printJobService.getSortedPrintJobs(sortBy,sortByTime,sortOrder);
            return ResponseEntity.ok(jobs);
        }




}
