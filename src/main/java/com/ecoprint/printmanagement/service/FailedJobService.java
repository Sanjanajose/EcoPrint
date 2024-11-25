package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.FailedJob;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.Printer;
import com.ecoprint.printmanagement.repository.FailedJobRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.PrinterRepository;

@Service
public class FailedJobService {

	
    @Autowired
    private FailedJobRepository failedJobRepository;
    
    @Autowired
    private PrintJobRepository printJobRepository;
    
    @Autowired
    private PrintJobService printJobService; 

    @Autowired
    private PrinterRepository printerRepository;
 
    
    public void logFailedJob(PrintJob printJob, String errorDetails, String failedBy) {
        FailedJob failedJob = new FailedJob();
        failedJob.setPrintJob(printJob);
        failedJob.setErrorDetails(errorDetails);
        failedJob.setFailedAt(LocalDateTime.now());
        failedJob.setFailedBy(failedBy);

        failedJobRepository.save(failedJob);
    }

/*
    public void retryJob(Long failedJobId) {
        FailedJob failedJob = failedJobRepository.findById(failedJobId)
                .orElseThrow(() -> new RuntimeException("Failed job not found"));
        
	    Optional<PrintJob> optionalJob = printJobRepository.findById(failedJobId);

        System.out.println(failedJob);
        // Retry logic (invoke print service or reassign the job)
        // Example: printService.printJob(failedJob.getJobId());
        
        failedJob.setRetryCount(optionalJob.getRetryCount() + 1);
        failedJobRepository.save(failedJob);
    }*/
    
    
    
    public void retryFailedJob(Long failedJobId) {
        // Retrieve the failed job
    	
    	System.out.println("failedJobId::->"+failedJobId);
        FailedJob failedJob = failedJobRepository.findById(failedJobId)
                .orElseThrow(() -> new RuntimeException("Failed job not found"));

        // Retrieve the associated print job
        PrintJob printJob = failedJob.getPrintJob();

        try {
            // Retry the print job
        	printJobService.retryPrintJob(printJob);

            // If successful, remove the failed job record
            failedJobRepository.delete(failedJob);

        } catch (Exception e) {
            // Update the retry count and error details if the retry fails
            failedJob.setRetryCount(failedJob.getRetryCount() + 1);
            failedJob.setErrorDetails(e.getMessage());
            failedJobRepository.save(failedJob);
            throw new RuntimeException("Retry failed: " + e.getMessage());
        }
    }
    
    
    //Updating fail Job Status.
    public void markJobAsFailed(Long jobId, String failureReason) {
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Print job not found"));
        printJob.setStatus(PrintJobStatus.FAILED);
        //printJob.setFailedAt(LocalDateTime.now());
        //printJob.setFailureReason(failureReason);
        printJobRepository.save(printJob);
    }

    
    public void reassignPrinter(Long jobId, Long newPrinterId) {
        FailedJob failedJob = failedJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Failed job not found"));

       Printer newPrinter = printerRepository.findById(newPrinterId)
                .orElseThrow(() -> new RuntimeException("Printer not found"));
      
       
       failedJob.setPrinter(newPrinter);
       failedJobRepository.save(failedJob);


    }



}
