package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "deleted_jobs")
public class DeletedJob {

    @Id
    private Long id; // This will be the same as the PrintJob id.

    @OneToOne
    @MapsId
    @JoinColumn(name = "job_id", referencedColumnName = "id", nullable = false)
    private PrintJob printJob;
    

    @ManyToOne
    @JoinColumn(name = "deleted_by", nullable = false)
    private User deletedBy;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "reason_for_deletion", length = 255)
    private String reasonForDeletion;

    @Column(name = "restorable_until")
    private LocalDateTime restorableUntil;

    @Column(name = "restored_status", nullable = false)
    private Boolean restoredStatus = false;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PrintJobStatus previousStatus;


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PrintJob getPrintJob() {
        return printJob;
    }

    public void setPrintJob(PrintJob printJob) {
        this.printJob = printJob;
    }

    public User getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(User deletedBy) {
        this.deletedBy = deletedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getReasonForDeletion() {
        return reasonForDeletion;
    }

    public void setReasonForDeletion(String reasonForDeletion) {
        this.reasonForDeletion = reasonForDeletion;
    }

    public LocalDateTime getRestorableUntil() {
        return restorableUntil;
    }

    public void setRestorableUntil(LocalDateTime restorableUntil) {
        this.restorableUntil = restorableUntil;
    }

    public Boolean getRestoredStatus() {
        return restoredStatus;
    }

    public void setRestoredStatus(Boolean restoredStatus) {
        this.restoredStatus = restoredStatus;
    }
    
    // Getter and Setter for previousStatus
    public PrintJobStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PrintJobStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

}
