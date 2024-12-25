package com.ecoprint.printmanagement.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.PrintTask;
import com.ecoprint.printmanagement.repository.PrintTaskRepository;

@Service

public class PrintTaskService {

	
	@Value("${upload.directory}")
	private String uploadDir;

    @Autowired
    private PrintTaskRepository printTaskRepository;

    public List<PrintTask> getActivePrintTasks() {
        return printTaskRepository.findByStatus("Printing");
    }

    public PrintTask createPrintTask(MultipartFile file, int totalPages) {
        // Save file locally or in a storage system
        String filePath = saveFile(file);

        // Create and save a new PrintTask entity
        PrintTask printTask = new PrintTask();
        printTask.setStatus("Queued");
        printTask.setTotalPages(totalPages);
        printTask.setPagesPrinted(0);
        printTask.setStartTime(LocalDateTime.now());
        printTaskRepository.save(printTask);

        // Trigger the printing process asynchronously
        new Thread(() -> processPrintTask(printTask.getId(), filePath)).start();

        return printTask;
    }
    
    private String saveFile(MultipartFile file) {
        try {
            // Define the directory where the file will be saved
            String uploadDir = "uploads"; // You can change this to your desired directory
            File directory = new File(uploadDir);
            
            // Create the directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate a unique filename to avoid conflicts
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;

            // Create the full file path
            String filePath = uploadDir + File.separator + uniqueFilename;

            // Save the file to the directory
            File destinationFile = new File(filePath);
            file.transferTo(destinationFile);

            // Return the file path
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }
    }

    
    private void processPrintTask(Long taskId, String filePath) {
        try {
            PrintTask task = printTaskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Print Task not found"));

            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
            if (printService == null) {
                task.setStatus("Failed");
                task.setErrorMessage("No default printer configured");
                printTaskRepository.save(task);
                return;
            }

            DocPrintJob printJob = printService.createPrintJob();
            Doc doc = new SimpleDoc(new FileInputStream(filePath), DocFlavor.INPUT_STREAM.AUTOSENSE, null);

            task.setStatus("Printing");
            printTaskRepository.save(task);

            printJob.print(doc, null);

            task.setStatus("Completed");
            task.setPagesPrinted(task.getTotalPages());
            printTaskRepository.save(task);

        } catch (Exception e) {
            PrintTask task = printTaskRepository.findById(taskId).orElseThrow();
            task.setStatus("Failed");
            task.setErrorMessage(e.getMessage());
            printTaskRepository.save(task);
        }
    }
    
    
    private PrintService getDefaultPrintService() {
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        if (printService == null) {
            System.out.println("No default printer configured.");
            return null;
        }
        System.out.println("Default printer found: " + printService.getName());
        return printService;
    }



}
