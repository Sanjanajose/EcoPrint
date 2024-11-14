package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.ecoprint.printmanagement.model.JobHistory;
import com.ecoprint.printmanagement.model.PrintJobStatus;

@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory, Long> {

    // Method to find job history by print job ID, ordered by timestamp
    List<JobHistory> findByPrintJobIdOrderByTimestampAsc(Long printJobId);

    // Removed findByStatus(PrintJobStatus status), as 'status' no longer exists in the entity

    // Find by updated status (if that's what you're trying to filter by)
    List<JobHistory> findByUpdatedStatus(PrintJobStatus status);

    // Or find by previous status (if that's what you're trying to filter by)
    List<JobHistory> findByPreviousStatus(PrintJobStatus status);

    // Find by user name
    List<JobHistory> findByUserName(String userName);
    
    // Additional custom queries as needed...
    JobHistory findTopByPrintJobIdOrderByTimestampDesc(Long printJobId);  
}
