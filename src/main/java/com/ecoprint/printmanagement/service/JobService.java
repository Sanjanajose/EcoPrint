package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.Job;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.repository.JobHistoryRepository;
import com.ecoprint.printmanagement.repository.JobRepository;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    @Autowired
    private PrintJobRepository printJobRepository;  // Inject PrintJobRepository

    /**
     * Creates a new print job and associated job record.
     * 
     * @param printJob The print job to create.
     * @return The saved print job.
     */
    public PrintJob createPrintJob(PrintJob printJob) {
        // Create and save the print job
        return printJobRepository.save(printJob);
    }

    /**
     * Creates a new job with initial status SUBMITTED and logs the initial history entry.
     * 
     * @param job The job to create.
     * @return The saved job.
     */
    public Job createJob(final Job job) {
        job.setStatus(JobStatus.SUBMITTED);  // Set the initial status to SUBMITTED
        Job savedJob = jobRepository.save(job);  // Save the job

        // Log the initial job history with SUBMITTED status
        logJobHistory(savedJob, JobStatus.SUBMITTED);

        return savedJob;
    }

    /**
     * Fetches a job along with its complete history.
     * 
     * @param jobId The ID of the job.
     * @return The job along with its history.
     */
    public Job getJobWithHistory(final Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job with ID " + jobId + " not found"));

        // Fetch and set the job history
        List<JobHistory> history = jobHistoryRepository.findAllByJob_JobId(jobId);
        job.setHistory(history);

        return job;
    }

    /**
     * Updates the job status with validation and logs the status change in JobHistory.
     * 
     * @param jobId    The ID of the job to update.
     * @param newStatus The new status to set for the job.
     * @return The updated job.
     */
    @Transactional
    public Job updateJobStatus(final Long jobId, final JobStatus newStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job with ID " + jobId + " not found"));

        // Validate if the status transition is allowed
        validateStatusTransition(job.getStatus(), newStatus);

        // Update the job status
        job.setStatus(newStatus);
        jobRepository.save(job);

        // Log the status change in job history
        logJobHistory(job, newStatus); // Log the status change with the current timestamp

        return job;
    }

    /**
     * Validates if the job can transition from the current status to the new status.
     * 
     * @param currentStatus The current job status.
     * @param newStatus     The desired new job status.
     */
    private void validateStatusTransition(final JobStatus currentStatus, final JobStatus newStatus) {
        if (currentStatus == JobStatus.COMPLETED && newStatus != JobStatus.DELETED) {
            throw new IllegalStateException("Cannot change status from COMPLETED to another active state.");
        }
        // Add additional validation rules here as required
    }

    /**
     * Logs the job status change to JobHistory with the current timestamp.
     * 
     * @param job       The job whose status has changed.
     * @param newStatus The new status for the job.
     */
    private void logJobHistory(Job job, JobStatus newStatus) {
        JobHistory history = new JobHistory(job, newStatus, LocalDateTime.now());  // Create job history entry with timestamp
        jobHistoryRepository.save(history);  // Save the history entry
    }
}
