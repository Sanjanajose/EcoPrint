package com.ecoprint.printmanagement.dto;

import java.time.LocalDateTime;

public class QueuedJobDTO {
	
	
    private Long jobId; // ID from the print_jobs table
    private String documentName; // Corresponds to file_name in the print_jobs table
    private Long userId; // ID of the user who submitted the job
    private String userName; // Name of the user who submitted the job
    private Long printerId; // ID of the printer assigned
    private int pagesPrinted; // Number of pages in the document
    private int numCopies; // Number of copies requested
    private LocalDateTime uploadTimestamp; // Timestamp when the file was uploaded
    private LocalDateTime submissionTimestamp; // Timestamp when the job was submitted
    private String jobPriority; // Priority of the job (LOW, MEDIUM, HIGH, URGENT)
    private Integer queuePosition; // Position in the queue
    private String status; // Status of the job (e.g., QUEUED, PRINTING, COMPLETED)
    
    // Getters and Setters
    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getPrinterId() {
        return printerId;
    }

    public void setPrinterId(Long printerId) {
        this.printerId = printerId;
    }

    public int getPagesPrinted() {
        return pagesPrinted;
    }

    public void setPagesPrinted(int pagesPrinted) {
        this.pagesPrinted = pagesPrinted;
    }


    public int getNumCopies() {
        return numCopies;
    }

    public void setNumCopies(int numCopies) {
        this.numCopies = numCopies;
    }

    public LocalDateTime getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public String getJobPriority() {
        return jobPriority;
    }

    public void setJobPriority(String jobPriority) {
        this.jobPriority = jobPriority;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
   
   

}
