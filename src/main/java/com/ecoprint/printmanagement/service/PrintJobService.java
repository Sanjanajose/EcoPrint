package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.Job;
import com.ecoprint.printmanagement.model.JobStatus;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class PrintJobService {

    @Autowired
    private PrintJobRepository printJobRepository;

    @Autowired
    private JobService jobService;  // Inject JobService for job creation

    private final Tika tika = new Tika(); // For MIME type detection
    private final long maxSize = 5 * 1024 * 1024; // Example: 5 MB size limit

    public PrintJobService(PrintJobRepository printJobRepository) {
        this.printJobRepository = printJobRepository;
    }

    public PrintJob uploadFile(MultipartFile file, String userName, String description) throws IOException {
        // Validate file size
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of " + (maxSize / (1024 * 1024)) + " MB");
        }

        // Validate file type using Tika
        String fileType = tika.detect(file.getInputStream());
        validateFileType(fileType);

        // Convert file content to byte array for database storage
        byte[] fileData = file.getBytes();

        // Create and populate PrintJob entity with metadata and file data
        PrintJob printJob = new PrintJob();
        printJob.setFileName(file.getOriginalFilename());
        printJob.setFileType(fileType);
        printJob.setFileSize(file.getSize());
        printJob.setUserName(userName);
        printJob.setUploadTimestamp(LocalDateTime.now());
        printJob.setFileData(fileData);  // Set the file data for database storage
        printJob.setDescription(description);  // Set the description

        // Create Job object for the PrintJob
        Job job = new Job();
        job.setStatus(JobStatus.QUEUED);  // Example of setting the job status as PENDING
        job.setPrintJob(printJob);  // Associate the PrintJob with the Job

        // Use JobService to create the job related to this PrintJob
        job = jobService.createJob(job);  // Create job entry

        // Save the PrintJob entity to the database
        printJobRepository.save(printJob);

        // Return the created PrintJob
        return printJob;
    }

    private void validateFileType(String fileType) {
        // List of allowed MIME types
        List<String> allowedFileTypes = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "image/jpeg",
            "image/png",
            "image/tiff",
            "image/bmp",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv"
        );

        if (!allowedFileTypes.contains(fileType)) {
            throw new IllegalArgumentException("File type not supported");
        }
    }

    // Method to retrieve file data as Resource by ID
    public Resource downloadFile(Long id) {
        PrintJob printJob = findPrintJobById(id); // Fetches the PrintJob entity
        return new ByteArrayResource(printJob.getFileData()); // Returns file data as Resource
    }

    // Method to retrieve PrintJob metadata by ID
    public PrintJob findPrintJobById(Long id) {
        return printJobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + id));
    }
}
