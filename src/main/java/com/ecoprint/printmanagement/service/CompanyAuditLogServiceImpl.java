package com.ecoprint.printmanagement.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecoprint.printmanagement.model.CompanyAuditLog;
import com.ecoprint.printmanagement.repository.CompanyAuditLogRepository;


@Service
public class CompanyAuditLogServiceImpl implements CompanyAuditLogService{

    @Autowired
    private CompanyAuditLogRepository companyAuditLogRepository;

    @Override
    public List<CompanyAuditLog> getAllAuditLogs() {
        return companyAuditLogRepository.findAll();
    }

    @Override
    public List<CompanyAuditLog> getAuditLogsByCompanyId(Long companyId) {
        return companyAuditLogRepository.findByCompanyId(companyId);
    }


}
