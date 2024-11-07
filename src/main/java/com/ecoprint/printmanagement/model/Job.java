package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime submittedAt;
    private LocalDateTime queuedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime readyAt;
    private LocalDateTime printingAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private LocalDateTime deletedAt;
    private LocalDateTime favoritedAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobHistory> history;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "print_job_id")
    private PrintJob printJob;  // Link to PrintJob

    // Getters and Setters
    
   

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
        updateTimestamps(status);
    }

   

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(LocalDateTime queuedAt) {
        this.queuedAt = queuedAt;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(LocalDateTime pausedAt) {
        this.pausedAt = pausedAt;
    }

    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(LocalDateTime readyAt) {
        this.readyAt = readyAt;
    }

    public LocalDateTime getPrintingAt() {
        return printingAt;
    }

    public void setPrintingAt(LocalDateTime printingAt) {
        this.printingAt = printingAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }

    public List<JobHistory> getHistory() {
        return history;
    }

    public void setHistory(List<JobHistory> history) {
        this.history = history;
    }

    public PrintJob getPrintJob() {
        return printJob;
    }

    public void setPrintJob(PrintJob printJob) {
        this.printJob = printJob;
    }
    
    

    // Method to update timestamps based on status
    private void updateTimestamps(JobStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case SUBMITTED:
                this.submittedAt = now;
                break;
            case QUEUED:
                this.queuedAt = now;
                break;
            case PAUSED:
                this.pausedAt = now;
                break;
            case READY:
                this.readyAt = now;
                break;
            case PRINTING:
                this.printingAt = now;
                break;
            case COMPLETED:
                this.completedAt = now;
                break;
            case FAILED:
                this.failedAt = now;
                break;
            case DELETED:
                this.deletedAt = now;
                break;
            case FAVORITE:
                this.favoritedAt = now;
                break;
            default:
                break;
        }
    }
}
