package com.ecoprint.printmanagement.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
@Entity
public class Printer {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "printer_name", nullable = false)
    private String name;

    @Column(name = "printer_model")
    private String model;

    @Column(name = "ip_address", unique = true, nullable = false)
    private String ipAddress;

    @Column(name = "status", nullable = false)
    private String status; // E.g., ONLINE, OFFLINE, MAINTENANCE

    @Column(name = "location")
    private String location; // Location of the printer (e.g., Office Floor 1)

    // Relationship with PrintJob
    @OneToMany(mappedBy = "printer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PrintJob> printJobs;
    

    // Relationship with FailedJob
    @OneToMany(mappedBy = "printer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FailedJob> failedJobs;

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<PrintJob> getPrintJobs() {
        return printJobs;
    }

    public void setPrintJobs(List<PrintJob> printJobs) {
        this.printJobs = printJobs;
    }

    public List<FailedJob> getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(List<FailedJob> failedJobs) {
        this.failedJobs = failedJobs;
    }
}
