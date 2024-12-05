package com.ecoprint.printmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queued_jobs")
public class QueuedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId; // Primary Key, corresponds to job_id in print_jobs

    @Column(nullable = false)
    private String documentName; // Document name (file name)

    @Column(nullable = false)
    private Long userId; // Foreign key referencing user

    @Column(nullable = false)
    private Long printerId; // Foreign key referencing printer

    @Column(nullable = false)
    private int pagesPrinted; // Total pages in the document

    @Column(nullable = false)
    private int numCopies; // Number of copies requested

    @Column(nullable = false, updatable = false)
    private LocalDateTime submissionTimestamp = LocalDateTime.now(); // Timestamp of submission

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority jobPriority = Priority.MEDIUM; // Job priority (LOW, MEDIUM, HIGH, URGENT)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrintJobStatus status = PrintJobStatus.QUEUED; // Status of the print job
    
    @Column(name = "queue_position", nullable = true)
    private Integer queuePosition; // Change from int to Integer
    @OneToOne
    @MapsId
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private PrintJob printJob;

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

    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }

    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }

    public Priority getJobPriority() {
        return jobPriority;
    }

    public void setJobPriority(Priority jobPriority) {
        this.jobPriority = jobPriority;
    }

    public PrintJobStatus getStatus() {
        return status;
    }

    public void setStatus(PrintJobStatus status) {
        this.status = status;
    }
    
    
    public PrintJob getPrintJob() {
        return printJob;
    }

    // Setter
    public void setPrintJob(PrintJob printJob) {
        this.printJob = printJob;
    }
    
    // Getter and Setter for queuePosition
    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
}
