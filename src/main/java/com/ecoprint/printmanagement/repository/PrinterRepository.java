package com.ecoprint.printmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.Printer;

@Repository
public interface PrinterRepository  extends JpaRepository<Printer, Long>{

	
    // Find printer by name
    Optional<Printer> findByName(String name);

    // Find printer by IP address
    Optional<Printer> findByIpAddress(String ipAddress);

    // Find all printers by status
    List<Printer> findByStatus(String status);

    // Find printers by location
    List<Printer> findByLocation(String location);

    // Check if a printer with a given IP exists
    boolean existsByIpAddress(String ipAddress);

    // Custom query example (if needed)
    // Find printers that are online and located in a specific location
    List<Printer> findByStatusAndLocation(String status, String location);
    
    
    
    // Custom query to find printer IP by job ID
  //  @Query("SELECT p.ipAddress FROM PrintJob pj JOIN pj.printer p WHERE pj.id = :jobId")
   // String getPrinterIpByJobId(@Param("jobId") long jobId);

}
