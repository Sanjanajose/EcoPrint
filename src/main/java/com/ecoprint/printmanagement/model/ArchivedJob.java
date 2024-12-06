package com.ecoprint.printmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "archived_jobs")
public class ArchivedJob {

    @Id
    private Long jobId; // Same ID as CompletedJob

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "description")
    private String description;

    @Column(name = "pages_printed")
    private Integer pagesPrinted;

    @Column(name = "printer_name", nullable = false)
    private String printerName;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

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

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

    
}
