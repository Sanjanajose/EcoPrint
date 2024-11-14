package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "job_history")
public class JobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "print_job_id", nullable = false)
    private Long printJobId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = true)
    private PrintJobStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "updated_status", nullable = false) // This ensures the column is NOT NULL in the DB
    private PrintJobStatus updatedStatus;



    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "comments")
    private String comments;
    
    // New fields for tracking position changes
    @Column(name = "previous_position")
    private Integer previousPosition;

    @Column(name = "new_position")
    private Integer newPosition;


    // Constructors
    public JobHistory() {}

    public JobHistory(Long printJobId, Long userId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus, LocalDateTime timestamp, String comments) {
        this.printJobId = printJobId;
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.updatedStatus = updatedStatus;
        this.timestamp = timestamp;
        this.comments = comments;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrintJobId() {
        return printJobId;
    }

    public void setPrintJobId(Long printJobId) {
        this.printJobId = printJobId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PrintJobStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(PrintJobStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public PrintJobStatus getUpdatedStatus() {
        return updatedStatus;
    }

    public void setUpdatedStatus(PrintJobStatus updatedStatus) {
        this.updatedStatus = updatedStatus;
    }
    
    

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    public Integer getPreviousPosition() {
        return previousPosition;
    }

    public void setPreviousPosition(Integer previousPosition) {
        this.previousPosition = previousPosition;
    }

    public Integer getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(Integer newPosition) {
        this.newPosition = newPosition;
    }
}
