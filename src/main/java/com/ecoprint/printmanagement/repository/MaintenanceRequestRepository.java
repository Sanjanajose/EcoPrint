package com.ecoprint.printmanagement.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecoprint.printmanagement.model.MaintenanceRequest;
import com.ecoprint.printmanagement.model.MaintenanceStatus;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
  
    List<MaintenanceRequest> findByPrinterId(Long printerId, Pageable pageable);
    
    
    @Query("SELECT mr FROM MaintenanceRequest mr WHERE mr.status = :status")
    Page<MaintenanceRequest> findByStatus(@Param("status") MaintenanceStatus status, Pageable pageable);
    
    
    @Query("SELECT m FROM MaintenanceRequest m JOIN FETCH m.printer WHERE m.status = :status")
    Page<MaintenanceRequest> findByStatusWithPrinter(@Param("status") MaintenanceStatus status, Pageable pageable);


}
