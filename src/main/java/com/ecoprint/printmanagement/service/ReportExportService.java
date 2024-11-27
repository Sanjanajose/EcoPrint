package com.ecoprint.printmanagement.service;

import org.springframework.stereotype.Service;
import com.ecoprint.printmanagement.dto.DeletedJobResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportExportService {

    // Generates a CSV string for the given list of DeletedJobResponse objects
    public String generateCSVReport(List<DeletedJobResponse> deletedJobResponses) {
        StringBuilder csvBuilder = new StringBuilder();
        
        // Header row
        csvBuilder.append("Job ID,Deleted By,Deleted At,Reason,Previous Status,Restorable Until\n");

        // Data rows
        for (DeletedJobResponse job : deletedJobResponses) {
            csvBuilder.append(String.format(
                "%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                job.getId(),
                escapeCsv(job.getDeletedByUsername()), // Escape commas in text
                job.getDeletedAt(),
                escapeCsv(job.getReasonForDeletion()), // Escape commas in text
                job.getPreviousStatus(),
                job.getRestorableUntil()
            ));
        }

        return csvBuilder.toString();
    }

    // Generates a byte array of the CSV, ready for export
    public byte[] generateDeletedJobsReport(List<DeletedJobResponse> deletedJobs) {
        String csvContent = generateCSVReport(deletedJobs);
        return csvContent.getBytes(StandardCharsets.UTF_8);
    }

    // Utility method to escape commas or special characters in CSV fields
    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        // Escape double quotes by doubling them, wrap the field in double quotes if it contains a comma
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
