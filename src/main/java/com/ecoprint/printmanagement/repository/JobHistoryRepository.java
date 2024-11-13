package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.PrintJobStatus;

@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory, Long> {

    // Method to find job history by print job ID, ordered by timestamp
    List<JobHistory> findByPrintJobIdOrderByTimestampAsc(Long printJobId);
    List<JobHistory> findByStatus(PrintJobStatus status);
    JobHistory findTopByPrintJobIdOrderByTimestampDesc(Long printJobId);  
   
    
    List<JobHistory> findByUserName(String userName);

    



    
}
