package com.ecoprint.printmanagement.service;
import java.util.List;

import com.ecoprint.printmanagement.model.CompanyAuditLog;


public interface CompanyAuditLogService {
    List<CompanyAuditLog> getAllAuditLogs();
    List<CompanyAuditLog> getAuditLogsByCompanyId(Long companyId);



}
