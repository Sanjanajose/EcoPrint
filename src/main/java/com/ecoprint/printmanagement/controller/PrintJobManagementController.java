package com.ecoprint.printmanagement.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintEvent;
import com.ecoprint.printmanagement.model.PrintProcess;
import com.ecoprint.printmanagement.repository.PrintJobManagementRepository;
import com.ecoprint.printmanagement.service.PrintJobManagementService;



@RestController
@RequestMapping("/api/print")
public class PrintJobManagementController {
	
	@Autowired	
    private PrintJobManagementService printJobManagementService;

	
	@Autowired	
    private PrintJobManagementRepository printJobManagementRepository;
	
	

	@PostMapping("/file")
	public String sendPrintJobs(        @RequestParam String printerIp,
	        @RequestParam(defaultValue = "9100") int printerPort,
	        @RequestParam MultipartFile[] files,
	        @RequestParam(required = false) Integer defaultTray // Optional tray parameter
	) {
	    try {
	        // Fetch available tray if not provided
	       // logger.debug("Fetching available trays...");
	        Integer availableTray = getAvailableTray(printerIp, defaultTray);
	       // logger.debug("Tray selected: {}", availableTray);

	        List<InputStream> fileDataList = Arrays.stream(files)
	                .map(file -> {
	                    try {
	                        return file.getInputStream();
	                    } catch (IOException e) {
	                        throw new RuntimeException(e);
	                    }
	                }).collect(Collectors.toList());
	        List<String> fileNames = Arrays.stream(files)
	                .map(MultipartFile::getOriginalFilename)
	                .collect(Collectors.toList());

	        List<Long> jobIds = printJobManagementService.startJobsForMultipleFiles(
	                printerIp, printerPort, fileDataList, fileNames, availableTray); // Pass tray info

	        StringBuilder response = new StringBuilder();
	        for (int i = 0; i < jobIds.size(); i++) {
	            long jobId = jobIds.get(i);
	            if (jobId == -1L) {
	                response.append("Failed to process file: ").append(fileNames.get(i)).append("\n");
	            } else {
	                response.append("Print job sent successfully using Tray ").append(availableTray).append("! File: ")
	                        .append(fileNames.get(i)).append(" Job ID: ").append(jobId).append("\n");
	            }
	        }
	        return response.toString();
	    } catch (Exception e) {
	       // logger.error("Error processing files: {}", e.getMessage(), e);
	        return "Error processing files: " + e.getMessage();
	    }
	}
	
	
    private Integer getAvailableTray(String printerIp, Integer defaultTray) throws Exception {
        String trayNameOid = "1.3.6.1.2.1.43.8.2.1.18";
        String trayStatusOid = "1.3.6.1.2.1.43.8.2.1.10";

        // Fetch tray names and statuses
        Map<Integer, String> trayStatuses = snmpFetchTrayStatusesAsIntegers(printerIp, trayNameOid, trayStatusOid);

        //logger.debug("Fetched tray statuses: {}", trayStatuses);

        // Check for available trays
        for (Map.Entry<Integer, String> entry : trayStatuses.entrySet()) {
            if ("READY".equalsIgnoreCase(entry.getValue()) || "LOADED".equalsIgnoreCase(entry.getValue())) {
               // logger.debug("Found available tray: {}", entry.getKey());
                return entry.getKey(); // Return the tray number
            }
        }

        // Fallback to default tray if specified
        if (defaultTray != null) {
           // logger.warn("No READY trays found. Using default tray: {}", defaultTray);
            return defaultTray;
        }

        throw new Exception("No available trays for printing and no default tray specified.");
    }

	
	
    private Map<Integer, String> snmpFetchTrayStatusesAsIntegers(String printerIp, String trayNameOid, String trayStatusOid) throws Exception {
        Map<Integer, String> trayStatuses = new HashMap<>();

        // Setup SNMP
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();
        // Community Target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public")); // Replace "public" with your SNMP community string
        target.setAddress(GenericAddress.parse("udp:" + printerIp + "/161"));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        // Tray Names and Statuses
        OID nameOid = new OID(trayNameOid);
        OID statusOid = new OID(trayStatusOid);

        // Fetch tray names and statuses
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(nameOid));
        pdu.add(new VariableBinding(statusOid));
        pdu.setType(PDU.GETBULK);

        ResponseEvent responseEvent = snmp.send(pdu, target);
        if (responseEvent.getResponse() == null) {
            throw new Exception("SNMP request timed out.");
        }

        for (VariableBinding vb : responseEvent.getResponse().getVariableBindings()) {
            String oid = vb.getOid().toString();
            String value = vb.getVariable().toString();

            // Extract tray number from OID and map to status
            if (oid.startsWith(trayNameOid)) {
                int trayNumber = Integer.parseInt(oid.substring(oid.lastIndexOf('.') + 1));
                trayStatuses.put(trayNumber, value);
            } else if (oid.startsWith(trayStatusOid)) {
                int trayNumber = Integer.parseInt(oid.substring(oid.lastIndexOf('.') + 1));
                trayStatuses.put(trayNumber, value);
            }
        }

        snmp.close();
        return trayStatuses;
    }
    

	
	
    
    // Fetch active jobs
    @GetMapping("/active/jobs")
    public List<JobStatus> getActiveJobs() {
        return printJobManagementService.getActiveJobs();
    }
    
   //INSTEAD OF THIS I NEED ERROR STATUS. 
    @GetMapping("/job/{jobId}/status")
    public JobStatus getJobStatus(@PathVariable long jobId) {
    	PrintProcess printProcess = printJobManagementService.getPrintProcessById(jobId);
    	return new JobStatus(
    		    printProcess.getJobId(),
    		    printProcess.getStatus(),
    		    printProcess.getPrinterId(),
    		    printProcess.getStartTime(),
    		    printProcess.getUpdateTime()
    		);

    }
    
    
    @GetMapping("/job/{jobId}/progress")
    public ResponseEntity<?> getJobProgress(@PathVariable long jobId) {
        try {
            Map<String, Object> progress = printJobManagementService.getJobProgress(jobId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job progress not found for job ID: " + jobId);
        }
    }


    // Pause a job
    @PostMapping("/job/{jobId}/pause")
    public String pauseJob(@PathVariable("jobId") long jobId) {
        return printJobManagementService.pauseJob(jobId);
    }

    // Resume a job
    @PostMapping("/job/{jobId}/resume")
    public String resumeJob(@PathVariable long jobId) {
        return printJobManagementService.resumeJob(jobId);
    }

    // Cancel a job
    @PostMapping("/{jobId}/cancel")
    public String cancelJob(@PathVariable long jobId) {
        return printJobManagementService.cancelJob(jobId);
    }
/*
    // Fetch job history
    @GetMapping("/jobs/history")
    public List<JobHistory> getJobHistory() {
        return printJobManagementService.getJobHistory();
    }
*/
    
    
    @PostMapping("/jobs/auto-resume")
    public String triggerAutoResume() {
        printJobManagementService.autoResumeJobs();
        return "Auto-resume process triggered.";
    }

    
    
    //3. Error Handling for Failed Jobs: (6)
    @GetMapping("/job/{jobId}/error-status")
    public ResponseEntity<String> getJobErrorStatus(@PathVariable Long jobId) {
        try {
            // Fetch the print event by job ID
            PrintEvent printEvent = printJobManagementRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job ID not found: " + jobId));
            
            String printerIp = printEvent.getPrinterIp();
            if (printerIp == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Printer IP is missing for job ID: " + jobId);
            }

            // Call the service method to fetch error status
            String errorStatus = printJobManagementService.fetchPrinterErrorStatus(printerIp);

            // Optionally, update the database with the error status
            printEvent.setErrorMessage(errorStatus);
            printJobManagementRepository.save(printEvent);

            // Return the error status
            return ResponseEntity.ok("Error Status for Job ID " + jobId + ": " + errorStatus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch error status for Job ID " + jobId + ": " + e.getMessage());
        }
    }



}