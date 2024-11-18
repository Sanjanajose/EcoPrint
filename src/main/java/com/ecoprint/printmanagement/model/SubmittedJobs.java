package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.Data;

@Data
@Entity
@Table(name = "submitted_Jobs") // Specifies the table name in the database
public class SubmittedJobs {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") 
    private Long id;

    // Link to User entity for owner information
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // File Details
    @NotNull(message = "File cannot be null.")
    @Column(name = "file_name", nullable = false) 
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;

    // Job Metadata
    @NotEmpty(message = "User name is required.")
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "upload_timestamp", nullable = false)
    private LocalDateTime uploadTimestamp;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @NotNull(message = "Pages printed is mandatory")
    @Min(value = 1, message = "Pages printed must be at least 1")
    @Column(name="pages_printed", nullable = false)
    private int pagesPrinted;

    
    
       
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private PrintJobStatus status;


    // Add a default value
    public SubmittedJobs() {
    }

    // Getter and Setter


	public Long getId() {
		return id;
	}

	public PrintJobStatus getStatus() {
		return status;
	}

	public void setStatus(PrintJobStatus status) {
		this.status = status;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public int getPagesPrinted() {
		return pagesPrinted;
	}

	public void setPagesPrinted(int pagesPrinted) {
		this.pagesPrinted = pagesPrinted;
	}
  

}
