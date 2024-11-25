package com.ecoprint.printmanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.DTO.FailedJobDTO;
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
    failedJobService.retryFailedJob(id);
        return ResponseEntity.ok("Job retried successfully.");
    }

    
    @PostMapping("/log")
    public ResponseEntity<String> logFailedJob(@RequestBody @Valid FailedJobDTO failedJobDTO) {
    	failedJobService.markJobAsFailed(failedJobDTO.getJobId(), failedJobDTO.getFailureReason());
        return ResponseEntity.ok("Failed job logged successfully.");
    }

    
    @PostMapping("/{id}/reassign")
    public ResponseEntity<String> reassignPrinter(@PathVariable Long id, @RequestBody FailedJobDTO failedJobDTO) {
        failedJobService.reassignPrinter(id, failedJobDTO.getNewPrinterId());
        return ResponseEntity.ok("Printer reassigned successfully.");
    }
    
    


}
