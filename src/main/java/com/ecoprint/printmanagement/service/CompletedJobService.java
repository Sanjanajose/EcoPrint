package com.ecoprint.printmanagement.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.dto.PrintJobReceiptDTO;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CompletedJob;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.User;
import com.ecoprint.printmanagement.repository.CompletedJobRepository;
import com.ecoprint.printmanagement.repository.UserRepository;

@Service
public class CompletedJobService {

    @Autowired
    private CompletedJobRepository completedJobRepository;
    
    @Autowired
    private UserRepository userrepository;
    

    /**
     * Fetch all completed jobs (Admin only).
     */
    public List<CompletedJob> getAllCompletedJobs() {
        return completedJobRepository.findAll();
    }
    
    
    public Page<CompletedJob> getFilteredCompletedJobs(Long userId, String printerName,
            LocalDate fromDate, LocalDate toDate, int page, int size) {
    	Pageable pageable = PageRequest.of(page, size);

    	Specification<CompletedJob> spec = Specification.where(null);

    	if (userId != null) {
    		spec = spec.and((root, query, criteriaBuilder) ->
    		criteriaBuilder.equal(root.get("userId"), userId));
    	}
    	if (printerName != null) {
    		spec = spec.and((root, query, criteriaBuilder) ->
    		criteriaBuilder.like(root.get("printerName"), "%" + printerName + "%"));
    	}
    	
    	if (fromDate != null) {
    		spec = spec.and((root, query, criteriaBuilder) ->
    		criteriaBuilder.greaterThanOrEqualTo(root.get("completedAt"), fromDate.atStartOfDay()));
    	}
    	if (toDate != null) {
    		spec = spec.and((root, query, criteriaBuilder) ->
    		criteriaBuilder.lessThanOrEqualTo(root.get("completedAt"), toDate.atTime(LocalTime.MAX)));
    	}

    	return completedJobRepository.findAll(spec, pageable);
    }


    /**
     * Fetch a specific completed job by its ID (Admin or Job Owner).
     */
    public CompletedJob getCompletedJobById(Long jobId) {
        return completedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));
    }

    /**
     * Fetch all completed jobs for a specific user (Admin or Job Owner).
     */
    public List<CompletedJob> getCompletedJobsByUserId(Long userId) {
        return completedJobRepository.findByUserId(userId);
    }

    /**
     * Fetch all completed jobs for the currently logged-in user.
     */
    public List<CompletedJob> getMyCompletedJobs(Long currentUserId) {
        return completedJobRepository.findByUserId(currentUserId);
    }

    /**
     * Delete a specific completed job by its ID (Admin only).
     */
    public void deleteCompletedJob(Long jobId) {
        CompletedJob job = completedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));
        completedJobRepository.delete(job);
    }

    /**
     * Get the user ID associated with a completed job (for access checks).
     */
    public Long getUserIdByCompletedJobId(Long jobId) {
        return completedJobRepository.findById(jobId)
                .map(CompletedJob::getUserId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));
    }
    
    public byte[] generateReceiptPdf(Long jobId) {
        CompletedJob job = completedJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("CompletedJob", "id", jobId));

        // Fetch user details
        Optional<User> userOptional = userrepository.findById(job.getUserId());
        String userName = userOptional.map(User::getUsername).orElse("Unknown User");

        // Create a DTO for the receipt
        PrintJobReceiptDTO receipt = new PrintJobReceiptDTO();
        receipt.setJobId(job.getJobId());
        receipt.setDocumentName(job.getDescription());
        receipt.setPrinterName(job.getPrinterName());
        receipt.setTotalPages(job.getPagesPrinted());
        receipt.setCompletedAt(job.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        receipt.setUserName(userName);

        try {
            // Generate the receipt PDF
            return createReceiptPDF(receipt);
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF receipt", e);
        }
    }




    private boolean hasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        throw new IllegalStateException("User ID could not be retrieved from Principal");
    }

    private byte[] createReceiptPDF(PrintJobReceiptDTO receipt) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 700);

                contentStream.showText("Print Job Receipt");
                contentStream.newLine();
                contentStream.showText("------------------------------");
                contentStream.newLine();
                contentStream.showText("Job ID: " + receipt.getJobId());
                contentStream.newLine();
                contentStream.showText("Document Name: " + receipt.getDocumentName());
                contentStream.newLine();
                contentStream.showText("Printer Name: " + receipt.getPrinterName());
                contentStream.newLine();
                contentStream.showText("Total Pages: " + receipt.getTotalPages());
                contentStream.newLine();
                contentStream.showText("Completed At: " + receipt.getCompletedAt());
                contentStream.newLine();
                contentStream.showText("User: " + receipt.getUserName());
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    

}
