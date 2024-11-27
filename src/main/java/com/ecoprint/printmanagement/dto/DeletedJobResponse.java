package com.ecoprint.printmanagement.dto;

import java.time.LocalDateTime;

import com.ecoprint.printmanagement.model.PrintJobStatus;

public class DeletedJobResponse {

    private Long id;
    private LocalDateTime deletedAt;
    private String deletedByUsername;
    private String reasonForDeletion;
    private PrintJobStatus previousStatus;
    private LocalDateTime restorableUntil;

    public DeletedJobResponse(Long id, LocalDateTime deletedAt, String deletedByUsername, String reasonForDeletion, PrintJobStatus previousStatus, LocalDateTime restorableUntil) {
        this.id = id;
        this.deletedAt = deletedAt;
        this.deletedByUsername = deletedByUsername;
        this.reasonForDeletion = reasonForDeletion;
        this.previousStatus = previousStatus;
        this.restorableUntil = restorableUntil;
    }


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedByUsername() {
        return deletedByUsername;
    }

    public void setDeletedByUsername(String deletedByUsername) {
        this.deletedByUsername = deletedByUsername;
    }

    public String getReasonForDeletion() {
        return reasonForDeletion;
    }

    public void setReasonForDeletion(String reasonForDeletion) {
        this.reasonForDeletion = reasonForDeletion;
    }
    
 // Getter for previousStatus
    public PrintJobStatus getPreviousStatus() { 
        return previousStatus; 
    }

    // Setter for previousStatus
    public void setPreviousStatus(PrintJobStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    // Getter for restorableUntil
    public LocalDateTime getRestorableUntil() {
        return restorableUntil; 
    }

    // Setter for restorableUntil
    public void setRestorableUntil(LocalDateTime restorableUntil) {
        this.restorableUntil = restorableUntil;
    }

}
