package com.ecoprint.printmanagement.service;

import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.dto.DeletedJobResponse;
import com.ecoprint.printmanagement.model.DeletedJob;

import java.util.List; // Ensure this is included

@Service
public class ReportExportService {
	public String generateCSVReport(List<DeletedJobResponse> deletedJobResponses) {
	    StringBuilder csv = new StringBuilder("Job ID,Deleted By,Deleted At,Reason\n");

	    for (DeletedJobResponse job : deletedJobResponses) {
	        csv.append(job.getId()).append(",")  // Assuming getId() fetches the Job ID
	           .append("\"").append(job.getDeletedByUsername()).append("\"").append(",") // Assuming getDeletedBy() returns the username
	           .append(job.getDeletedAt()).append(",") // Assuming getDeletedAt() returns the timestamp
	           .append("\"").append(job.getReasonForDeletion()).append("\"").append("\n"); // Assuming getReasonForDeletion() is present
	    }

	    return csv.toString(); // Return the generated CSV as a string
	}


}
