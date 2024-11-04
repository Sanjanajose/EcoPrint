package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Company_Audit_Log")
public class CompanyAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    // Default constructor
    public CompanyAuditLog() {
    }

    // Parameterized constructor
    public CompanyAuditLog(Long companyId, String action, String description, Long adminId) {
        this.companyId = companyId;
        this.action = action;
        this.description = description;
        this.adminId = adminId;
        this.timestamp = LocalDateTime.now(); // Setting the timestamp to the current time
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CompanyAuditLog{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", adminId=" + adminId +
                ", companyId=" + companyId +
                ", action='" + action + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
