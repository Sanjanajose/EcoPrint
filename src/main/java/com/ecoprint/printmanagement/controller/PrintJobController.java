package com.ecoprint.printmanagement.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecoprint.printmanagement.service.PrintJobService;

@RestController
@RequestMapping("/api/print-jobs")
public class PrintJobController {
	
    private static final Logger logger = LoggerFactory.getLogger(PrintJobController.class);

		
    @Autowired
    private PrintJobService printJobService;
    
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPrintJob(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userName") String userName) {
        try {
            printJobService.uploadFile(file, userName);
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
  
        
    
    
    

}
