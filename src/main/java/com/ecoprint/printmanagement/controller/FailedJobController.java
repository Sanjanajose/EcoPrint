package com.ecoprint.printmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.DTO.FailedJobDTO;
import com.ecoprint.printmanagement.model.FailedJob;
import com.ecoprint.printmanagement.service.FailedJobService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/failed-jobs")
public class FailedJobController {
	
    @Autowired
    private FailedJobService failedJobService;


    
    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> retryJob(@PathVariable Long id) {
        try {
            failedJobService.retryFailedJob(id);
            return ResponseEntity.ok("Job retried successfully.");
        } catch (IllegalStateException e) {
            // Return a specific error message when retry limit is exceeded
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    
    @GetMapping("/logs")
    public ResponseEntity<List<FailedJobDTO>> getAllFailedJobs() {
        List<FailedJobDTO> failedJobs = failedJobService.getAllFailedJobs();
        return ResponseEntity.ok(failedJobs);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{id}/reassign")
    public ResponseEntity<String> reassignPrinter(@PathVariable Long id, @RequestBody FailedJobDTO failedJobDTO) {
        failedJobService.reassignPrinter(id, failedJobDTO.getNewPrinterId());
        return ResponseEntity.ok("Printer reassigned successfully.");
    }
    
    


}
