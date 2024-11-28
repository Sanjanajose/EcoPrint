package com.ecoprint.printmanagement.model;

import java.util.Date;

public class PrintProcess {
	
    private long jobId;
    private String status;
    private String printerId;
    private Date startTime;
    private Date updateTime;    
    
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
         

}
