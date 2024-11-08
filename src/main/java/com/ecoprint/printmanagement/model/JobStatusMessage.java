package com.ecoprint.printmanagement.model;

import com.ecoprint.printmanagement.model.PrintJobStatus;

public class JobStatusMessage {

    private Long jobId;
    private PrintJobStatus status;
    private String comments;

    // Default constructor
    public JobStatusMessage() {
    }

    // Parameterized constructor
    public JobStatusMessage(Long jobId, PrintJobStatus status, String comments) {
        this.jobId = jobId;
        this.status = status;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public PrintJobStatus getStatus() {
        return status;
    }

    public void setStatus(PrintJobStatus status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "JobStatusMessage{" +
                "jobId=" + jobId +
                ", status=" + status +
                ", comments='" + comments + '\'' +
                '}';
    }
}
