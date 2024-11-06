package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.Job;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.service.JobService;
import com.ecoprint.printmanagement.service.PrintJobService;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private PrintJobService printJobService;

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestParam MultipartFile file,
                                          @RequestParam String userName,
                                          @RequestParam(required = false) String description) {
        try {
            // First, create the print job
            PrintJob printJob = printJobService.uploadFile(file, userName, description);

            // Then, create the job associated with the print job
            Job job = new Job();
            job.setUser(printJob.getUser());  // Set the user from the print job
            job.setStatus(JobStatus.SUBMITTED);  // Set initial status
            job.setSubmittedAt(LocalDateTime.now());  // Set submission timestamp

            // Create the job in the job service
            Job createdJob = jobService.createJob(job);

            return ResponseEntity.ok(createdJob);
        } catch (Exception e) {
            // Handle exceptions as needed
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Job> getJob(@PathVariable Long jobId) {
        Job job = jobService.getJobWithHistory(jobId);
        return ResponseEntity.ok(job);
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<Job> updateJobStatus(@PathVariable Long jobId, @RequestBody JobStatus newStatus) {
        Job updatedJob = jobService.updateJobStatus(jobId, newStatus);
        return ResponseEntity.ok(updatedJob);
    }
}
