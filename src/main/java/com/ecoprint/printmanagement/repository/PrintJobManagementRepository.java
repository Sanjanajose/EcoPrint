package com.ecoprint.printmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecoprint.printmanagement.model.PrintEvent;

public interface PrintJobManagementRepository extends JpaRepository<PrintEvent, Long>{
	
	
    @Query("SELECT MAX(p.jobId) FROM PrintEvent p")
    Optional<Long> findMaxJobId();


}
