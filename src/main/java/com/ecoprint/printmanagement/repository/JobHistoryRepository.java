package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory, Long> {
    List<JobHistory> findAllByJob_JobId(Long jobId);
}
