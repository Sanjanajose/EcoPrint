package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.DeletedJob;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.RoleName;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import com.ecoprint.printmanagement.service.DeletedJobService;
import com.ecoprint.printmanagement.service.ReportExportService;
import com.ecoprint.printmanagement.service.UserService;


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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;


import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/deleted-jobs")
public class DeletedJobController {

    @Autowired
    private DeletedJobService deletedJobService;

    @Autowired
    private PrintJobRepository printJobRepository;

    @Autowired
    private UserRepository userRepository;

 // Add the @Autowired annotation or constructor injection for UserService
    @Autowired
    private UserService userService;

    
    @Autowired
    private ReportExportService reportService;


    @Autowired
    public DeletedJobController(ReportExportService reportExportService) {
        this.reportService = reportExportService;
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
    
    
 // Admin Endpoint: Fetch all deleted jobs
    @GetMapping("/admin/deleted-jobs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<DeletedJobResponse>> getDeletedJobsForAdmin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long deletedByUserId) {
        List<DeletedJobResponse> deletedJobs = deletedJobService.fetchDeletedJobsForAdmin(startDate, endDate, deletedByUserId);
        return ResponseEntity.ok(deletedJobs);
    }

    // User Endpoint: Fetch their own deleted jobs
    @GetMapping("/user/deleted-jobs")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<DeletedJobResponse>> getDeletedJobsForUser(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<DeletedJobResponse> deletedJobs = deletedJobService.fetchDeletedJobsForUser(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(deletedJobs);
    }
    
    
    /*@GetMapping("/export/csv")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void exportDeletedJobsToCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long deletedByUserId,
            HttpServletResponse response) throws IOException {
        List<DeletedJobResponse> deletedJobs = deletedJobService.fetchDeletedJobsForAdmin(startDate, endDate, deletedByUserId);
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=deleted_jobs_report.csv");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Job ID,Deleted At,Deleted By,Reason,Previous Status,Restorable Until");
            for (DeletedJobResponse job : deletedJobs) {
                writer.printf("%d,%s,%d,%s,%s,%s%n",
                        job.getId(),
                        job.getDeletedAt(),
                        job.getDeletedByUsername(),
                        job.getReasonForDeletion(),
                        job.getPreviousStatus(),
                        job.getRestorableUntil());
            }
        }
    }
*/
    
    @GetMapping("/deleted-jobs/export")
    public ResponseEntity<?> exportDeletedJobs(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        Authentication authentication) {
        try {
            String username = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                              .anyMatch(grantedAuthority -> 
                                  grantedAuthority.getAuthority().equals("ROLE_ADMIN") || 
                                  grantedAuthority.getAuthority().equals("ROLE_SUPERADMIN"));

            List<DeletedJobResponse> deletedJobs;
            if (isAdmin) {
                deletedJobs = deletedJobService.getDeletedJobsForAdmin(startDate, endDate);
            } else {
                Long userId = userService.findUserIdByUsername(username);
               
                deletedJobs = deletedJobService.getDeletedJobsForUser(startDate, endDate, userId);
            }

            if (deletedJobs.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            byte[] report = reportService.generateDeletedJobsReport(deletedJobs);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=deleted-jobs-report.csv")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(report);
        } catch (UsernameNotFoundException ex) {
            
            return ResponseEntity.status(HttpStatus.SC_FORBIDDEN).body("User not authorized");
        } catch (Exception e) {
           
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Failed to export deleted jobs report");
        }
    }

    

}
