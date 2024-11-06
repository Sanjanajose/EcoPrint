package com.ecoprint.printmanagement.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.service.PrintJobService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/print-jobs")

public class PrintJobController {
	
    private static final Logger logger = LoggerFactory.getLogger(PrintJobController.class);
    private static final double COST_PER_PAGE = 0.50; // Define your cost per page rate here


		
    @Autowired
    private PrintJobService printJobService;
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(
    	    summary = "Upload a print job file",
    	    description = "Allows a user to upload a file for a print job, calculates the cost based on pages printed."
    	)
    public ResponseEntity<String> uploadPrintJob(
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "userName", required = true) String userName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "pagesPrinted", required = true) int pagesPrinted) {
        try {
            double cost = pagesPrinted * COST_PER_PAGE;

            printJobService.uploadFile(file, userName,description,pagesPrinted, cost);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully");
        } catch (IOException e) {
            logger.error("IOException during file upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during file upload");
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error during file upload");
        }
    }
  
    
    // This endpoint is tempory purpose
    @GetMapping("/download/{id}")
   public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            Resource resource = printJobService.downloadFile(id);
            PrintJob printJob = printJobService.findPrintJobById(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(printJob.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + printJob.getFileName() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    
    
    
    
 
        
       

}
