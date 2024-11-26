package com.ecoprint.printmanagement.service;

import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.dto.DeletedJobResponse;
import com.ecoprint.printmanagement.model.DeletedJob;

import java.util.List; // Ensure this is included



@Service
public class ReportExportService {
	public String generateCSVReport(List<DeletedJobResponse> reportData) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Job ID,Deleted At,Reason for Deletion,File Name,Deleted By\n");

        for (DeletedJobResponse job : reportData) {
            csvBuilder.append(job.getId()).append(",")
                      .append(job.getDeletedAt()).append(",")
                      .append(job.getReasonForDeletion()).append(",")
                      .append(job.getFileName()).append(",")
                      .append(job.getDeletedByUsername()).append("\n");
        }

        return csvBuilder.toString();
    }

	
	
	
}
