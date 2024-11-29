package com.ecoprint.printmanagement.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintEvent;
import com.ecoprint.printmanagement.model.PrintProcess;
import com.ecoprint.printmanagement.repository.PrintJobManagementRepository;
import com.ecoprint.printmanagement.request.PrintEventRequest;
import com.ecoprint.printmanagement.service.PrintJobManagementService;


@RestController
@RequestMapping("/api/print")
public class PrintJobManagementController {
	
	@Autowired	
    private PrintJobManagementService printJobManagementService;

	
	@Autowired	
    private PrintJobManagementRepository printJobManagementRepository;

    @PostMapping("/file")
    public String sendPrintJob(@RequestParam String printerIp,
                               @RequestParam(defaultValue = "9100") int printerPort,
                               @RequestParam MultipartFile file) {
        try {
            long jobId = printJobManagementService.startJob(printerIp, printerPort, file.getInputStream(), file.getOriginalFilename());
            return "Print job sent successfully! Job ID: " + jobId;
        } catch (Exception e) {
            return "Error sending print job: " + e.getMessage();
        }
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
    public ResponseEntity<Map<String, Object>> getJobProgress(@PathVariable long jobId) {
        Map<String, Object> progress = printJobManagementService.getJobProgress(jobId);
        return ResponseEntity.ok(progress);
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


}
