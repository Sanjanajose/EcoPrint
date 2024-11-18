package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
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

    
    @NotNull
    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "comments")
    private String comments;
    

    @Column(name = "user_name", nullable = true)
    private String userName;

    // New fields for tracking position changes
    @Column(name = "previous_position")
    private Integer previousPosition;

    @Column(name = "new_position")
    private Integer newPosition;


    @Column(name = "file_size")
    private Long fileSize;  // This will store the size of the file


    // Constructors
    public JobHistory() {}


 

    public JobHistory(Long printJobId, Long userId, PrintJobStatus previousStatus, PrintJobStatus updatedStatus, LocalDateTime timestamp, String comments,String userName) {

        this.printJobId = printJobId;
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.updatedStatus = updatedStatus;
        this.timestamp = timestamp;
        this.comments = comments;
        this.userName=userName;
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


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

}
