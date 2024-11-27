package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "failed_jobs")
public class FailedJob {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;

    @Column(name = "error_details", nullable = false)
    private String errorDetails;

    @Column(name = "failed_by")
    private String failedBy;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;
    
    @Column(name = "failure_reason")
    private String failureReason;

    
    
    @ManyToOne
    @JoinColumn(name = "print_job_id", nullable = false)
    private PrintJob printJob;

    
   
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "old_printer_id")
    private Printer printer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_printer_id")
    private Printer newPrinter;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public LocalDateTime getFailedAt() {
		return failedAt;
	}

	public void setFailedAt(LocalDateTime failedAt) {
		this.failedAt = failedAt;
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

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public PrintJob getPrintJob() {
		return printJob;
	}

	public void setPrintJob(PrintJob printJob) {
		this.printJob = printJob;
	}

	public Printer getPrinter() {
		return printer;
	}

	public void setPrinter(Printer printer) {
		this.printer = printer;
	}

	public Printer getNewPrinter() {
		return newPrinter;
	}

	public void setNewPrinter(Printer newPrinter) {
		this.newPrinter = newPrinter;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	

    

}
