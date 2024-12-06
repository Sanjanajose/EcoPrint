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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.integration.PrinterCommunicator;
import com.ecoprint.printmanagement.model.FailedJob;
import com.ecoprint.printmanagement.model.JobProgress;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintEvent;
import com.ecoprint.printmanagement.model.PrintProcess;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.PrintJobManagementRepository;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
//import org.snmp4j.SnmpConstants;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;



@Service
public class PrintJobManagementService {
	
	@Autowired
	private PrinterCommunicator printerCommunicator;
	
	@Autowired
	private PrintJobManagementRepository printJobManagementRepository;
	
    @Autowired
    private PrinterMonitoringService printerMonitoringService; // To fetch printer status

    @Autowired
    private EmailNotificationService emailNotificationService; // To send email notifications


	
    private final AtomicLong jobIdGenerator = new AtomicLong(1); // Start job IDs at 1
    private final ConcurrentHashMap<Long, String> activeJobs = new ConcurrentHashMap<>();
    private final Map<Long, PrintProcess> printProcessStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, JobProgress> jobProgress = new ConcurrentHashMap<>();

    
    @PostConstruct
    public void initializeJobIdGenerator() {
        // Initialize the jobIdGenerator with the maximum jobId from the database
        long maxJobId = printJobManagementRepository.findMaxJobId().orElse(0L);
        jobIdGenerator.set(maxJobId + 1);
        System.out.println("Job ID Generator initialized to start from: " + jobIdGenerator.get());
    }


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
    
    /*IT WAS WORKING BEFORE 3.10
    public List<Long> startJobsForMultipleFiles(
            String printerIp, int printerPort, List<InputStream> fileDataList, List<String> fileNames) throws Exception {

        if (fileDataList.size() != fileNames.size()) {
            throw new IllegalArgumentException("Mismatch between file data and file names count.");
        }

        List<Long> jobIds = new ArrayList<>();
        for (int i = 0; i < fileDataList.size(); i++) {
            InputStream fileData = fileDataList.get(i);
            String fileName = fileNames.get(i);

            // Create and initialize a PrintEvent for this file
            long jobId = jobIdGenerator.incrementAndGet();
            PrintEvent printEvent = new PrintEvent();
            printEvent.setJobId(jobId);
            printEvent.setFileName(fileName);
            printEvent.setPrinterIp(printerIp);
            printEvent.setStatus("PRINTING");
            printEvent.setCreatedAt(new Date());
            printEvent.setLastUpdated(LocalDateTime.now());
            printEvent.setTotalPages(100); // Example default total pages
            printJobManagementRepository.save(printEvent);

            try {
                // Delegate to the startJob method
                startJob(printerIp, printerPort, fileData, fileName);
                activeJobs.put(jobId, "COMPLETED");
                printEvent.setStatus("COMPLETED");
                jobIds.add(jobId);
            } catch (Exception e) {
                // Log error and update the status to FAILED
                System.err.println("Failed to start job for file: " + fileName + ". Error: " + e.getMessage());
                activeJobs.put(jobId, "FAILED");
                printEvent.setStatus("FAILED");
                jobIds.add(-1L); // Indicate failure for this file
            } finally {
                // Save the final status of the PrintEvent
                printEvent.setLastUpdated(LocalDateTime.now());
                printJobManagementRepository.save(printEvent);
            }
        }

        return jobIds;
    }
    */
    
   /* 
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
*/
    
    public List<Long> startJobsForMultipleFiles(
            String printerIp, int printerPort, List<InputStream> fileDataList, List<String> fileNames, Integer tray) throws Exception {

        if (fileDataList.size() != fileNames.size()) {
            throw new IllegalArgumentException("Mismatch between file data and file names count.");
        }

        List<Long> jobIds = new ArrayList<>();
        for (int i = 0; i < fileDataList.size(); i++) {
            InputStream fileData = fileDataList.get(i);
            String fileName = fileNames.get(i);

            // Pass tray info to startJob
            long jobId = -1L;
            try {
                jobId = startJob(printerIp, printerPort, fileData, fileName, tray);
                jobIds.add(jobId);
            } catch (Exception e) {
               // logger.error("Failed to process file: {}. Error: {}", fileName, e.getMessage(), e);
                jobIds.add(-1L); // Indicate failure for this file
            }
        }

        return jobIds;
    }

    /* IT WAS WORKING BEFORE 3.10
    public long startJob(String printerIp, int printerPort, InputStream fileData, String fileName) throws Exception {
        long jobId = jobIdGenerator.incrementAndGet();
        activeJobs.put(jobId, "PRINTING");

        // Create and save initial PrintEvent
        PrintEvent printEvent = new PrintEvent();
        printEvent.setJobId(jobId);
        printEvent.setCompletedPages(0);
        printEvent.setTotalPages(0); // Set default, as this is no longer needed
        printEvent.setProgressPercentage(0.0);
        printEvent.setFileName(fileName);
        printEvent.setStatus("PRINTING");
        printEvent.setCreatedAt(new Date());
        printEvent.setLastUpdated(LocalDateTime.now());
        printEvent.setPrinterIp(printerIp);
        printJobManagementRepository.save(printEvent);

        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytes = 0; // Total bytes processed to simulate progress

            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

                // Increment processed bytes
                totalBytes += bytesRead;

                // Simulate one page printed per chunk
                int completedPages = totalBytes / 1024; // Example: Assume 1KB equals 1 page
                printEvent.setCompletedPages(completedPages);

                // Update progress percentage dynamically
                printEvent.setProgressPercentage(100.0 * totalBytes / fileData.available());

                // Update last updated timestamp and save progress
                printEvent.setLastUpdated(LocalDateTime.now());
                printJobManagementRepository.save(printEvent);

                // Simulate delay to mimic printing
                Thread.sleep(50); // Adjust this as needed to simulate time per page
            }

            // Mark the job as completed
            activeJobs.put(jobId, "COMPLETED");
            printEvent.setStatus("COMPLETED");
            printEvent.setProgressPercentage(100.0); // Ensure progress is 100%
        } catch (Exception e) {
            // Handle job failure
            activeJobs.put(jobId, "FAILED");
            printEvent.setStatus("FAILED");
            printEvent.setProgressPercentage(0.0);
            printEvent.setLastUpdated(LocalDateTime.now());
            printJobManagementRepository.save(printEvent);
            throw new Exception("Failed to send print job to the printer: " + e.getMessage(), e);
        } finally {
            // Save the final status in case of success or failure
            printJobManagementRepository.save(printEvent);
        }

        return jobId;
    }*/
    
    public long startJob(String printerIp, int printerPort, InputStream fileData, String fileName, Integer tray) throws Exception {
        long jobId = jobIdGenerator.incrementAndGet(); // Generate a unique Job ID
        activeJobs.put(jobId, "PENDING"); // Initially set to PENDING

        // Create and save the initial PrintEvent
        PrintEvent printEvent = new PrintEvent();
        printEvent.setJobId(jobId);
        printEvent.setFileName(fileName);
        printEvent.setPrinterIp(printerIp);
        printEvent.setStatus("PENDING"); // Initial Status
        printEvent.setCreatedAt(new Date());
        printEvent.setLastUpdated(LocalDateTime.now());
        printEvent.setProgressPercentage(0.0);

        if (tray != null) {
            // Optional: Save tray info if provided
           // printEvent.setPrinterTray(tray);
        }
        printJobManagementRepository.save(printEvent);

        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {

            // Update status to PROCESSING
            printEvent.setStatus("PROCESSING");
            printJobManagementRepository.save(printEvent);

            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytes = 0; // Track total processed bytes
            long fileSize = fileData.available(); // Total file size for progress calculation

            // Simulate sending file data and update progress
            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

                totalBytes += bytesRead; // Update total bytes processed

                // Update completed pages dynamically
                int completedPages = totalBytes / 1024; // Example: 1 KB = 1 Page
                printEvent.setCompletedPages(completedPages);

                // Update progress percentage
                double progressPercentage = (double) totalBytes / fileSize * 100;
                printEvent.setProgressPercentage(Math.min(progressPercentage, 100.0));

                // Update timestamp and save progress
                printEvent.setLastUpdated(LocalDateTime.now());
                printJobManagementRepository.save(printEvent);

                // Simulate delay for realistic progress
                Thread.sleep(50);
            }

            // Mark job as COMPLETED
            activeJobs.put(jobId, "COMPLETED");
            printEvent.setStatus("COMPLETED");
            printEvent.setProgressPercentage(100.0); // Ensure progress is capped at 100%
        } catch (Exception e) {
            // Handle job failure and mark as FAILED
            activeJobs.put(jobId, "FAILED");
            printEvent.setStatus("FAILED");
            printEvent.setProgressPercentage(0.0);

            // Record error details
            printEvent.setErrorMessage(e.getMessage());
            printEvent.setLastUpdated(LocalDateTime.now());
            printJobManagementRepository.save(printEvent);

            throw new Exception("Failed to send print job: " + e.getMessage(), e);
        } finally {
            // Save the final status (COMPLETED or FAILED)
            printJobManagementRepository.save(printEvent);
        }

        return jobId;
    }
    
    public Map<String, Object> getJobProgress(long jobId) {
        // Fetch the PrintEvent for the given jobId
        PrintEvent printEvent = printJobManagementRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("No progress found for job ID: " + jobId));

        // Dynamically calculate progressPercentage if status is "COMPLETED"
        double progressPercentage = printEvent.getStatus().equalsIgnoreCase("COMPLETED") ? 100.0 : printEvent.getProgressPercentage();

        // Build the progress details
        Map<String, Object> progressDetails = new HashMap<>();
        progressDetails.put("jobId", printEvent.getJobId());
       // progressDetails.put("completedPages", printEvent.getCompletedPages());
        progressDetails.put("progressPercentage", progressPercentage);
        progressDetails.put("estimatedTimeRemaining", printEvent.getEstimatedTimeRemaining() != null ? printEvent.getEstimatedTimeRemaining() : "N/A");

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

    @Scheduled(fixedRate = 30000) // Poll every 30 seconds
    public void monitorPrinterErrors() {
        try {
            List<String> printers = getAllPrinters(); // Fetch list of printer IPs
            for (String printerIp : printers) {
                String errorStatus = printerMonitoringService.getPrinterErrorStatus(printerIp);
               /* if (!errorStatus.equalsIgnoreCase("No Error")) {
                    notifyError(printerIp, errorStatus); // Call notification logic
                }*/
            }
        } catch (Exception e) {
            System.err.println("Error during printer monitoring: " + e.getMessage());
        }
    }

    private List<String> getAllPrinters() {
        // Fetch printer IPs (from database, properties file, etc.)
        return List.of("10.255.254.101"); // Example IPs
    }
/*
    private void notifyError(String printerIp, String errorMessage) {
    	
        // Notification logic
        String message = "Error detected on printer " + printerIp + ": " + errorMessage;
        emailNotificationService.sendEmailNotification(emailRecipient, subject, message); // Send email notification
        
    }*/

    
    /*
    private void sendAlertToUser(FailedJob failedJob) {
        Long userId = Long.parseLong(failedJob.getFailedBy()); // Assuming `failedBy` stores the user ID as a String
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String emailRecipient = user.getEmail();
            String message = "Failed job " + failedJob.getId() + " has exceeded retry limit. Manual intervention required.";
            String subject = "Alert Mail for Failed Job " + failedJob.getId();

            notificationService.sendEmailNotification(emailRecipient, subject, message);
        } else {
            System.err.println("User not found for ID: " + userId);
        }
    }*/

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
    
    
    public String fetchPrinterErrorStatus(String printerIp) throws Exception {
        // OID for printer error status (alerts table)
        String errorStatusOid = "1.3.6.1.2.1.43.18.1.1.8";
        
        // SNMP setup
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();
        
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public")); // Default SNMP community string
        target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        // Prepare SNMP PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(errorStatusOid))); // OID for error status
        pdu.setType(PDU.GETBULK);

        // Send SNMP request
        ResponseEvent responseEvent = snmp.send(pdu, target);
        if (responseEvent.getResponse() == null) {
            throw new Exception("SNMP request timed out.");
        }

        // Parse response
        StringBuilder errorMessages = new StringBuilder();
        for (VariableBinding vb : responseEvent.getResponse().getVariableBindings()) {
            errorMessages.append(vb.getVariable().toString()).append("; ");
        }

        snmp.close();

        // Return consolidated error messages
        return errorMessages.toString().trim();
    }

}