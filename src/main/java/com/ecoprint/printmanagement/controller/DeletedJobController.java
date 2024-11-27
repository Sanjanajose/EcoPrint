package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.DeletedJob;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.service.DeletedJobService;
import com.ecoprint.printmanagement.service.ReportExportService;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deleted-jobs")
public class DeletedJobController {

    @Autowired
    private DeletedJobService deletedJobService;

    @Autowired
    private PrintJobRepository printJobRepository;

    @Autowired
    private UserRepository userRepository;

    private final ReportExportService reportExportService;

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
    @Transactional
    @GetMapping("/report")
    public ResponseEntity<List<DeletedJobResponse>> getDeletedJobsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId) throws AccessDeniedException {

        // Delegating access control and report fetching to the service
        List<DeletedJobResponse> report = deletedJobService.getDeletedJobsReportWithAccessControl(startDate, endDate, userId);
        return ResponseEntity.ok(report);
    }



    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportDeletedJobsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long userId) throws AccessDeniedException {

        List<DeletedJobResponse> report = deletedJobService.getDeletedJobsReport(startDate, endDate, userId);

        if (report.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SC_NO_CONTENT)
                    .body(new InputStreamResource(new ByteArrayInputStream("No data found.".getBytes(StandardCharsets.UTF_8))));
        }

        String csv = reportExportService.generateCSVReport(report);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=deleted-jobs-report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

}
