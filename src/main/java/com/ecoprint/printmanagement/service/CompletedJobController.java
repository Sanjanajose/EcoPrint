package com.ecoprint.printmanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CompletedJob;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.repository.CompletedJobRepository;

@RestController
@RequestMapping("/api/completed-jobs")
public class CompletedJobController {
	
	@Autowired
    private CompletedJobService completedJobService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompletedJob>> getAllCompletedJobs() {
        List<CompletedJob> jobs = completedJobService.getAllCompletedJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == @completedJobService.getUserIdByCompletedJobId(#id)")
    public ResponseEntity<CompletedJob> getCompletedJobById(@PathVariable Long id) {
        CompletedJob job = completedJobService.getCompletedJobById(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
    public ResponseEntity<List<CompletedJob>> getCompletedJobsByUserId(@PathVariable Long userId) {
        List<CompletedJob> jobs = completedJobService.getCompletedJobsByUserId(userId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CompletedJob>> getMyCompletedJobs() {
        Long currentUserId = getCurrentUserId(); // Retrieve the current user's ID
        List<CompletedJob> jobs = completedJobService.getMyCompletedJobs(currentUserId);
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompletedJob(@PathVariable Long id) {
        completedJobService.deleteCompletedJob(id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
