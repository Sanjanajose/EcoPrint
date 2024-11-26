package com.ecoprint.printmanagement.dto;

import java.time.LocalDateTime;

public class DeletedJobResponse {

    private Long id;
    private LocalDateTime deletedAt;
    private String deletedByUsername;
    private String reasonForDeletion;
    private String fileName;
    

    // Constructor
    public DeletedJobResponse(Long id, LocalDateTime deletedAt, String reasonForDeletion, String fileName, String deletedByUsername) {
        this.id = id;
        this.deletedAt = deletedAt;
        this.reasonForDeletion = reasonForDeletion;
        this.fileName = fileName;
        this.deletedByUsername = deletedByUsername;
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
    
    

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
