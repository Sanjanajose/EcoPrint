package com.ecoprint.printmanagement.dto;

import java.time.LocalDateTime;

public class DeletedJobResponse {

    private Long id;
    private LocalDateTime deletedAt;
    private String deletedByUsername;
    private String reasonForDeletion;

    // Constructor
    public DeletedJobResponse(Long id, LocalDateTime deletedAt, String deletedByUsername, String reasonForDeletion) {
        this.id = id;
        this.deletedAt = deletedAt;
        this.deletedByUsername = deletedByUsername;
        this.reasonForDeletion = reasonForDeletion;
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
}
