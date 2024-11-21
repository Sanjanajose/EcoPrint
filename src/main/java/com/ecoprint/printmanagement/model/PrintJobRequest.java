package com.ecoprint.printmanagement.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


public class PrintJobRequest  {

    private Long userId;

    private String description;
    
    
    
    @NotEmpty(message = "fileName must not be empty")
    private String fileName;
    
    private int pages;

    // Add the priority field with validation
    @JsonProperty("priority")
    private Priority priority;  // Priority field
    
    
    //@NotNull(message = "Submitter ID is required.")
    //private Long submitterId;

    @NotNull(message = "Color is required.")
    private Boolean color; // Indicates whether the print is color or black & white

    @NotNull(message = "Duplex is required.")
    private Boolean duplex; // Indicates whether the print is duplex or single-sided


    @NotEmpty(message = "Paper type must not be empty")
    private String paper; // Paper type (e.g., A4, A3, Letter)



    @NotEmpty(message = "Size must not be empty")
    private String size; // Paper size (e.g., small, medium, large)


 // No-args constructor
    public PrintJobRequest() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @JsonCreator
    public PrintJobRequest(
            @JsonProperty("userId") Long userId,
            @JsonProperty("description") String description,
            @JsonProperty("fileName") @NotEmpty(message = "fileName must not be empty") String fileName,
            @JsonProperty("pages") int pages,
            @JsonProperty("priority") Priority priority) {
        this.userId = userId;
        this.description = description;
        this.fileName = fileName;
        this.pages = pages;
        this.priority = priority;
    }
    
}