package com.ecoprint.printmanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class JobProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long jobId;             // Unique identifier for the job
    private int completedPages;     // Number of pages completed
    private int totalPages;         // Total number of pages in the job
    private long startTime;         // Timestamp when the job started (in milliseconds)
    private long lastUpdatedTime;   // Timestamp of the last progress update (in milliseconds)
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public int getCompletedPages() {
		return completedPages;
	}
	public void setCompletedPages(int completedPages) {
		this.completedPages = completedPages;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}
	public void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

    
    public int getProgressPercentage() {
        if (totalPages == 0) {
            return 0; // Avoid division by zero
        }
        return (completedPages * 100) / totalPages;
    }

    // Calculate estimated time remaining (in seconds)
    public long getEstimatedTimeRemaining() {
        if (completedPages == 0) {
            return -1; // Can't calculate if no pages are completed yet
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        double averageTimePerPage = (double) elapsedTime / completedPages;
        int remainingPages = totalPages - completedPages;

        return (long) (averageTimePerPage * remainingPages / 1000); // Convert milliseconds to seconds
    }

    // Update progress
    public void updateProgress(int additionalPages) {
        this.completedPages += additionalPages;
        this.lastUpdatedTime = System.currentTimeMillis(); // Update timestamp
    }

    
    public JobProgress(long jobId, int completedPages, int totalPages) {
        this.jobId = jobId;
        this.completedPages = completedPages;
        this.totalPages = totalPages;
    }


}
