package com.ecoprint.printmanagement.responses;

import java.time.LocalDateTime;

public class FailedJobResponse {
	
    private Long id;                // ID of the failed job
    private Long printJobId;        // Associated print job ID
    private String failureReason;   // Reason for the failure
    private String errorDetails;    // Detailed description of the failure
    private String failedBy;        // User/system responsible for the failure
    private LocalDateTime failedAt; // Timestamp when the job failed
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPrintJobId() {
		return printJobId;
	}
	public void setPrintJobId(Long printJobId) {
		this.printJobId = printJobId;
	}
	public String getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
	public String getErrorDetails() {
		return errorDetails;
	}
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
	public String getFailedBy() {
		return failedBy;
	}
	public void setFailedBy(String failedBy) {
		this.failedBy = failedBy;
	}
	public LocalDateTime getFailedAt() {
		return failedAt;
	}
	public void setFailedAt(LocalDateTime failedAt) {
		this.failedAt = failedAt;
	}
    
    
    
  


}
