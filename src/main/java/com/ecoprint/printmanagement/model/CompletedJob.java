package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "completed_jobs")
public class CompletedJob {

    @Id
    private Long jobId; // Match the ID of the print job

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PrintJobStatus status = PrintJobStatus.COMPLETED;
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "description")
    private String description;

    @Column(name = "pages_printed")
    private Integer pagesPrinted;

    
    @Column(name = "printer_name", nullable = false)
    private String printerName;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
    
 

    public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public PrintJobStatus getStatus() {
		return status;
	}

	public void setStatus(PrintJobStatus status) {
		this.status = status;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPagesPrinted() {
		return pagesPrinted;
	}

	public void setPagesPrinted(Integer pagesPrinted) {
		this.pagesPrinted = pagesPrinted;
	}

	

}
