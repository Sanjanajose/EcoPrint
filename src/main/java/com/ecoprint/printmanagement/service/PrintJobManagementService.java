package com.ecoprint.printmanagement.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.ecoprint.printmanagement.repository.SnmpRepository;
import com.ecoprint.printmanagement.responses.FailedJobResponse;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
//import org.snmp4j.SnmpConstants;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import java.awt.Color;





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
    private static final int PAGE_SIZE_BYTES = 1024; // Define the size of one page in bytes

    @Autowired
    private  SnmpRepository snmpRepository;


	
    private final AtomicLong jobIdGenerator = new AtomicLong(1); // Start job IDs at 1
    private final ConcurrentHashMap<Long, String> activeJobs = new ConcurrentHashMap<>();
    private final Map<Long, PrintProcess> printProcessStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, JobProgress> jobProgress = new ConcurrentHashMap<>();
    public static final int COMPLETED = 3; // Example status code for "Completed"
    public static final int PROCESSING = 2; // Example status code for "Processing"
    public static final int PENDING = 1; // Example status code for "Pending"
    

    
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
    	    String printerIp, int printerPort, List<InputStream> fileDataList, List<String> fileNames, Integer tray, String paperSize
    	) throws Exception {
    	    if (fileDataList.size() != fileNames.size()) {
    	        throw new IllegalArgumentException("Mismatch between file data and file names count.");
    	    }

    	    List<Long> jobIds = new ArrayList<>();
    	    for (int i = 0; i < fileDataList.size(); i++) {
    	        InputStream fileData = fileDataList.get(i);
    	        String fileName = fileNames.get(i);

    	        long jobId = -1L;
    	        try {
    	            jobId = startJob(printerIp, printerPort, fileData, fileName, tray, paperSize);
    	            jobIds.add(jobId);
    	        } catch (Exception e) {
    	            jobIds.add(-1L);
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
   
    /**
     * Starts a print job by sending file data to the printer, tracking its progress, and handling any errors.
     *
     * @param printerIp  The IP address of the target printer.
     * @param printerPort The port number used to communicate with the printer.
     * @param fileData   The input stream of the file to be printed.
     * @param fileName   The name of the file being printed.
     * @param tray       (Optional) The printer tray to use.
     * @param paperSize  (Optional) The paper size (e.g., A4, Letter) for the print job.
     * @return           A unique job ID assigned to the print job.
     * @throws Exception If an error occurs during the print process.
     */
    /*BEFORE JOB ID
    public long startJob(
        String printerIp, int printerPort, InputStream fileData, String fileName, Integer tray, String paperSize
    ) throws Exception {
        // Generate a unique job ID for this print job
        long jobId = jobIdGenerator.incrementAndGet();
        //
        // Initialize job status to "PENDING"
        activeJobs.put(jobId, "PENDING");

        // Create and save the initial job metadata (PrintEvent)
        PrintEvent printEvent = new PrintEvent();
        printEvent.setJobId(jobId);
        printEvent.setFileName(fileName);
        printEvent.setPrinterIp(printerIp);
        printEvent.setStatus("PENDING");
        printEvent.setCreatedAt(new Date());
        printEvent.setLastUpdated(LocalDateTime.now());
        printEvent.setProgressPercentage(0.0);
/*
        if (tray != null) {
            // Optional: Set printer tray information if provided
            printEvent.setPrinterTray(tray);
        }
        if (paperSize != null) {
            // Optional: Set paper size information if provided
            printEvent.setPaperSize(paperSize);
        }*/
     /*   printJobManagementRepository.save(printEvent); // Persist the metadata

        try (Socket socket = new Socket(printerIp, printerPort);
             OutputStream outputStream = socket.getOutputStream()) {

            // Update status to "PROCESSING" and persist
            printEvent.setStatus("PROCESSING");
            activeJobs.put(jobId, "PROCESSING");
            printJobManagementRepository.save(printEvent);

            byte[] buffer = new byte[1024];
            int bytesRead;
            int totalBytes = 0;
            long fileSize = fileData.available(); // For progress calculation

            // Stream file data to the printer in chunks
            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                // Update progress and pages printed
                int completedPages = totalBytes / 1024; // Assume 1KB = 1 page
                printEvent.setCompletedPages(completedPages);
                double progressPercentage = (double) totalBytes / fileSize * 100;
                printEvent.setProgressPercentage(Math.min(progressPercentage, 100.0));
                printEvent.setLastUpdated(LocalDateTime.now());

                // Save the progress to the database
                printJobManagementRepository.save(printEvent);

                // Simulate processing delay for realism
                Thread.sleep(50);
            }
        } catch (Exception e) {
            // Handle failure: Update status and log the error
            activeJobs.put(jobId, "FAILED");
            printEvent.setStatus("FAILED");
            printEvent.setErrorMessage(e.getMessage());
            printEvent.setLastUpdated(LocalDateTime.now());
            printJobManagementRepository.save(printEvent);
            throw new Exception("Failed to send print job: " + e.getMessage(), e);
        } finally {
            // Ensure final job state is saved regardless of outcome
            printJobManagementRepository.save(printEvent);
        }

        // Return the job ID to the caller
        return jobId;
    }
    
    */
    
    
    
    public long startJob(
    	    String printerIp, int printerPort, InputStream fileData, String fileName, Integer tray, String paperSize
    	) throws Exception {
    	    // Generate a unique job ID for this print job
    	    long jobId = jobIdGenerator.incrementAndGet();
    	    activeJobs.put(jobId, "PENDING");

    	    // Create and save initial job metadata
    	    PrintEvent printEvent = new PrintEvent();
    	    printEvent.setJobId(jobId);
    	    printEvent.setFileName(fileName);
    	    printEvent.setPrinterIp(printerIp);
    	    printEvent.setStatus("PENDING");
    	    printEvent.setCreatedAt(new Date());
    	    printEvent.setLastUpdated(LocalDateTime.now());
    	    try {

    	        byte[] fileBytes = fileData.readAllBytes(); // Reads all data from InputStream

    	        printEvent.setFileData(fileBytes); // Save file data

    	    } catch (IOException e) {

    	        throw new Exception("Failed to read file data: " + e.getMessage(), e);

    	    }

    	    printJobManagementRepository.save(printEvent);
    	    try {
    	        // Send the print job to the printer
    	        sendPrintDataToPrinter(printerIp, printerPort, fileData);

    	        // Use the `getCurrentPrintingJobId` method to fetch the printer's job ID
    	        Integer printerJobId = getCurrentPrintingJobId(printerIp);

    	        if (printerJobId == null || printerJobId == -1) {
    	            throw new Exception("No valid job ID found from the printer.");
    	        }

    	        System.out.println("Printer Job ID: " + printerJobId);

    	        // Update the PrintEvent with the printer-specific job ID
    	        printEvent.setPrinterJobId(printerJobId.longValue());
    	        printEvent.setStatus("PROCESSING");
    	        activeJobs.put(jobId, "PROCESSING");

    	        // Save the updated PrintEvent
    	        printJobManagementRepository.save(printEvent);
    	    } catch (Exception e) {
    	        // Handle failures and update the job status
    	        activeJobs.put(jobId, "FAILED");
    	        printEvent.setStatus("FAILED");
    	        printEvent.setErrorMessage(e.getMessage());
    	        printEvent.setLastUpdated(LocalDateTime.now());
    	        printJobManagementRepository.save(printEvent);
    	        throw new Exception("Failed to process print job: " + e.getMessage(), e);
    	        
    	        
    	        
    	    }
    	    return jobId;
    	}

    	private void sendPrintDataToPrinter(String printerIp, int printerPort, InputStream fileData) throws IOException {
    	    try (Socket socket = new Socket(printerIp, printerPort);
    	         OutputStream outputStream = socket.getOutputStream()) {
    	        byte[] buffer = new byte[1024];
    	        int bytesRead;
    	        while ((bytesRead = fileData.read(buffer)) != -1) {
    	            outputStream.write(buffer, 0, bytesRead);
    	        }
    	    }
    	}

    	private Long getPrinterJobId(String printerIp, String oid, String community) throws Exception {
    	    int retryCount = 3; // Number of retry attempts
    	    for (int attempt = 1; attempt <= retryCount; attempt++) {
    	        try {
    	            // Build the SNMP command
    	            String command = "snmpwalk -v2c -c " + community + " " + printerIp + " " + oid;
    	            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
    	            processBuilder.redirectErrorStream(true);

    	            // Start the process and capture the output
    	            Process process = processBuilder.start();
    	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    	            String line;

    	            while ((line = reader.readLine()) != null) {
    	                if (line.contains("STRING:")) { // Update this condition to match expected output format
    	                    return parseJobId(line); // Parse the job ID from the SNMP response
    	                }
    	            }

    	            // Wait for the process to complete
    	            int exitCode = process.waitFor();
    	            if (exitCode != 0) {
    	                throw new IOException("SNMP command failed with exit code " + exitCode);
    	            }

    	        } catch (Exception e) {
    	           // log.error("SNMP request failed on attempt " + attempt + ": " + e.getMessage());
    	            if (attempt == retryCount) {
    	                throw new RuntimeException("Failed to fetch printer job ID after " + retryCount + " attempts", e);
    	            }
    	        }
    	    }
    	    return null; // If no job ID is found
    	}
    	
    	
    	
    	

    	// Example method to parse job ID from SNMP response line
    	private Long parseJobId(String responseLine) {
    	    // Extract job ID from the response. Adjust this based on the SNMP response format.
    	    String[] parts = responseLine.split(":");
    	    if (parts.length > 1) {
    	        return Long.parseLong(parts[1].trim()); // Assuming job ID is the second part
    	    }
    	    throw new IllegalArgumentException("Invalid SNMP response: " + responseLine);
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

    
   
    
    

    
  /*  
    public List<PrintEvent> getActiveJobs(String printerIp) {
        if (printerIp == null || printerIp.isEmpty()) {
            throw new IllegalArgumentException("Printer IP cannot be null or empty.");
        }
        return snmpRepository.getJobStates(printerIp).entrySet().stream()
                .filter(entry -> entry.getValue() == 11) // State "eProcessing" (Printing)
                .map(entry -> new PrintEvent(entry.getKey(), "Printing"))
                .collect(Collectors.toList());
    }
*/
    
    public List<PrintEvent> getActiveJobs(String printerIp) {
        // Fetch job states from SNMP
        Map<String, Integer> jobStates = snmpRepository.getJobStates(printerIp);
        List<PrintEvent> activeJobs = new ArrayList<>();

        // Fetch active jobs from the database based on printer_job_id
        Integer printerJobId = getCurrentPrintingJobId(printerIp);

        List<PrintEvent> dbActiveJobs = printJobManagementRepository.findActiveJobsByPrinterJobId(printerJobId);

        // Add jobs from the database to the response
        activeJobs.addAll(dbActiveJobs);

        // Process SNMP data and add jobs in the "Processing" state
        for (Map.Entry<String, Integer> entry : jobStates.entrySet()) {
            // Only add jobs in the "Processing" state if they are not already in the database
            if (entry.getValue() == 11 && dbActiveJobs.stream().noneMatch(job -> job.getPrinterJobId().equals(entry.getKey()))) {
                activeJobs.add(new PrintEvent(entry.getKey(), "Printing"));
            }
        }

        return activeJobs;
    }
    
    
    
    
    public List<PrintEvent> getActiveJobs(Integer printerJobId) {
        // Fetch active jobs from the database
        List<PrintEvent> dbActiveJobs = printJobManagementRepository.findActiveJobsByPrinterJobId(printerJobId);

        // Initialize the response list
        List<PrintEvent> activeJobs = new ArrayList<>(dbActiveJobs);

        // Fetch job states from SNMP
        Map<String, Integer> jobStates = snmpRepository.getJobStates(printerJobId.toString());

        // Add SNMP jobs only if they are not already in the database list
        for (Map.Entry<String, Integer> entry : jobStates.entrySet()) {
            // Only add jobs in the "Processing" state
            if (entry.getValue() == 11 && dbActiveJobs.stream().noneMatch(job -> job.getPrinterJobId().equals(Integer.parseInt(entry.getKey())))) {
                activeJobs.add(new PrintEvent(entry.getKey(), "Printing"));
            }
        }

        return activeJobs;
    }

/*
    public List<PrintEvent> getActiveJobs(String printerIp) {
        Map<String, Integer> jobStates = snmpRepository.getJobStates(printerIp);
        List<PrintEvent> activeJobs = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : jobStates.entrySet()) {
            // Only add jobs in the "Processing" state
            if (entry.getValue() == 11) {
                activeJobs.add(new PrintEvent(entry.getKey(), "Printing"));
            }
        }

        return activeJobs;
    }
  */  
    
    


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
        PrintEvent job = printJobManagementRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job ID not found: " + jobId));

        if (!"PROCESSING".equalsIgnoreCase(job.getStatus())) {
            throw new IllegalStateException("Cannot pause a job that is not in PROCESSING state.");
        }

        // Communicate with the printer to pause the job
        boolean printerResponse = sendPauseCommandToPrinter(job.getPrinterIp(), job.getJobId());

        if (!printerResponse) {
            throw new IllegalStateException("Failed to send pause command to the printer.");
        }

        // Update the status in the database after confirming the printer action
        job.setStatus("PAUSED");
        job.setLastUpdated(LocalDateTime.now());
        printJobManagementRepository.save(job);

        return "Job " + jobId + " has been paused.";
    }

    
    private boolean sendPauseCommandToPrinter(String printerIp, Long jobId) {
        try {
            // Define the OID for pausing a job (specific to the printer's MIB)
            //String pauseJobOid = "1.3.6.1.2.1.43.11.1.1.9"; // Replace with actual OID for pause action
            
            String pauseJobOid = "1.3.6.1.2.1.43.18.1.1.8.1.610"; // Replace with your OID


            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private")); // Use your printer's community string
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(pauseJobOid), new Integer32(jobId.intValue())));
            pdu.setType(PDU.SET);

            ResponseEvent response = snmp.send(pdu, target);
            snmp.close();

            // Check if the response is successful
            return response.getResponse() != null && response.getResponse().getErrorStatus() == PDU.noError;
        } catch (Exception e) {
            System.err.println("Error sending pause command: " + e.getMessage());
            return false;
        }
    }

    
    public boolean pausePrintJob(String printerIp, String jobId) {
        try {
            System.out.println("Entered into PrintJobManagementService:::" + printerIp + "::::" + jobId);

            // Initialize SNMP client
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            // Configure the SNMP target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private")); // Use the "private" community string
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161")); // Printer IP and SNMP port
            target.setRetries(20); // Retry count
            target.setTimeout(5000); // Timeout in milliseconds
            target.setVersion(SnmpConstants.version2c); // SNMP version 2c

            // OID for pausing a print job
          //  OID pauseJobOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.1.3.0"); // Replace with the actual OID for pause
            OID pauseJobOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.1.2.0"); // Replace with the actual OID for pause

            // Create the PDU for the SNMP SET request
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(pauseJobOid, new Integer32(Integer.parseInt(jobId))));
            pdu.setType(PDU.SET); // SNMP SET operation

            // Send the SNMP request
            System.out.println("Sending SNMP SET request to pause job...");
            ResponseEvent response = snmp.send(pdu, target);

            // Handle the response
            if (response.getResponse() == null) {
                System.out.println("SNMP Request timed out while pausing the job.");
                return false;
            }

            System.out.println("Job pausing response: " + response.getResponse());

            // Update the job status in the database
            PrintEvent printEvent = printJobManagementRepository.findByPrinterJobId(Long.parseLong(jobId));
            if (printEvent != null) {
                printEvent.setStatus("PAUSED");
                printEvent.setLastUpdated(LocalDateTime.now());
                printJobManagementRepository.save(printEvent);
            } else {
                System.err.println("No record found for Printer Job ID: " + jobId);
            }

            snmp.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String resumeJob(long jobId) {
        // Fetch the job details
        PrintEvent job = printJobManagementRepository.findByPrinterJobId(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job ID not found: " + jobId);
        }

        // Ensure the job is in PAUSED state
        if (!"PAUSED".equalsIgnoreCase(job.getStatus())) {
            throw new IllegalStateException("Cannot resume a job that is not in PAUSED state.");
        }

        try {
            // Update the job status to PROCESSING
            job.setStatus("PROCESSING");
            job.setLastUpdated(LocalDateTime.now());
            printJobManagementRepository.save(job);

            // Printing logic: Resume from the last completed page
            int bytesToSkip = job.getCompletedPages() * PAGE_SIZE_BYTES; // Assuming PAGE_SIZE_BYTES is defined
            InputStream fileData = fetchJobFile(job.getJobId()); // Fetch the file associated with the job
            fileData.skip(bytesToSkip); // Skip bytes to resume printing from the correct point

            try (Socket socket = new Socket(job.getPrinterIp(), 9100);
                 OutputStream outputStream = socket.getOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                // Stream data to the printer
                while ((bytesRead = fileData.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    Thread.sleep(50); // Simulate time per page if needed
                }

                // Update status to COMPLETED after successful printing
                job.setStatus("COMPLETED");
                job.setProgressPercentage(100.0);
                job.setLastUpdated(LocalDateTime.now());
            } catch (Exception e) {
                // Handle any printing errors
                job.setStatus("FAILED");
                job.setErrorMessage(e.getMessage());
                job.setLastUpdated(LocalDateTime.now());
                throw new Exception("Failed to resume the print job: " + e.getMessage(), e);
            } finally {
                // Save the job status
                printJobManagementRepository.save(job);
            }

            return "Job " + jobId + " has been resumed and is now printing.";
        } catch (Exception e) {
            throw new RuntimeException("Error resuming job: " + e.getMessage(), e);
        }
    }



    
    
//below code is working fine    
  /*  public boolean cancelPrintJob(String printerIp, String jobId) {
        try {
            System.out.println("Entered into PrintJobManagementService:::" + printerIp + "::::" + jobId);

            // Initialize SNMP client
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            // Configure the SNMP target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private")); // Use the "private" community string
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161")); // Printer IP and SNMP port
            target.setRetries(20); // Retry count (adjustable based on network conditions)
            target.setTimeout(5000); // Timeout in milliseconds
            target.setVersion(SnmpConstants.version2c); // SNMP version 2c


            // OID for canceling a print job (replace with your printer's OID)
            OID cancelJobOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.1.2.0");

            // Create the PDU for the SNMP SET request
            PDU pdu = new PDU();
            // Add the OID and the job ID as an integer value
            pdu.add(new VariableBinding(cancelJobOid, new Integer32(Integer.parseInt(jobId))));
            pdu.setType(PDU.SET); // SNMP SET operation

            // Send the SNMP request
            System.out.println("Sending SNMP SET request to cancel job...");
            ResponseEvent response = snmp.send(pdu, target);

            // Handle the response
            if (response.getResponse() == null) {
                System.out.println("SNMP Request timed out while canceling the job.");
                return false;
            }

            System.out.println("Job cancellation response: " + response.getResponse());
            snmp.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/
    
    public boolean cancelPrintJob(String printerIp, String jobId) {
        try {
            System.out.println("Entered into PrintJobManagementService:::" + printerIp + "::::" + jobId);

            // Initialize SNMP client
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            // Configure the SNMP target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private")); // Use the "private" community string
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161")); // Printer IP and SNMP port
            target.setRetries(20); // Retry count
            target.setTimeout(5000); // Timeout in milliseconds
            target.setVersion(SnmpConstants.version2c); // SNMP version 2c

            // OID for canceling a print job
            OID cancelJobOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.1.2.0");
            


            // Create the PDU for the SNMP SET request
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(cancelJobOid, new Integer32(Integer.parseInt(jobId))));
            pdu.setType(PDU.SET); // SNMP SET operation

            // Send the SNMP request
            System.out.println("Sending SNMP SET request to cancel job...");
            ResponseEvent response = snmp.send(pdu, target);

            // Handle the response
            if (response.getResponse() == null) {
                System.out.println("SNMP Request timed out while canceling the job.");
                return false;
            }

            System.out.println("Job cancellation response: " + response.getResponse());

            // Update the job status in the database
            PrintEvent printEvent = printJobManagementRepository.findByPrinterJobId(Long.parseLong(jobId));
            if (printEvent != null) {
                printEvent.setStatus("CANCELLED");
                printEvent.setLastUpdated(LocalDateTime.now());
                printJobManagementRepository.save(printEvent);
            } else {
                System.err.println("No record found for Printer Job ID: " + jobId);
            }

            snmp.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

/*
    
    public boolean cancelPrintJobDUMMY(String printerIp, String jobId) {
        try {
            System.out.println("Entered into PrintJobManagementService:::" + printerIp + "::::" + jobId);

            // Initialize SNMP client
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            // Configure the SNMP target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private")); // Use the "private" community string
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161")); // Printer IP and SNMP port
            target.setRetries(20); // Retry count
            target.setTimeout(5000); // Timeout in milliseconds
            target.setVersion(SnmpConstants.version2c); // SNMP version 2c

            // OID to check the status of the specific job
            OID jobStatusOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.5.19." + jobId + ".0");

            // Create the PDU for the SNMP GET request
            PDU getPdu = new PDU();
            getPdu.add(new VariableBinding(jobStatusOid)); // Add the OID to fetch the job status
            getPdu.setType(PDU.GET);

            // Send the SNMP GET request
            System.out.println("Sending SNMP GET request to fetch job status...");
            ResponseEvent getResponse = snmp.get(getPdu, target);

            // Handle the response
            if (getResponse.getResponse() == null) {
                System.out.println("SNMP Request timed out while fetching job status.");
                return false;
            }

            // Extract the status from the response
            VariableBinding vb = getResponse.getResponse().get(0);
            int jobStatus = vb.getVariable().toInt();
            System.out.println("Job status retrieved: " + jobStatus);

            // Check if the job is completed
            
            if (jobStatus == COMPLETED) {
                throw new IllegalStateException("Cannot cancel a job that is already completed ");
            }


            // OID for canceling a print job
            OID cancelJobOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.1.2.0");

            // Create the PDU for the SNMP SET request
            PDU setPdu = new PDU();
            setPdu.add(new VariableBinding(cancelJobOid, new Integer32(Integer.parseInt(jobId)))); // Add the OID and the job ID
            setPdu.setType(PDU.SET); // SNMP SET operation

            // Send the SNMP SET request to cancel the job
            System.out.println("Sending SNMP SET request to cancel job...");
            ResponseEvent setResponse = snmp.send(setPdu, target);

            // Handle the response
            if (setResponse.getResponse() == null) {
                System.out.println("SNMP Request timed out while canceling the job.");
                return false;
            }

            System.out.println("Job cancellation response: " + setResponse.getResponse());
            snmp.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

*/

    private void executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Failed to execute SNMP command. Exit code: " + exitCode);
        }

        System.out.println("Command executed successfully: " + command);
    }

    private String fetchActiveJobId(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            if (process.waitFor() != 0) {
                throw new RuntimeException("Command execution failed: " + output);
            }

            // Parse and return the active job ID from the SNMP output
            // Adjust parsing logic based on your SNMP response format
            return parseJobIdFromSnmpOutput(output.toString());
        }
    }

    private String parseJobIdFromSnmpOutput(String snmpOutput) {
        // Implement logic to extract job ID from the SNMP response
        // Example: Assume the job ID is the last number in the output
        String[] lines = snmpOutput.split("\n");
        if (lines.length > 0) {
            String lastLine = lines[lines.length - 1];
            String[] parts = lastLine.split(" ");
            return parts[parts.length - 1]; // Assuming job ID is the last token
        }
        return null;
    }

    
    private void triggerPrintJob(PrintEvent job) {
        // Logic to resume the print job
        // Example: Continue streaming the file to the printer
        try {
            Socket socket = new Socket(job.getPrinterIp(), 9100);
            OutputStream outputStream = socket.getOutputStream();

            // Resume printing from the last completed page
            int bytesToSkip = job.getCompletedPages() * PAGE_SIZE_BYTES; // Assuming PAGE_SIZE_BYTES is defined
            InputStream fileData = fetchJobFile(job.getJobId());
            fileData.skip(bytesToSkip);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileData.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            socket.close();

            // Update status to COMPLETED
            job.setStatus("COMPLETED");
            job.setLastUpdated(LocalDateTime.now());
            printJobManagementRepository.save(job);

        } catch (Exception e) {
            // Handle errors
            job.setStatus("FAILED");
            printJobManagementRepository.save(job);
        }
    }
    
    public InputStream fetchJobFile(Long jobId) throws Exception {
        PrintEvent printEvent = printJobManagementRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job ID not found: " + jobId));

        byte[] fileData = printEvent.getFileData(); // Assuming `fileData` is a BLOB field
        if (fileData == null || fileData.length == 0) {
            throw new IllegalArgumentException("File data is missing for Job ID: " + jobId);
        }

        return new ByteArrayInputStream(fileData);
    }


    public void stopPrintJob(Long jobId) throws Exception {
        // Fetch the PrintEvent to get the printer IP and job details
        PrintEvent printEvent = printJobManagementRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job ID not found: " + jobId));

        String printerIp = printEvent.getPrinterIp();
        if (printerIp == null || printerIp.isEmpty()) {
            throw new IllegalArgumentException("Printer IP is missing for Job ID: " + jobId);
        }

        // Send a command to the printer to stop the job
        boolean stopped = sendStopCommandToPrinter(printerIp, jobId);
        if (!stopped) {
            throw new Exception("Failed to stop print job on the printer.");
        }

        // Update job status to "CANCELED"
        printEvent.setStatus("CANCELED");
        printEvent.setLastUpdated(LocalDateTime.now());
        printJobManagementRepository.save(printEvent);
    }

    private boolean sendStopCommandToPrinter(String printerIp, Long jobId) {
        try {
            // Example implementation for SNMP-based stop command
            String stopCommandOid = "1.3.6.1.2.1.43.11.1.1.9.1"; // Replace with actual OID for stopping a job

            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public")); // Use correct SNMP community string
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(stopCommandOid), new Integer32(jobId.intValue())));
            pdu.setType(PDU.SET);

            ResponseEvent response = snmp.send(pdu, target);
            snmp.close();

            return response.getResponse() != null && response.getResponse().getErrorStatus() == PDU.noError;
        } catch (Exception e) {
            System.err.println("Error stopping print job: " + e.getMessage());
            return false;
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

    
    public Map<String, String> fetchTrayStatuses(String printerIp) throws Exception {
        // OIDs for tray names and statuses
        String trayNameOid = "1.3.6.1.2.1.43.8.2.1.18";
        String trayStatusOid = "1.3.6.1.2.1.43.8.2.1.10";

        Map<String, String> trayStatuses = new HashMap<>();

        // Setup SNMP
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public")); // Replace with your SNMP community string
        target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        // Fetch tray names
        PDU namePdu = new PDU();
        namePdu.add(new VariableBinding(new OID(trayNameOid)));
        namePdu.setType(PDU.GETBULK);

        ResponseEvent nameResponse = snmp.send(namePdu, target);
        if (nameResponse.getResponse() == null) {
            throw new Exception("Failed to fetch tray names. SNMP request timed out.");
        }

        // Parse tray names
        Map<Integer, String> trayNames = new HashMap<>();
        for (VariableBinding vb : nameResponse.getResponse().getVariableBindings()) {
            String oid = vb.getOid().toString();
            String value = vb.getVariable().toString();
            int trayIndex = Integer.parseInt(oid.substring(oid.lastIndexOf('.') + 1));
            trayNames.put(trayIndex, value);
        }

        // Fetch tray statuses
        PDU statusPdu = new PDU();
        statusPdu.add(new VariableBinding(new OID(trayStatusOid)));
        statusPdu.setType(PDU.GETBULK);

        ResponseEvent statusResponse = snmp.send(statusPdu, target);
        if (statusResponse.getResponse() == null) {
            throw new Exception("Failed to fetch tray statuses. SNMP request timed out.");
        }

        // Parse tray statuses
        Map<Integer, String> statuses = new HashMap<>();
        for (VariableBinding vb : statusResponse.getResponse().getVariableBindings()) {
            String oid = vb.getOid().toString();
            String value = vb.getVariable().toString();
            int trayIndex = Integer.parseInt(oid.substring(oid.lastIndexOf('.') + 1));
            statuses.put(trayIndex, value);
        }

        // Combine tray names and statuses
        for (Map.Entry<Integer, String> entry : trayNames.entrySet()) {
            int trayIndex = entry.getKey();
            String trayName = entry.getValue();
            String status = statuses.getOrDefault(trayIndex, "Unknown");
            trayStatuses.put(trayName, status);
        }

        snmp.close();
        return trayStatuses;
    }
    
    
    public void resizeToA4(InputStream inputPdf, OutputStream outputPdf) throws IOException {
        // Load the PDF document
        PDDocument document = PDDocument.load(inputPdf);

        // Resize each page to A4
        for (PDPage page : document.getPages()) {
            page.setMediaBox(PDRectangle.A4); // Set the media box to A4
            page.setCropBox(PDRectangle.A4); // Optional: Set the crop box to A4
            page.setBleedBox(PDRectangle.A4); // Optional: Set the bleed box to A4
            page.setTrimBox(PDRectangle.A4); // Optional: Set the trim box to A4
        }

        // Save the resized PDF to the output stream
        document.save(outputPdf);

        // Close the document
        document.close();
    }

    
    public Integer getCurrentPrintingJobId(String printerIp) {
        try {
            System.out.println("Fetching the current job ID from printer: " + printerIp);

            // Initialize SNMP client
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            // Configure the SNMP target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("private"));
            target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
            target.setRetries(20);
            target.setTimeout(5000);
            target.setVersion(SnmpConstants.version2c);

            // OID for fetching the current job ID
            OID currentJobOid = new OID("1.3.6.1.4.1.11.2.3.9.4.2.1.1.6.2.1.1.0");
            // Create the PDU for the SNMP GET request
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(currentJobOid));
            pdu.setType(PDU.GET);
            System.out.print("pdu::"+pdu);

            // Send the SNMP request
            ResponseEvent response = snmp.send(pdu, target);
            System.out.print("response:::"+response.getResponse());

            // Check for null response
            if (response.getResponse() == null) {
                System.out.println("SNMP Request timed out.");
                return null;
            }
            System.out.println("response.getResponse().getVariableBindings()::: " + response.getResponse().getVariableBindings());

            // Handle the response and check for noSuchInstance
            for (VariableBinding vb : response.getResponse().getVariableBindings()) {
                if (vb.getVariable().isException()) {
                    System.out.println("OID does not exist: " + vb.getOid());
                    return -1; // No job currently printing
                }
            }

            VariableBinding vb = response.getResponse().get(0);
            System.out.println("vb::::: " + vb);

            Integer jobId = vb.getVariable().toInt();
            System.out.println("jobId::::: " + jobId);

            System.out.println("Current job ID: " + jobId);
            snmp.close();
            return jobId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private FailedJobResponse mapToFailedJobResponse(PrintEvent printEvent) {
        FailedJobResponse response = new FailedJobResponse();
        response.setId(printEvent.getJobId());
        response.setPrintJobId(printEvent.getPrinterJobId());
        response.setFailureReason("Job marked as FAILED"); // Example reason
        response.setErrorDetails(printEvent.getErrorMessage());
        //response.setFailedBy(printEvent.get); // Or add logic to fetch responsible user/admin
        response.setFailedAt(printEvent.getLastUpdated());
        return response;
    }

  /*  
    public List<PrintEvent> getFailedJobs() {
        // Fetch failed jobs from the repository
        return printJobManagementRepository.findFailedJobs();
    }*/
    
    
    public List<FailedJobResponse> getFailedJobs() {
        // Fetch failed jobs from the repository
        List<PrintEvent> failedJobs = printJobManagementRepository.findFailedJobs();

        // Map to FailedJobResponse
        return failedJobs.stream().map(this::mapToFailedJobResponse).toList();
    }

}