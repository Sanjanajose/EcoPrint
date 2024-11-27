package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import com.ecoprint.printmanagement.model.PrintJob;
import com.ecoprint.printmanagement.model.PrintJobStatus;
import com.ecoprint.printmanagement.model.User;

public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    // Method to find jobs by status, ordered by priority
    List<PrintJob> findByStatusOrderByPriorityAsc(PrintJobStatus status);

    // Method to find all jobs ordered by status and priority
    List<PrintJob> findAllByOrderByStatusAscPriorityAsc();
    
    @Query("SELECT MAX(p.queuePosition) FROM PrintJob p")
    Integer findMaxQueuePosition();
    
    @Query("SELECT p FROM PrintJob p WHERE p.queuePosition BETWEEN :start AND :end ORDER BY p.queuePosition")
    List<PrintJob> findByQueuePositionBetween(@Param("start") int start, @Param("end") int end);
    
 // Fetch jobs by a list of statuses
    List<PrintJob> findAllByStatusIn(List<PrintJobStatus> statuses);

   
    // Find PrintJob by file name
    Optional<PrintJob> findByFileName(String fileName);
    
    
    @Query("SELECT p FROM PrintJob p WHERE p.id = :jobId")
    Optional<PrintJob> findJobById(@Param("jobId") Long jobId);


    
    

}
