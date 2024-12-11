package com.ecoprint.printmanagement.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;


import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintEvent;
import com.ecoprint.printmanagement.model.PrintProcess;
import com.ecoprint.printmanagement.model.PrintTask;
import com.ecoprint.printmanagement.repository.PrintJobManagementRepository;
import com.ecoprint.printmanagement.service.IppService;
import com.ecoprint.printmanagement.service.PrintJobManagementService;
import com.ecoprint.printmanagement.service.PrintTaskService;
import com.ecoprint.printmanagement.service.SNMPService;

@RestController
@RequestMapping("/api/print")
public class PrintJobManagementController {

	@Autowired
	private PrintJobManagementService printJobManagementService;

	@Autowired
	private PrintJobManagementRepository printJobManagementRepository;

	@Autowired
	private SNMPService snmpService;

	@Autowired
	private PrintTaskService printTaskService;
	@Autowired
	private IppService ippService;
/*
	@PostMapping("/file")
	public String sendPrintJobs(
	    @RequestParam String printerIp,
	    @RequestParam(defaultValue = "9100") int printerPort,
	    @RequestParam MultipartFile[] files,
	    @RequestParam(required = false) Integer defaultTray, // Optional tray parameter
	    @RequestParam(required = false) String paperSize // New parameter for paper size
	) {
	    try {
	        Integer availableTray = getAvailableTray(printerIp, defaultTray);

	        List<InputStream> fileDataList = Arrays.stream(files).map(file -> {
	            try {
	                return file.getInputStream();
	            } catch (IOException e) {
	                throw new RuntimeException(e);
	            }
	        }).collect(Collectors.toList());
	        List<String> fileNames = Arrays.stream(files).map(MultipartFile::getOriginalFilename)
	                .collect(Collectors.toList());

	        List<Long> jobIds = printJobManagementService.startJobsForMultipleFiles(
	            printerIp, printerPort, fileDataList, fileNames, availableTray, paperSize
	        ); // Pass paperSize along with tray info

	        StringBuilder response = new StringBuilder();
	        for (int i = 0; i < jobIds.size(); i++) {
	            long jobId = jobIds.get(i);
	            if (jobId == -1L) {
	                response.append("Failed to process file: ").append(fileNames.get(i)).append("\n");
	            } else {
	                response.append("Print job sent successfully using Tray ").append(availableTray)
	                        .append(" and Paper Size ").append(paperSize)
	                        .append("! File: ").append(fileNames.get(i))
	                        .append(" Job ID: ").append(jobId).append("\n");
	            }
	        }
	        return response.toString();
	    } catch (Exception e) {
	        return "Error processing files: " + e.getMessage();
	    }
	}*/

	
	@PostMapping("/file")
	public String sendPrintJobs(
	    @RequestParam String printerIp,
	    @RequestParam(defaultValue = "9100") int printerPort,
	    @RequestParam MultipartFile[] files,
	    @RequestParam(required = false) Integer defaultTray, // Optional tray parameter
	    @RequestParam(required = false) String paperSize // New parameter for paper size
	) {
	    try {
	        Integer availableTray = getAvailableTray(printerIp, defaultTray);

	        List<InputStream> fileDataList = new ArrayList<>();
	        List<String> fileNames = new ArrayList<>();

	        for (MultipartFile file : files) {
	            try {
	                // Save the uploaded file temporarily
	                File tempFile = File.createTempFile("uploaded", ".pdf");
	                file.transferTo(tempFile);

	                // Convert to A4 size if specified
	                File convertedFile = File.createTempFile("converted", ".pdf");
	                if ("A4".equalsIgnoreCase(paperSize)) {
	                    try (InputStream inputPdf = new FileInputStream(tempFile);
	                         OutputStream outputPdf = new FileOutputStream(convertedFile)) {
	                        printJobManagementService.resizeToA4(inputPdf, outputPdf);
	                    }
	                } else {
	                    convertedFile = tempFile; // Use original if not converting
	                }

	                // Add the converted file to the list for printing
	                fileDataList.add(new FileInputStream(convertedFile));
	                fileNames.add(file.getOriginalFilename());
	            } catch (IOException e) {
	                throw new RuntimeException("Error processing file: " + file.getOriginalFilename(), e);
	            }
	        }

	        List<Long> jobIds = printJobManagementService.startJobsForMultipleFiles(
	            printerIp, printerPort, fileDataList, fileNames, availableTray, paperSize
	        );

	        StringBuilder response = new StringBuilder();
	        for (int i = 0; i < jobIds.size(); i++) {
	            long jobId = jobIds.get(i);
	            if (jobId == -1L) {
	                response.append("Failed to process file: ").append(fileNames.get(i)).append("\n");
	            } else {
	                response.append("Print job sent successfully using Tray ").append(availableTray)
	                        .append(" and Paper Size ").append(paperSize)
	                        .append("! File: ").append(fileNames.get(i))
	                        .append(" Job ID: ").append(jobId).append("\n");
	            }
	        }
	        return response.toString();
	    } catch (Exception e) {
	        return "Error processing files: " + e.getMessage();
	    }
	}

	private Integer getAvailableTray(String printerIp, Integer defaultTray) throws Exception {
		String trayNameOid = "1.3.6.1.2.1.43.8.2.1.18";
		String trayStatusOid = "1.3.6.1.2.1.43.8.2.1.10.1.2";
		// 2.43.8.2.1.18.1.2
		// snmpget -v2c -c public 10.255.254.101 1.3.6.1.2.1.43.8.2.1.10.1.2

		// Fetch tray names and statuses
		Map<Integer, Integer> rawTrayStatuses = snmpFetchTrayStatusesAsIntegers(printerIp, trayNameOid, trayStatusOid);

		// Map numerical statuses to strings
		Map<Integer, String> trayStatuses = rawTrayStatuses.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> mapTrayStatus(entry.getValue())));

		// Log raw and mapped statuses for debugging
		// logger.debug("Raw tray statuses: {}", rawTrayStatuses);
		// logger.debug("Mapped tray statuses: {}", trayStatuses);

		// Exclude Tray 1 from consideration
		int excludedTray = 1; // Assuming Tray 1 is represented by key 1 in the SNMP response

		// Check for available trays, excluding Tray 1
		for (Map.Entry<Integer, String> entry : trayStatuses.entrySet()) {
			if (!entry.getKey().equals(excludedTray)
					&& ("READY".equalsIgnoreCase(entry.getValue()) || "LOADED".equalsIgnoreCase(entry.getValue()))) {
				// logger.debug("Found available tray: {}", entry.getKey());
				return entry.getKey(); // Return the tray number
			}
		}

		// Fallback to Tray 2 or a default tray if specified
		int preferredTray = 2; // Assuming Tray 2 is your preferred tray
		if (trayStatuses.containsKey(preferredTray) && ("READY".equalsIgnoreCase(trayStatuses.get(preferredTray))
				|| "LOADED".equalsIgnoreCase(trayStatuses.get(preferredTray)))) {
			return preferredTray;
		}

		if (defaultTray != null) {
			// logger.warn("No READY trays found. Using default tray: {}", defaultTray);
			return defaultTray;
		}

		throw new Exception("No available trays for printing and no default tray specified.");
	}

	private String mapTrayStatus(int status) {
		switch (status) {
		case 0:
			return "READY";
		case 104:
			return "EMPTY";
		// Add other mappings based on SNMP response
		default:
			return "UNKNOWN";
		}
	}

	private Map<Integer, Integer> snmpFetchTrayStatusesAsIntegers(String printerIp, String trayNameOid,
			String trayStatusOid) throws Exception {
		Map<Integer, Integer> trayStatuses = new HashMap<>();

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

		// Fetch tray statuses (GETBULK request for both OIDs)
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(trayNameOid))); // Tray Names
		pdu.add(new VariableBinding(new OID(trayStatusOid))); // Tray Statuses
		pdu.setType(PDU.GETBULK);

		ResponseEvent responseEvent = snmp.send(pdu, target);

		if (responseEvent.getResponse() == null) {
			throw new Exception("SNMP request timed out.");
		}

		// Parse the SNMP response
		for (VariableBinding vb : responseEvent.getResponse().getVariableBindings()) {
			String oid = vb.getOid().toString();
			String value = vb.getVariable().toString();

			if (oid.startsWith(trayStatusOid)) {
				try {
					// Extract tray number from OID
					int trayNumber = Integer.parseInt(oid.substring(oid.lastIndexOf('.') + 1));
					// Parse value as integer (tray status)
					int status = Integer.parseInt(value.trim());
					trayStatuses.put(trayNumber, status);
				} catch (NumberFormatException e) {
					// Log and continue for non-integer or invalid data
					System.err.println("Invalid tray status value: " + value);
				}
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

	// INSTEAD OF THIS I NEED ERROR STATUS.
	@GetMapping("/job/{jobId}/status")
	public JobStatus getJobStatus(@PathVariable long jobId) {
		PrintProcess printProcess = printJobManagementService.getPrintProcessById(jobId);
		return new JobStatus(printProcess.getJobId(), printProcess.getStatus(), printProcess.getPrinterId(),
				printProcess.getStartTime(), printProcess.getUpdateTime());

	}

	/*
	 * @GetMapping("/printer/{printerIp}/trays") public ResponseEntity<?>
	 * getTrayStatuses(@PathVariable String printerIp) { try { Map<String, String>
	 * trayStatuses = printJobManagementService.fetchTrayStatuses(printerIp); return
	 * ResponseEntity.ok(trayStatuses); } catch (Exception e) { return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body("Error fetching tray statuses: " + e.getMessage()); } }
	 */

	@GetMapping("/job/{jobId}/progress")
	public ResponseEntity<?> getJobProgress(@PathVariable long jobId) {
		try {
			Map<String, Object> progress = printJobManagementService.getJobProgress(jobId);
			return ResponseEntity.ok(progress);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job progress not found for job ID: " + jobId);
		}
	}
/*
	@PostMapping("/job/{jobId}/pause")
	public ResponseEntity<String> pauseJob(@PathVariable long jobId) {
		try {
			String response = printJobManagementService.pauseJob(jobId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
*/
	@PostMapping("/job/{jobId}/resume")
	public ResponseEntity<String> resumeJob(@PathVariable long jobId) {
		try {
			String response = printJobManagementService.resumeJob(jobId);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	

	
/*	
	@PostMapping("/{printerIp}/cancel/{jobId}")
	public ResponseEntity<String> cancelPrintJob(
	        @PathVariable String printerIp,
	        @PathVariable String jobId) {

	    System.out.println("Entered into cancelPrintJob in Controller:::" + printerIp + "::::" + jobId);

	    boolean isCanceled = printJobManagementService.cancelPrintJob(printerIp, jobId);

	    System.out.println("After isCanceled:::" + isCanceled);

	    if (isCanceled) {
	        return ResponseEntity.ok("Print job " + jobId + " canceled successfully.");
	    } else {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Failed to cancel print job " + jobId);
	    }
	}
	*/
	
	
	@PostMapping("/{printerIp}/cancel/{jobId}")
	public ResponseEntity<String> cancelPrintJob(
	        @PathVariable String printerIp,
	        @PathVariable String jobId) {
	    try {
	        System.out.println("Entered into cancelPrintJob in Controller:::" + printerIp + "::::" + jobId);

	        boolean isCanceled = printJobManagementService.cancelPrintJob(printerIp, jobId);

	        System.out.println("After isCanceled:::" + isCanceled);

	        if (isCanceled) {
	            return ResponseEntity.ok("Print job " + jobId + " canceled successfully.");
	        } else {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("Failed to cancel print job " + jobId);
	        }
	    } catch (IllegalStateException e) {
	        // Log the exception
	        System.err.println("Error: " + e.getMessage());
	        e.printStackTrace();
	        // Send the error message and stack trace in the response body
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Error: " + e.getMessage() + "\nStackTrace: " + getStackTrace(e));
	    } catch (Exception e) {
	        // Log the exception
	        System.err.println("Unexpected Error: " + e.getMessage());
	        e.printStackTrace();
	        // Send the error message and stack trace in the response body
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Unexpected Error: " + e.getMessage() + "\nStackTrace: " + getStackTrace(e));
	    }
	}



	/*
	 * // Fetch job history
	 * 
	 * @GetMapping("/jobs/history") public List<JobHistory> getJobHistory() { return
	 * printJobManagementService.getJobHistory(); }
	 */

	@PostMapping("/jobs/auto-resume")
	public String triggerAutoResume() {
		printJobManagementService.autoResumeJobs();
		return "Auto-resume process triggered.";
	}

	// 3. Error Handling for Failed Jobs: (6)
	@GetMapping("/job/{jobId}/error-status")
	public ResponseEntity<String> getJobErrorStatus(@PathVariable Long jobId) {
		try {
			// Fetch the print event by job ID
			PrintEvent printEvent = printJobManagementRepository.findById(jobId)
					.orElseThrow(() -> new IllegalArgumentException("Job ID not found: " + jobId));

			String printerIp = printEvent.getPrinterIp();
			if (printerIp == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Printer IP is missing for job ID: " + jobId);
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

	@GetMapping("/api/tray-status")
	public List<String> getTrayStatus(@RequestParam String printerIp) throws Exception {
		String oid = "1.3.6.1.2.1.43.16.5.1.2"; // OID for tray statuses
		return snmpService.fetchTrayStatuses(printerIp, oid);
	}

	@PostMapping("/submit")
	public ResponseEntity<?> submitPrintTask(@RequestParam("file") MultipartFile file,
			@RequestParam("totalPages") int totalPages) {
		PrintTask printTask = printTaskService.createPrintTask(file, totalPages);
		return ResponseEntity.ok(Map.of("message", "Print task submitted successfully", "taskId", printTask.getId()));
	}
	
	
	
	 


	 
	 
	    @PostMapping("/{jobId}/pause")

	    public String pausePrintJob(@PathVariable int jobId) {

	        return ippService.pausePrintJob(jobId);

	    }
	    
	    
	    @GetMapping("/{printerIp}/current-job")
	    public ResponseEntity<String> getCurrentPrintingJob(@PathVariable String printerIp) {
	        System.out.println("Fetching current printing job for printer: " + printerIp);

	        Integer currentJobId = printJobManagementService.getCurrentPrintingJobId(printerIp);

	        if (currentJobId == null) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("Failed to fetch the current printing job.");
	        }

	        if (currentJobId == -1) {
	            return ResponseEntity.ok("No job is currently printing.");
	        }

	        return ResponseEntity.ok("Current printing job ID: " + currentJobId);
	    }


	
	    private String getStackTrace(Throwable e) {
	        StringBuilder sb = new StringBuilder();
	        for (StackTraceElement element : e.getStackTrace()) {
	            sb.append(element.toString()).append("\n");
	        }
	        return sb.toString();
	    }

	 

}