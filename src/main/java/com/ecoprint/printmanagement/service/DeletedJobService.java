package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.dto.DeletedJobResponse;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.DeletedJob;
import com.ecoprint.printmanagement.model.DeletionAuditLog;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.DeletedJobRepository;
import com.ecoprint.printmanagement.repository.DeletionAuditLogRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

@Service
public class DeletedJobService {

    @Autowired
    private PrintJobRepository printJobRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeletedJobRepository deletedJobRepository;
    
    @Autowired
    private UserService userservice;
    
    
    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private DeletionAuditLogRepository deletionAuditLogRepository;
    
    
    
    private final EmailNotificationService emailNotificationService;
    private final PushNotificationService pushNotificationService;

    @Autowired
    public DeletedJobService(EmailNotificationService emailNotificationService,
                             PushNotificationService pushNotificationService) {
        this.emailNotificationService = emailNotificationService;
        this.pushNotificationService = pushNotificationService;
    }

    
   /* @Transactional
    public void deleteJob(Long jobId, Long deletedByUserId, String reason) {
        // Fetch the PrintJob entity (ensure it's managed)
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
        System.out.println("Fetched PrintJob: " + printJob);

        // Fetch the User entity (ensure it's managed)
        User deletedBy = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", deletedByUserId));
        System.out.println("Fetched User: " + deletedBy);

        // Create a DeletedJob entity
        DeletedJob deletedJob = new DeletedJob();
        deletedJob.setPrintJob(printJob);  // Attach the managed PrintJob
        deletedJob.setDeletedBy(deletedBy);  // Attach the managed User
        deletedJob.setDeletedAt(LocalDateTime.now());
        deletedJob.setReasonForDeletion(reason);
        deletedJob.setRestorableUntil(LocalDateTime.now().plusDays(30)); // Example: 30 days restorable
        deletedJob.setPreviousStatus(printJob.getStatus()); 
        System.out.println("Created DeletedJob: " + deletedJob);

        // Save the DeletedJob entity
        deletedJobRepository.save(deletedJob);
        System.out.println("Saved DeletedJob.");

        // Update the status of the PrintJob
        printJob.setStatus(PrintJobStatus.DELETED); // Assuming DELETED is part of the PrintJobStatus enum
        printJobRepository.save(printJob); // Persist the updated status
        System.out.println("Updated PrintJob status to DELETED.");
        
     // Log the action
        activityLogService.logActivity(
                "DELETE_JOB",
                deletedBy.getUsername(),
                deletedBy.getId(),
                "Deleted print job with ID: " + jobId + ". Reason: " + reason
            );
    } */
    
    @Transactional
    public void deleteJob(Long jobId, Long deletedByUserId, String reason) {
        // Fetch the PrintJob entity (ensure it's managed)
        PrintJob printJob = printJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("PrintJob", "id", jobId));
        System.out.println("Fetched PrintJob: " + printJob);

        // Fetch the User entity (ensure it's managed)
        User deletedBy = userRepository.findById(deletedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", deletedByUserId));
        System.out.println("Fetched User: " + deletedBy);

        // Create a DeletedJob entity
        DeletedJob deletedJob = new DeletedJob();
        deletedJob.setPrintJob(printJob);  // Attach the managed PrintJob
        deletedJob.setDeletedBy(deletedBy);  // Attach the managed User
        deletedJob.setDeletedAt(LocalDateTime.now());
        deletedJob.setReasonForDeletion(reason);
        deletedJob.setRestorableUntil(LocalDateTime.now().plusDays(30)); // Example: 30 days restorable
        deletedJob.setPreviousStatus(printJob.getStatus()); 
        System.out.println("Created DeletedJob: " + deletedJob);

        // Save the DeletedJob entity
        deletedJobRepository.save(deletedJob);
        System.out.println("Saved DeletedJob.");

        // Update the status of the PrintJob
        printJob.setStatus(PrintJobStatus.DELETED); // Assuming DELETED is part of the PrintJobStatus enum
        printJobRepository.save(printJob); // Persist the updated status
        System.out.println("Updated PrintJob status to DELETED.");

        // Log the action
        activityLogService.logActivity(
                "DELETE_JOB",
                deletedBy.getUsername(),
                deletedBy.getId(),
                "Deleted print job with ID: " + jobId + ". Reason: " + reason
        );

        // Add audit log for deletion
        DeletionAuditLog deletionAuditLog = new DeletionAuditLog();
        deletionAuditLog.setJobId(jobId);
        deletionAuditLog.setDeletedByUserId(deletedByUserId);
        deletionAuditLog.setDocumentName(printJob.getFileName()); // Assuming the fileName field exists in PrintJob
        deletionAuditLog.setDeletionTime(LocalDateTime.now());
        deletionAuditLog.setReasonForDeletion(reason);
        deletionAuditLogRepository.save(deletionAuditLog);
        System.out.println("Deletion audit log created for job ID: " + jobId);
        
        notifyJobDeletion(jobId, deletedByUserId, reason);
    }


    



    
    @Transactional
    public void restoreDeletedJob(Long jobId, Long userId) {
        // Fetch the DeletedJob entity
        DeletedJob deletedJob = deletedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("DeletedJob", "jobId", jobId));

        // Fetch the associated PrintJob
        PrintJob printJob = deletedJob.getPrintJob();

        if (printJob == null) {
            throw new IllegalStateException("Associated PrintJob not found for DeletedJob with jobId: " + jobId);
        }

        // Restore the PrintJob status to its previous state
        PrintJobStatus previousStatus = deletedJob.getPreviousStatus();
        if (previousStatus == null) {
            throw new IllegalStateException("Previous status is null for DeletedJob with jobId: " + jobId);
        }

        printJob.setStatus(previousStatus);

        // Save the restored PrintJob
        printJobRepository.save(printJob);

        // Remove the DeletedJob entry
        deletedJobRepository.delete(deletedJob);
     // Log the action
        activityLogService.logActivity("DELETE_JOB", "System", userId, "Deleted job with ID: " + jobId);
        
        notifyJobRestoration(jobId, userId);

    }
    
    //public List<DeletedJobResponse> getDeletedJobsReport(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
      //  return deletedJobRepository.findDeletedJobSummaries(startDate, endDate, userId);
    //}
    
 // For Admin: Fetch all deleted jobs with filters
    public List<DeletedJobResponse> fetchDeletedJobsForAdmin(LocalDateTime startDate, LocalDateTime endDate, Long deletedByUserId) {
        return deletedJobRepository.findDeletedJobs(startDate, endDate, deletedByUserId)
                .stream()
                .map(this::mapToDeletedJobResponse)
                .collect(Collectors.toList());
    }
 // For User: Fetch only their deleted jobs
    public List<DeletedJobResponse> fetchDeletedJobsForUser(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return deletedJobRepository.findDeletedJobsByUser(userId, startDate, endDate)
                .stream()
                .map(this::mapToDeletedJobResponse)
                .collect(Collectors.toList());
    }

    private DeletedJobResponse mapToDeletedJobResponse(DeletedJob deletedJob) {
        return new DeletedJobResponse(
            deletedJob.getId(),
            deletedJob.getDeletedAt(),
            deletedJob.getDeletedBy().getUsername(), // Assuming deletedBy is a User object with a username
            deletedJob.getReasonForDeletion(),
            deletedJob.getPreviousStatus(),
            deletedJob.getRestorableUntil()
        );
    }

    
    public List<DeletedJobResponse> getDeletedJobsForAdmin(LocalDateTime startDate, LocalDateTime endDate) {
        return deletedJobRepository.findDeletedJobSummaries(startDate, endDate, null);
    }

    public List<DeletedJobResponse> getDeletedJobsForUser(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        return deletedJobRepository.findDeletedJobSummaries(startDate, endDate, userId);
    }

    
    public void notifyJobDeletion(Long jobId, Long userId, String reason) {
        PrintJob job = printJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String message = String.format(
            "Dear %s, your job with ID %d titled '%s' has been deleted. Reason: %s",
            user.getUsername(), job.getId(), job.getDescription(), reason
        );
        String emailRecipient = user.getEmail();
        String subject = "Notification: Your Job Has Been Deleted";

        // Send email notification
        emailNotificationService.sendEmailNotification(emailRecipient, subject, message);
        
        // Optionally send an in-app notification (if applicable)
        pushNotificationService.sendPushNotification(userId, "Job Deleted", message);
    }
    
    
    public void notifyJobRestoration(Long jobId, Long userId) {
        PrintJob job = printJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = String.format(
            "Dear %s, your job with ID %d titled '%s' has been restored successfully.",
            user.getUsername(), job.getId(), job.getDescription()
        );
        String emailRecipient = user.getEmail();
        String subject = "Notification: Your Job Has Been Restored";

        // Send email notification
        emailNotificationService.sendEmailNotification(emailRecipient, subject, message);
        
        // Optionally send an in-app notification (if applicable)
        pushNotificationService.sendPushNotification(userId, "Job Restored", message);
    }




}
