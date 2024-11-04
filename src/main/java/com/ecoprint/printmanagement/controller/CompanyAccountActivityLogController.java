package com.ecoprint.printmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.model.CompanyAuditLog;
import com.ecoprint.printmanagement.service.CompanyAuditLogService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/company-account-logs")
public class CompanyAccountActivityLogController {
    @Autowired
    private CompanyAuditLogService companyAuditLogService;

    @Operation(summary = "Get all audit logs")
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllAuditLogs() {
        List<CompanyAuditLog> logs = companyAuditLogService.getAllAuditLogs();
        if (logs.isEmpty()) {
            return new ResponseEntity<>("No audit logs found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(logs, HttpStatus.OK);

    }
    
    @Operation(summary = "Get audit logs by companyId")
    @GetMapping("/getCompanyId/{companyId}")
    public ResponseEntity<?> getAuditLogsByAdminId(@PathVariable Long companyId) {
        List<CompanyAuditLog> logs = companyAuditLogService.getAuditLogsByCompanyId(companyId);
        if (logs.isEmpty()) {
            return new ResponseEntity<>("No audit logs found for Company ID: " + companyId, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(logs, HttpStatus.OK);

    }


}
