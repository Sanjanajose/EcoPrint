package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.DeletionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletionAuditLogRepository extends JpaRepository<DeletionAuditLog, Long> {
}
