package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.dto.QueuedJobDTO;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.service.QueueManagementService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueManagementController {

    private final QueueManagementService queueManagementService;

    public QueueManagementController(QueueManagementService queueManagementService) {
        this.queueManagementService = queueManagementService;
    }

    @PostMapping("/add")
    public ResponseEntity<QueuedJobDTO> addJobToQueue(@RequestBody QueuedJobDTO queuedJobDTO) {
        QueuedJobDTO savedJob = queueManagementService.addJobToQueue(queuedJobDTO);
        return ResponseEntity.ok(savedJob);
    }

    @GetMapping("/all")
    public ResponseEntity<List<QueuedJobDTO>> getAllQueuedJobs() {
        List<QueuedJobDTO> queuedJobs = queueManagementService.getAllQueuedJobs();
        return ResponseEntity.ok(queuedJobs);
    }

    @Operation(summary = "Update the status of a queued job",
            description = "Allows updating the status of a queued job by specifying its ID and the new status."
        )
    @PutMapping("/{jobId}/updateStatus")
    public ResponseEntity<QueuedJobDTO> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam PrintJobStatus status) {
        QueuedJobDTO updatedJob = queueManagementService.updateJobStatus(jobId, status);
        return ResponseEntity.ok(updatedJob);
    }

    @DeleteMapping("/{jobId}/remove")
    public ResponseEntity<String> removeJobFromQueue(@PathVariable Long jobId) {
        queueManagementService.removeJobFromQueue(jobId);
        return ResponseEntity.ok("Job removed from the queue successfully.");
    }
}
