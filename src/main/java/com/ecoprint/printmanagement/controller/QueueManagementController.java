package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.dto.QueuedJobDTO;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.QueuedJob;
import com.ecoprint.printmanagement.service.PrintJobService;
import com.ecoprint.printmanagement.service.QueueManagementService;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueManagementController {
	
	@Autowired
	private PrintJobService printJobService;

    private final QueueManagementService queueManagementService;

    public QueueManagementController(QueueManagementService queueManagementService) {
        this.queueManagementService = queueManagementService;
    }

    @PostMapping("/add")
    public ResponseEntity<QueuedJobDTO> addJobToQueue(@RequestParam Long jobId) {
        QueuedJobDTO savedJob = queueManagementService.addJobToQueue(jobId);
        return ResponseEntity.ok(savedJob);
    }

    
    

    @GetMapping("/all")
    public ResponseEntity<List<QueuedJobDTO>> getAllQueuedJobs() {
        List<QueuedJobDTO> queuedJobs = queueManagementService.getAllQueuedJobs();
        return ResponseEntity.ok(queuedJobs);
    }

   



    @DeleteMapping("/{jobId}/remove")
    public ResponseEntity<String> removeJobFromQueue(@PathVariable Long jobId) {
        queueManagementService.removeJobFromQueue(jobId);
        return ResponseEntity.ok("Job removed from the queue and marked as READY in the print jobs table.");
    }
}
