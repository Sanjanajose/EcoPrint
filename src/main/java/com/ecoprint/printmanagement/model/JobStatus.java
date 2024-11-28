package com.ecoprint.printmanagement.model;

import java.util.Date;

public class JobStatus {
	
    private long jobId;       // Unique identifier for the job
    private String status;    // Current status of the job (e.g., PRINTING, PAUSED, COMPLETED)
    private String printerId; // Logical identifier for the printer handling the job
    private Date startTime;   // Timestamp when the job started
    private Date updateTime;  // Timestamp of the last status update
    
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPrinterId() {
		return printerId;
	}
	public void setPrinterId(String printerId) {
		this.printerId = printerId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
   
    public JobStatus(long jobId, String status) {
        this.jobId = jobId;
        this.status = status;
    }

    
    public JobStatus(long jobId, String status, String printerId, Date startTime, Date updateTime) {
        this.jobId = jobId;
        this.status = status;
        this.printerId = printerId;
        this.startTime = startTime;
        this.updateTime = updateTime;
    }

 


}
