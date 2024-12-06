package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "print_event") 
public class PrintEvent {
	
	
    @Id
    @Column(name = "job_id", nullable = false, unique = true)
    private Long jobId;

    @Column(name = "printer_ip", nullable = true)
    private String printerIp;
    
    @Column(nullable = false)
    private int printerPort;

    
    
    
    @NotNull
    @Column(name = "file_name")
    private String fileName;
    
    @Column(nullable = true)
    private String status; // e.g., PRINTING, PAUSED, COMPLETED, FAILED

    @Column
    private int totalPages;

    @Column
    private int pagesPrinted;
    
    @Column(name = "completed_pages", nullable = true)
    private int completedPages;
    
    @Column(name = "progress_percentage", nullable = true)
    private double progressPercentage;
    
    @Column(name = "estimated_time_remaining")
    private String estimatedTimeRemaining;
      
    
    @Column(name = "last_updated", nullable = true)
    private LocalDateTime lastUpdated;


    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    @Column(name = "error_message", nullable = true)
    private String errorMessage;



	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}


	public String getPrinterIp() {
		return printerIp;
	}

	public void setPrinterIp(String printerIp) {
		this.printerIp = printerIp;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public int getPrinterPort() {
		return printerPort;
	}

	public void setPrinterPort(int printerPort) {
		this.printerPort = printerPort;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getPagesPrinted() {
		return pagesPrinted;
	}

	public void setPagesPrinted(int pagesPrinted) {
		this.pagesPrinted = pagesPrinted;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getCompletedPages() {
		return completedPages;
	}

	public void setCompletedPages(int completedPages) {
		this.completedPages = completedPages;
	}

	public double getProgressPercentage() {
		return progressPercentage;
	}

	public void setProgressPercentage(double progressPercentage) {
		this.progressPercentage = progressPercentage;
	}

	public String getEstimatedTimeRemaining() {
		return estimatedTimeRemaining;
	}

	public void setEstimatedTimeRemaining(String estimatedTimeRemaining) {
		this.estimatedTimeRemaining = estimatedTimeRemaining;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	
	
    


	   }
