package com.ecoprint.printmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.model.DeletionAuditLog;
import com.ecoprint.printmanagement.repository.DeletionAuditLogRepository;

@RestController
@RequestMapping("/api/audit-logs")
public class DeleteAuditLogController {
	
	@Autowired
    private DeletionAuditLogRepository auditLogRepository;

	@PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<DeletionAuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

}
