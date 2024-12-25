package com.ecoprint.printmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecoprint.printmanagement.model.PrintEvent;

public interface PrintJobManagementRepository extends JpaRepository<PrintEvent, Long>{
	
	
    @Query("SELECT MAX(p.jobId) FROM PrintEvent p")
    Optional<Long> findMaxJobId();

    @Query("SELECT p FROM PrintEvent p WHERE p.printerJobId = :printerJobId AND p.status = 'PROCESSING'")
    List<PrintEvent> findActiveJobsByPrinterJobId(@Param("printerJobId") Integer printerJobId);
    
    @Query("SELECT p FROM PrintEvent p WHERE p.status = 'FAILED'")
    List<PrintEvent> findFailedJobs();
    
    
    @Query("SELECT p FROM PrintEvent p WHERE p.printerJobId = :printerJobId")
    PrintEvent findByPrinterJobId(@Param("printerJobId") Long printerJobId);


}
