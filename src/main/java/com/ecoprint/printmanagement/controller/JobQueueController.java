package com.ecoprint.printmanagement.controller;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.service.JobQueueService;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobQueueController {

    @Autowired
    private JobQueueService jobQueueService;

    /**
     * Endpoint to fetch the current status of all jobs in the queue.
     * 
     * @return A map categorizing jobs by their status.
     */
    @GetMapping("/queue-status")
    @Operation(summary = "allows to fetch the job statuses from the service layer- queue")
    public ResponseEntity<Map<String, List<PrintJob>>> getQueueJobStatuses() {
        // Fetch the job statuses from the service layer
        Map<String, List<PrintJob>> queueJobStatuses = jobQueueService.getQueueJobStatuses();

        // Return the categorized jobs with an HTTP 200 status
        return ResponseEntity.ok(queueJobStatuses);
    }
}
