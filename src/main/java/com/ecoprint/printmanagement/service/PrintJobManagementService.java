package com.ecoprint.printmanagement.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.integration.PrinterCommunicator;
import com.ecoprint.printmanagement.model.JobProgress;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintEvent;
import com.ecoprint.printmanagement.model.PrintProcess;
import com.ecoprint.printmanagement.repository.PrintJobManagementRepository;



@Service
public class PrintJobManagementService {
	
	@Autowired
	private PrinterCommunicator printerCommunicator;
	
	@Autowired
	private PrintJobManagementRepository printJobManagementRepository;
	
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
    
    
    public long startJob(String printerIp, int printerPort, InputStream fileData, String fileName) throws Exception {
        long jobId = jobIdGenerator.incrementAndGet();
        int totalPages = 100; // Replace with logic to calculate total pages
        activeJobs.put(jobId, "PRINTING");
        
        //SUBMITED
        
        
        //PRINT JOBS

        // Check if fileName is null or empty
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is null or empty");
        }
        PrintEvent printEvent = new PrintEvent();
        printEvent.setJobId(jobId);
        printEvent.setCompletedPages(0);
        printEvent.setTotalPages(totalPages);
        printEvent.setProgressPercentage(0.0);
        printEvent.setFileName(fileName); // Set file name here
        printEvent.setStatus("PRINTING");
        printEvent.setCreatedAt(new Date());
        printEvent.setLastUpdated(LocalDateTime.now());
        printEvent.setPrinterIp(printerIp);
        printJobManagementRepository.save(printEvent); // Save the entity
        // Additional logic for printing
        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                Thread.sleep(50); // Simulate time per page
            }
            activeJobs.put(jobId, "COMPLETED");
            printEvent.setStatus("COMPLETED");
        } catch (Exception e) {
            activeJobs.put(jobId, "FAILED");
            printEvent.setStatus("FAILED");
            throw new Exception("Failed to send print job to the printer: " + e.getMessage(), e);
        } finally {
            printJobManagementRepository.save(printEvent); // Save the final status
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
    
    private String updateJobStatus(long jobId, String newStatus, String actionMessage) {
        if (!activeJobs.containsKey(jobId)) {
            return "Job ID " + jobId + " not found.";
        }
        activeJobs.put(jobId, newStatus);
        return actionMessage;
    }

    public String pauseJob(long jobId) {
        if (!activeJobs.containsKey(jobId)) {
            return "Job ID " + jobId + " not found.";
        }
        // Notify printer to pause the job (implementation depends on printer protocol)
        boolean printerResponse = printerCommunicator.pause(jobId);
        if (printerResponse) {
            activeJobs.put(jobId, "PAUSED");
            return "Job paused successfully!";
        } else {
            return "Failed to pause the job on the printer.";
        }
    }

    public String resumeJob(long jobId) {
        if (!activeJobs.containsKey(jobId)) {
            return "Job ID " + jobId + " not found.";
        }
        // Notify printer to resume the job
        boolean printerResponse = printerCommunicator.resume(jobId);
        if (printerResponse) {
            activeJobs.put(jobId, "RESUMED");
            return "Job resumed successfully!";
        } else {
            return "Failed to resume the job on the printer.";
        }
    }

    public String cancelJob(long jobId) {
        if (!activeJobs.containsKey(jobId)) {
            return "Job ID " + jobId + " not found.";
        }
        // Notify printer to cancel the job
        boolean printerResponse = printerCommunicator.cancel(jobId);
        if (printerResponse) {
            activeJobs.remove(jobId);
            return "Job canceled successfully!";
        } else {
            return "Failed to cancel the job on the printer.";
        }
    }

    
    public void autoResumeJobs() {
        activeJobs.forEach((jobId, status) -> {
            if ("PAUSED".equalsIgnoreCase(status) && printerCommunicator.isPrinterAvailable(jobId)) {
                try {
                    resumeJob(jobId);
                    // Log job resumption
                    System.out.println("Job " + jobId + " resumed automatically.");
                } catch (Exception e) {
                    System.err.println("Failed to resume job " + jobId + ": " + e.getMessage());
                }
            }
        });
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
