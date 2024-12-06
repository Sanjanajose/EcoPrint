package com.ecoprint.printmanagement.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecoprint.printmanagement.dto.PrintJobReceiptDTO;
import com.ecoprint.printmanagement.exception.ResourceNotFoundException;
import com.ecoprint.printmanagement.model.CompletedJob;
import com.ecoprint.printmanagement.model.CustomUserDetails;
import com.ecoprint.printmanagement.model.token.JwtAuthenticationToken;
import com.ecoprint.printmanagement.repository.CompletedJobRepository;
import com.ecoprint.printmanagement.service.CompletedJobService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/api/completed-jobs")
public class CompletedJobController {
	
	@Autowired
    private CompletedJobService completedJobService;

   /* @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CompletedJob>> getAllCompletedJobs() {
        List<CompletedJob> jobs = completedJobService.getAllCompletedJobs();
        return ResponseEntity.ok(jobs);
    }*/
	
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<CompletedJob>> getFilteredCompletedJobs(
	        @RequestParam(required = false) Long userId,
	        @RequestParam(required = false) String printerName,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    try {
	        Page<CompletedJob> jobs = completedJobService.getFilteredCompletedJobs(userId, printerName, fromDate, toDate, page, size);
	        return ResponseEntity.ok(jobs);
	    } catch (Exception e) {
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving completed jobs", e);
	    }
	}


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == @completedJobService.getUserIdByCompletedJobId(#id)")
    public ResponseEntity<CompletedJob> getCompletedJobById(@PathVariable Long id) {
        CompletedJob job = completedJobService.getCompletedJobById(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
    public ResponseEntity<List<CompletedJob>> getCompletedJobsByUserId(@PathVariable Long userId) {
        List<CompletedJob> jobs = completedJobService.getCompletedJobsByUserId(userId);
        return ResponseEntity.ok(jobs);
    }

    
  
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CompletedJob>> getMyCompletedJobs() {
        Long currentUserId = getCurrentUserId(); // Retrieve the current user's ID
        List<CompletedJob> jobs = completedJobService.getMyCompletedJobs(currentUserId);
        return ResponseEntity.ok(jobs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompletedJob(@PathVariable Long id) {
        completedJobService.deleteCompletedJob(id);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
    
   /* @GetMapping("/{jobId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long jobId) {
        try {
            PrintJobReceiptDTO receipt = completedJobService.generateReceipt(jobId);
            byte[] pdfContent = completedJobService.createReceiptPDF(receipt);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Receipt_" + jobId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }*/
    
    @GetMapping("/{jobId}/receipt")
    @PreAuthorize("hasRole('ADMIN') or principal.id == @completedJobService.getUserIdByCompletedJobId(#jobId)")
    public ResponseEntity<byte[]> getJobReceipt(@PathVariable Long jobId) {
        // Generate the receipt as a PDF byte array
        byte[] receiptPdf = completedJobService.generateReceiptPdf(jobId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt_" + jobId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(receiptPdf);
    }


    
    private boolean hasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleName));
    }



}


    