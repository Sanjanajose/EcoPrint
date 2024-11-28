package com.ecoprint.printmanagement.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.JobProgress;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintProcess;



@Service
public class PrintJobManagementService {
	
    private final AtomicLong jobIdGenerator = new AtomicLong(1); // Start job IDs at 1
    private final ConcurrentHashMap<Long, String> activeJobs = new ConcurrentHashMap<>();
    private final Map<Long, PrintProcess> printProcessStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, JobProgress> jobProgress = new ConcurrentHashMap<>();


    /**
     * Send a print job to the printer and track its progress. Printed using this below code
     */
	/*
    public long startJob(String printerIp, int printerPort, InputStream fileData) throws Exception {
        long jobId = jobIdGenerator.incrementAndGet();
        activeJobs.put(jobId, "PRINTING");

        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {

            // Stream file data to the printer
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();

            // Mark the job as completed
            activeJobs.put(jobId, "COMPLETED");
        } catch (Exception e) {
            // Mark the job as failed
            activeJobs.put(jobId, "FAILED");
            throw new Exception("Failed to send print job to the printer: " + e.getMessage(), e);
        }

        return jobId;
    }   */
    
    
    public long startJob(String printerIp, int printerPort, InputStream fileData) throws Exception {
        long jobId = jobIdGenerator.incrementAndGet();
        int totalPages = 100; // Example: Replace with logic to calculate total pages
        activeJobs.put(jobId, "PRINTING");
        jobProgress.put(jobId, new JobProgress(jobId, 0, totalPages)); // Initialize progress

        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

                // Simulate sending a page and updating progress
                Thread.sleep(50); // Simulate time taken per page
                jobProgress.get(jobId).updateProgress(1); // Increment by 1 page
            }

            activeJobs.put(jobId, "COMPLETED");
        } catch (Exception e) {
            activeJobs.put(jobId, "FAILED");
          //  jobHistory.add(new JobHistory(jobId, "FAILED", new Date(), e.getMessage()));
            throw new Exception("Failed to send print job to the printer: " + e.getMessage(), e);
        }

        return jobId;
    }
    
    public Map<String, Object> getJobProgress(long jobId) {
        JobProgress progress = jobProgress.get(jobId);
        if (progress == null) { 
            throw new IllegalArgumentException("No progress found for job ID: " + jobId);
        }
        Map<String, Object> progressDetails = new HashMap<>();
        progressDetails.put("jobId", progress.getJobId());
        progressDetails.put("completedPages", progress.getCompletedPages());
        progressDetails.put("totalPages", progress.getTotalPages());
        progressDetails.put("progressPercentage", progress.getProgressPercentage());
        progressDetails.put("estimatedTimeRemaining", progress.getEstimatedTimeRemaining());
        return progressDetails;
    }


    
    public List<JobStatus> getActiveJobs() {
        return activeJobs.entrySet()
                .stream()
                .map(entry -> new JobStatus(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public PrintProcess getPrintProcessById(long jobId) {
        if (!printProcessStorage.containsKey(jobId)) {
            throw new IllegalArgumentException("No print process found for ID: " + jobId);
        }
        return printProcessStorage.get(jobId);
    }
    
    
    
    
	
	/**
     * Add a new print job to the system.
     *//*
    public PrintJob addJob(String fileName, String description, int pages, InputStream fileData) {
        // Authenticate the user
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + userEmail));

        // Validate file name
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name must not be empty");
        }

        // Check if a job with the same file name exists
        if (printJobRepository.existsByFileName(fileName)) {
            throw new IllegalArgumentException("A job with this file name already exists");
        }

        // Create a new print job
        PrintJob printJob = new PrintJob();
        printJob.setFileName(fileName);
        printJob.setDescription(description);
        printJob.setPages(pages);
        printJob.setStatus("READY");
        printJob.setUser(user);
        printJob.setQueuePosition(assignNextQueuePosition());
        printJob.setCreatedAt(LocalDateTime.now());

        // Save the job to the database
        return printJobRepository.save(printJob);
    }
*/
}
