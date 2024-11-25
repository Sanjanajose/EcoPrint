package com.ecoprint.printmanagement.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class FailedJobDTO {

    @NotNull(message = "Job ID is required.")
    private Long jobId; // The ID of the failed print job

    @NotEmpty(message = "Failure reason is required.")
    private String failureReason; // A description of why the job failed

    //private String printerName; // (Optional) Name of the printer where the failure occurred

    private String printerStatus; // (Optional) Status of the printer (e.g., "Offline", "Busy")

    private int retryCount; // Number of retries attempted (optional for requests)

    private String printerName;
    
    private Long newPrinterId;

    
    // Getters and Setters
    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public String getPrinterStatus() {
        return printerStatus;
    }

    public void setPrinterStatus(String printerStatus) {
        this.printerStatus = printerStatus;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public Long getNewPrinterId() {
        return newPrinterId;
    }

    public void setNewPrinterId(Long newPrinterId) {
        this.newPrinterId = newPrinterId;
    }

}
