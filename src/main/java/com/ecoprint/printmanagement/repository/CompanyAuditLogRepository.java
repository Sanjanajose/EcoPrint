package com.ecoprint.printmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.CompanyAuditLog;

@Repository
public interface CompanyAuditLogRepository extends JpaRepository<CompanyAuditLog, Long> {
    List<CompanyAuditLog> findByCompanyId(Long companyId);
	

}
