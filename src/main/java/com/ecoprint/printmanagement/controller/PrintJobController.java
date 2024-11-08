package com.ecoprint.printmanagement.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.JobStatusMessage;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.service.PrintJobService;

import io.swagger.v3.oas.annotations.Operation;

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

}
