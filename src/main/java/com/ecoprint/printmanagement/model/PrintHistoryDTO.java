package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;

public class PrintHistoryDTO {
	
    private Long id;

    private Long printJobId;

    private Long userId;

    private PrintJobStatus previousStatus;

    private PrintJobStatus updatedStatus;

    private String actionType;

    private LocalDateTime timestamp;

    private String comments;
    
    private String userName;

    private Integer previousPosition;

    private Integer newPosition;

    private Long fileSize;  // This will store the size of the file

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

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
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

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
    
}
