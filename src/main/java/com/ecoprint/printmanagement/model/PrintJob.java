package com.ecoprint.printmanagement.model;
 
import java.time.LocalDateTime;
 
import com.ecoprint.printmanagement.util.Base64Serializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
 
@Entity
@Table(name = "print_jobs") // Specifies the table name in the database
public class PrintJob {
 
    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") 
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user", nullable = false)  // The column name is 'user', not 'user_id'
    private User user;  // The user associated with this print job
 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;
 
    // File Details
    @NotNull(message = "File cannot be null.")
    @NotEmpty(message = "fileName must not be empty")
    @Column(name = "file_name", nullable = false) 
    private String fileName;
 
 
    @Column(name = "file_type", nullable = true)
    private String fileType;
 
    @Column(name = "file_size", nullable = true)
    private long fileSize;
 
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = true)
    @JsonSerialize(using = Base64Serializer.class) 
    private byte[] fileData;
 
    // Job Metadata
    @NotEmpty(message = "User name is required.")
    @Column(name = "user_name", nullable = false)
    private String userName;
 
    @Column(name = "upload_timestamp", nullable = true)
    private LocalDateTime uploadTimestamp;
    @Column(name = "description", length = 500)
    private String description;
 
    // Queue Management
    @Column
    private Integer queuePosition;
 
 
    public Integer getQueuePosition() {
		return queuePosition;
	}

	public void setQueuePosition(Integer queuePosition) {
		this.queuePosition = queuePosition;
	}

	// Print Details
    @NotNull(message = "Pages printed is mandatory")
    @Min(value = 1, message = "Pages printed must be at least 1")
    @Column(name="pages_printed", nullable = false)
    private int pagesPrinted;
 
    @Column(name="cost", nullable = true)
    private double cost;
 
    // Job Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PrintJobStatus status;
 
    // Timestamps for Job Statuses
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
 
    @Column(name = "queued_at")
    private LocalDateTime queuedAt;
 
    @Column(name = "paused_at")
    private LocalDateTime pausedAt;
 
    @Column(name = "ready_at")
    private LocalDateTime readyAt;
 
    @Column(name = "printing_at")
    private LocalDateTime printingAt;
 
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
 
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
 
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
 
    @Column(name = "favorite_at")
    private LocalDateTime favoriteAt;
    
 
    // Constructors
    public PrintJob() {}
 
    // Getters and Setters
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
 
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
 
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
 
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
 
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
 
    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
 
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
 
    public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }
 
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  
 
    public int getPagesPrinted() { return pagesPrinted; }
    public void setPagesPrinted(int pagesPrinted) { this.pagesPrinted = pagesPrinted; }
 
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
 
    public PrintJobStatus getStatus() { return status; }
    public void setStatus(PrintJobStatus status) { this.status = status; }
 
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
 
    public LocalDateTime getQueuedAt() { return queuedAt; }
    public void setQueuedAt(LocalDateTime queuedAt) { this.queuedAt = queuedAt; }
 
    public LocalDateTime getPausedAt() { return pausedAt; }
    public void setPausedAt(LocalDateTime pausedAt) { this.pausedAt = pausedAt; }
 
    public LocalDateTime getReadyAt() { return readyAt; }
    public void setReadyAt(LocalDateTime readyAt) { this.readyAt = readyAt; }
 
    public LocalDateTime getPrintingAt() { return printingAt; }
    public void setPrintingAt(LocalDateTime printingAt) { this.printingAt = printingAt; }
 
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
 
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
 
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
 
    public LocalDateTime getFavoriteAt() { return favoriteAt; }
    public void setFavoriteAt(LocalDateTime favoriteAt) { this.favoriteAt = favoriteAt; }
// Convenience method to get userId from the User object
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
 
    // Convenience method to set User by userId (requires a User object)
    public void setUserId(Long userId) {
        if (user == null) {
            user = new User();
        }
        user.setId(userId);  // Assuming User has a setId method
    }
 
    public Priority getPriority() {
        return priority;
    }
 
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

	    
 
}
