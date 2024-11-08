package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.ecoprint.printmanagement.model.JobHistory;

public interface JobHistoryRepository extends JpaRepository<JobHistory, Long> {

    // Method to find job history by print job ID, ordered by timestamp
    List<JobHistory> findByPrintJobIdOrderByTimestampAsc(Long printJobId);
}
