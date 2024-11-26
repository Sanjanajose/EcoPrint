package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.DeletedJob;
import com.ecoprint.printmanagement.model.DeletionAuditLog;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.service.DeletedJobService;
import com.ecoprint.printmanagement.service.ReportExportService;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import com.ecoprint.printmanagement.dto.DeletedJobResponse;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/deleted-jobs")
public class DeletedJobController {

    @Autowired
    private DeletedJobService deletedJobService;

    @Autowired
    private PrintJobRepository printJobRepository;

    @Autowired
    private UserRepository userRepository;

    
    @Autowired
    private  ReportExportService reportExportService;

    @Autowired
    public DeletedJobController(ReportExportService reportExportService) {
        this.reportExportService = reportExportService;
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) String reason,
            @RequestParam Long deletedByUserId) {

        // Fetch the user who is trying to delete the job
        User user = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", deletedByUserId));

        // Fetch the job to be deleted
        PrintJob job = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));

        // Check if the user is an admin
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRole().equals(RoleName.ROLE_ADMIN));

        if (!isAdmin) {
            // Regular users: Ensure they can only delete their own jobs
            if (!job.getUser().getId().equals(deletedByUserId)) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                        .body("Users can only delete their own jobs.");
            }

            // Ensure the job status is eligible for deletion
            if (job.getStatus() != PrintJobStatus.QUEUED &&
                    job.getStatus() != PrintJobStatus.SUBMITTED &&
                    job.getStatus() != PrintJobStatus.READY) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                        .body("Users can only delete jobs in QUEUED, SUBMITTED, or READY status.");
            }
        }

        // Admins bypass ownership and status checks
        deletedJobService.deleteJob(jobId, deletedByUserId, reason);
        return ResponseEntity.ok("Job deleted successfully.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{jobId}/restore")
    public ResponseEntity<?> restoreJob(
            @PathVariable Long jobId,
            @RequestParam Long userId) {
        deletedJobService.restoreDeletedJob(jobId, userId);
        return ResponseEntity.ok("Job restored successfully.");
    }
    
    /**
     * Get the deleted jobs report, filtered by date and user.
     * @throws AccessDeniedException 
     */
    @GetMapping("/report")
    public ResponseEntity<?> getDeletedJobsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId,
            Principal principal) throws AccessDeniedException {

        // Fetch the requesting user
        User requestingUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", principal.getName()));

        // Fetch the report
        List<DeletionAuditLog> logs = deletedJobService.getDeletedJobsReport(startDate, endDate, userId, requestingUser);

        return ResponseEntity.ok(logs);
    }

    /**
     * Export deleted jobs report as a CSV file.
     * @throws java.io.IOException 
     */
    @GetMapping("/report/export")
    public void exportDeletedJobsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId,
            Principal principal,
            HttpServletResponse response) throws IOException, java.io.IOException {

        // Fetch the requesting user
        User requestingUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", principal.getName()));

        // Fetch the report
        List<DeletionAuditLog> logs = deletedJobService.getDeletedJobsReport(startDate, endDate, userId, requestingUser);

        // Write to CSV
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=deleted_jobs_report.csv");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Job ID,Deleted By User ID,Document Name,Deletion Time,Reason For Deletion");
            for (DeletionAuditLog log : logs) {
                writer.printf("%d,%d,%s,%s,%s%n",
                        log.getJobId(),
                        log.getDeletedByUserId(),
                        log.getDocumentName(),
                        log.getDeletionTime(),
                        log.getReasonForDeletion());
            }
        }
    }
}