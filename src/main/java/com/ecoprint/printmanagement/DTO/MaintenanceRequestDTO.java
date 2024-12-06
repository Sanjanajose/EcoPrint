package com.ecoprint.printmanagement.dto;

import com.ecoprint.printmanagement.model.MaintenanceStatus;

public class MaintenanceRequestDTO {

    private Long id;
    private String description;
    private String printerName; // Example for printer name
    private UserDTO submittedBy; // Consistent as UserDTO
    private MaintenanceStatus status;

    // Constructor with full details
    public MaintenanceRequestDTO(Long id, String description, String printerName, UserDTO submittedBy, MaintenanceStatus status) {
        this.id = id;
        this.description = description;
        this.printerName = printerName;
        this.submittedBy = submittedBy; // Directly assign UserDTO
        this.status = status;
    }

    // Constructor for minimal details
    public MaintenanceRequestDTO(Long id, String description, MaintenanceStatus status) {
        this.id = id;
        this.description = description;
        this.status = status;
    }

	// Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public UserDTO getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UserDTO submittedBy) {
        this.submittedBy = submittedBy;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }
}
