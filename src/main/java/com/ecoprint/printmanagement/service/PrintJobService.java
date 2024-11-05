package com.ecoprint.printmanagement.service;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.repository.PrintJobRepository;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
public class PrintJobService {

    private final Path uploadDir = Paths.get("uploads");
    private final Tika tika = new Tika();

    @Value("${file.max-size}")
    private long maxSize; // Maximum file size, injected from application.properties

    private final PrintJobRepository printJobRepository;

    public PrintJobService(PrintJobRepository printJobRepository) throws IOException {
        this.printJobRepository = printJobRepository;

        // Ensure the upload directory exists
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public void uploadFile(MultipartFile file, String userName) throws IOException {
        // Validate file size and content before processing
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds limit");
        }

        // Validate file type
        String fileType = tika.detect(file.getInputStream());
        validateFileType(fileType);

        // Define the file path where it will be saved
        String fileName = file.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        // Save the file to the specified path
        Files.copy(file.getInputStream(), filePath);

        // Create a PrintJob entity and set its metadata
        PrintJob printJob = new PrintJob();
        printJob.setFileName(fileName);
        printJob.setFileType(fileType);
        printJob.setFileSize(file.getSize());
        printJob.setUserName(userName);
        printJob.setUploadTimestamp(LocalDateTime.now());

        // Save metadata to the database
        saveFileMetadata(printJob);
    }

    // Validate allowed file types
    private void validateFileType(String fileType) {
        // Example: Only allow PDF and DOCX files
        if (!fileType.equals("application/pdf") && 
            !fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new IllegalArgumentException("File type not supported");
        }
    }    

    // Save metadata to the database
    private void saveFileMetadata(PrintJob printJob) {
        printJobRepository.save(printJob);
    }
}
