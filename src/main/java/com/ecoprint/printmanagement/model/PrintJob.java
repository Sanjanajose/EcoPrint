package com.ecoprint.printmanagement.model;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "print_jobs") // Specifies the table name in the database
public class PrintJob {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Specifies the column name in the database
    private Long id;

    public PrintJob() {
		
	}

	@Column(name = "file_name", nullable = false) // Customize column name and make it non-nullable
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "upload_timestamp", nullable = false)
    private LocalDateTime uploadTimestamp;
    
    @Column(name = "description", length = 500)
    private String description;  // Field to store the job description

        
    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;  // Field to store the file content

    @NotNull(message = "Pages printed is mandatory")
    @Min(value = 1, message = "Pages printed must be at least 1")
    @Column(name="pages_printed", nullable = false)
    private int pagesPrinted;

    @Column(name="cost",nullable = false)
    private double cost;

    
    // Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
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

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

   

}
