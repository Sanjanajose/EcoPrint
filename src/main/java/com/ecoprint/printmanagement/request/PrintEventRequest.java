package com.ecoprint.printmanagement.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrintEventRequest {
    @NotNull(message = "Printer IP cannot be null")
    @Size(min = 7, max = 15, message = "Printer IP must be a valid IPv4 address")
    private String printerIp;

    @NotNull(message = "Printer Port cannot be null")
    private Integer printerPort;

    @NotNull(message = "File Name cannot be null")
    @Size(min = 1, message = "File name must not be empty")
    private String fileName;

    private String filePath; // Optional if file path is needed
    
    private Integer totalPages; // Optional if total pages are calculated
    private Integer completedPages; // Optional for tracking print progress
    private String status; // Optional for status like "PRINTING", "COMPLETED", etc.

    // Getters and Setters
    public String getPrinterIp() {
        return printerIp;
    }

    public void setPrinterIp(String printerIp) {
        this.printerIp = printerIp;
    }

    public Integer getPrinterPort() {
        return printerPort;
    }

    public void setPrinterPort(Integer printerPort) {
        this.printerPort = printerPort;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getCompletedPages() {
        return completedPages;
    }

    public void setCompletedPages(Integer completedPages) {
        this.completedPages = completedPages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
