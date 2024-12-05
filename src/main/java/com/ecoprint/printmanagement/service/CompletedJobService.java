package com.ecoprint.printmanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CompletedJob;
import com.ecoprint.printmanagement.repository.CompletedJobRepository;

@Service
public class CompletedJobService {

    @Autowired
    private CompletedJobRepository completedJobRepository;

    /**
     * Fetch all completed jobs (Admin only).
     */
    public List<CompletedJob> getAllCompletedJobs() {
        return completedJobRepository.findAll();
    }

    /**
     * Fetch a specific completed job by its ID (Admin or Job Owner).
     */
    public CompletedJob getCompletedJobById(Long jobId) {
        return completedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));
    }

    /**
     * Fetch all completed jobs for a specific user (Admin or Job Owner).
     */
    public List<CompletedJob> getCompletedJobsByUserId(Long userId) {
        return completedJobRepository.findByUserId(userId);
    }

    /**
     * Fetch all completed jobs for the currently logged-in user.
     */
    public List<CompletedJob> getMyCompletedJobs(Long currentUserId) {
        return completedJobRepository.findByUserId(currentUserId);
    }

    /**
     * Delete a specific completed job by its ID (Admin only).
     */
    public void deleteCompletedJob(Long jobId) {
        CompletedJob job = completedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));
        completedJobRepository.delete(job);
    }

    /**
     * Get the user ID associated with a completed job (for access checks).
     */
    public Long getUserIdByCompletedJobId(Long jobId) {
        return completedJobRepository.findById(jobId)
                .map(CompletedJob::getUserId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));
    }
}
