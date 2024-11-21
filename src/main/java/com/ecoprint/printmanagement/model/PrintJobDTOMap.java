package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import java.util.List;

public class PrintJobDTOMap {

	private Long id;

	private Long userId; // The user associated with this print job

	private Priority priority;

	private String fileName;

	private String fileType;

	private long fileSize;

	private byte[] fileData;

	private String userName;

	private LocalDateTime uploadTimestamp;

	private String description;

	private int queuePosition;

	private int pagesPrinted;

	private double cost;

	private PrintJobStatus status;

	private LocalDateTime submittedAt;

	private LocalDateTime queuedAt;

	private LocalDateTime pausedAt;

	private LocalDateTime readyAt;

	private LocalDateTime printingAt;

	private LocalDateTime completedAt;

	private LocalDateTime failedAt;

	private LocalDateTime deletedAt;

	private LocalDateTime favoriteAt;

	private List<PrintHistoryDTO> printHIstory;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public LocalDateTime getUploadTimestamp() {
		return uploadTimestamp;
	}

	public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
		this.uploadTimestamp = uploadTimestamp;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getQueuePosition() {
		return queuePosition;
	}

	public void setQueuePosition(int queuePosition) {
		this.queuePosition = queuePosition;
	}

	public int getPagesPrinted() {
		return pagesPrinted;
	}

	public void setPagesPrinted(int pagesPrinted) {
		this.pagesPrinted = pagesPrinted;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public PrintJobStatus getStatus() {
		return status;
	}

	public void setStatus(PrintJobStatus status) {
		this.status = status;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public LocalDateTime getQueuedAt() {
		return queuedAt;
	}

	public void setQueuedAt(LocalDateTime queuedAt) {
		this.queuedAt = queuedAt;
	}

	public LocalDateTime getPausedAt() {
		return pausedAt;
	}

	public void setPausedAt(LocalDateTime pausedAt) {
		this.pausedAt = pausedAt;
	}

	public LocalDateTime getReadyAt() {
		return readyAt;
	}

	public void setReadyAt(LocalDateTime readyAt) {
		this.readyAt = readyAt;
	}

	public LocalDateTime getPrintingAt() {
		return printingAt;
	}

	public void setPrintingAt(LocalDateTime printingAt) {
		this.printingAt = printingAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public LocalDateTime getFailedAt() {
		return failedAt;
	}

	public void setFailedAt(LocalDateTime failedAt) {
		this.failedAt = failedAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public LocalDateTime getFavoriteAt() {
		return favoriteAt;
	}

	public void setFavoriteAt(LocalDateTime favoriteAt) {
		this.favoriteAt = favoriteAt;
	}

	public List<PrintHistoryDTO> getPrintHIstory() {
		return printHIstory;
	}

	public void setPrintHIstory(List<PrintHistoryDTO> printHIstory) {
		this.printHIstory = printHIstory;
	}

}
