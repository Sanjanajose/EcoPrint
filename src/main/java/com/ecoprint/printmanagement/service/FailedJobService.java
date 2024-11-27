package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecoprint.printmanagement.DTO.FailedJobDTO;
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
    
    @Autowired
    @Qualifier("emailNotificationService")
    private NotificationService notificationService;

 
    
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
    
    
  
    
    public FailedJob retryFailedJob(Long failedJobId) {
        FailedJob failedJob = failedJobRepository.findById(failedJobId)
                .orElseThrow(() -> new RuntimeException("Failed job not found"));
        // Check if retry limit exceeded
        if (failedJob.getRetryCount() >= 3) {
            sendAlertToAdmin(failedJob);
            throw new IllegalStateException("Retry limit exceeded. Manual intervention required.");
        }
        // Retry logic
        try {
            // Execute retry logic
            printJobService.processPrintJob(failedJob);
            // Remove from failed jobs if successful
            failedJobRepository.delete(failedJob);
            return failedJob;
        } catch (Exception e) {
            // Increment retry count and update the record
            failedJob.setRetryCount(failedJob.getRetryCount() + 1);
            failedJobRepository.save(failedJob);
            throw e;
        }
    }

    
    
    private void sendAlertToAdmin(FailedJob failedJob) {
        String message = "Failed job " + failedJob.getId() + " has exceeded retry limit. Manual intervention required.";
        String emailRecipient = "sashaprabha16@gmail.com";
        String subject = "Alert Mail for Failed Job " + failedJob.getId();
        
        notificationService.sendEmailNotification(emailRecipient, subject, message);
    }

    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void autoRetryFailedJobs() {
        List<FailedJob> failedJobs = failedJobRepository.findAll();
        for (FailedJob failedJob : failedJobs) {
            try {
                if (failedJob.getRetryCount() < 3) {
                    retryFailedJob(failedJob.getId());
                }
            } catch (Exception e) {
                //log.error("Failed to retry job {}", failedJob.getId(), e);
            }
        }
    }
    
    public boolean isRetryable(String failureType) {
        return "NETWORK".equals(failureType) || "TEMPORARY".equals(failureType);
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
    
    public List<FailedJobDTO> getAllFailedJobs() {
        return failedJobRepository.findAll()
                .stream()
                .map(failedJob -> new FailedJobDTO(
                        failedJob.getId(),
                        failedJob.getErrorDetails(),
                        failedJob.getPrinter() != null ? failedJob.getPrinter().getStatus() : null,
                        failedJob.getRetryCount(),
                        failedJob.getPrinter() != null ? failedJob.getPrinter().getName() : null,
                        failedJob.getPrinter() != null ? failedJob.getPrinter().getId() : null, // oldPrinterId
                        failedJob.getNewPrinter() != null ? failedJob.getNewPrinter().getId() : null // newPrinterId
                ))
                .collect(Collectors.toList());
    }


 
    
  /*  
    public List<FailedJobDTO> getAllFailedJobs() {
        List<FailedJob> failedJobs = failedJobRepository.findAllWithPrinters();
        return failedJobs.stream()
            .map(failedJob -> new FailedJobDTO(
                failedJob.getId(),
                failedJob.getFailureReason(),
                failedJob.getRetryCount(),
                failedJob.getPrinter() != null ? failedJob.getPrinter().getName() : null,
                failedJob.getPrinter() != null ? failedJob.getPrinter().getStatus() : null))
            .collect(Collectors.toList());
    }

*/


}
