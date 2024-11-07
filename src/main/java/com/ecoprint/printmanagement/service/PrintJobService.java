package com.ecoprint.printmanagement.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.Tika;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.repository.PrintJobRepository;

@Service
public class PrintJobService {

    private final PrintJobRepository printJobRepository;
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

        // Validate file type
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
        printJob.setFileData(fileData);
        printJob.setDescription(description);

        // Save the PrintJob entity to the database and return it
        return printJobRepository.save(printJob);
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
    
  /*  
    // Additional overloaded method to retrieve by filename if needed
    public Resource downloadFileByFileName(String fileName) {
        //PrintJob printJob = printJobService.findPrintJobById(id);

        PrintJob printJob = printJobRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found with name: " + fileName));
        return new ByteArrayResource(printJob.getFileData());
    }
*/

    
   
}
