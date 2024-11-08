package com.ecoprint.printmanagement.config;



import com.ecoprint.printmanagement.model.JobStatusMessage;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.service.PrintJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobStatusWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private PrintJobService printJobService;

    @PostMapping("/api/print-jobs/update-status")
    public ResponseEntity<String> updateJobStatus(@RequestParam Long jobId, @RequestParam PrintJobStatus status, @RequestParam String comments) {
        try {
            // Log the incoming request for debugging
            System.out.println("Updating job status for Job ID: " + jobId + " to status: " + status + " with comments: " + comments);
            
            // Update the status in the service
            printJobService.updateJobStatus(jobId, status, comments);
            
            // Log the message broadcasting
            System.out.println("Broadcasting to /topic/job-status: Job ID=" + jobId + ", Status=" + status + ", Comments=" + comments);
            
            // Send the update message to WebSocket subscribers
            messagingTemplate.convertAndSend("/topic/job-status", new JobStatusMessage(jobId, status, comments));
            
            return ResponseEntity.ok("Job status updated successfully");
        } catch (Exception e) {
            System.err.println("Error updating job status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update job status");
        }
    }
}
