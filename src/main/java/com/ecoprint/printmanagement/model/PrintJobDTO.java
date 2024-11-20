package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class PrintJobDTO {

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fileData);
		result = prime * result + Objects.hash(completedAt, cost, deletedAt, description, failedAt, favoriteAt,
				fileName, fileSize, fileType, id, pagesPrinted, pausedAt, printingAt, priority, queuePosition, queuedAt,
				readyAt, status, submittedAt, uploadTimestamp, userId, userName);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrintJobDTO other = (PrintJobDTO) obj;
		return Objects.equals(completedAt, other.completedAt)
				&& Double.doubleToLongBits(cost) == Double.doubleToLongBits(other.cost)
				&& Objects.equals(deletedAt, other.deletedAt) && Objects.equals(description, other.description)
				&& Objects.equals(failedAt, other.failedAt) && Objects.equals(favoriteAt, other.favoriteAt)
				&& Arrays.equals(fileData, other.fileData) && Objects.equals(fileName, other.fileName)
				&& fileSize == other.fileSize && Objects.equals(fileType, other.fileType)
				&& Objects.equals(id, other.id) && pagesPrinted == other.pagesPrinted
				&& Objects.equals(pausedAt, other.pausedAt) && Objects.equals(printingAt, other.printingAt)
				&& priority == other.priority && queuePosition == other.queuePosition
				&& Objects.equals(queuedAt, other.queuedAt) && Objects.equals(readyAt, other.readyAt)
				&& status == other.status && Objects.equals(submittedAt, other.submittedAt)
				&& Objects.equals(uploadTimestamp, other.uploadTimestamp) && Objects.equals(userId, other.userId)
				&& Objects.equals(userName, other.userName);
	}

}
