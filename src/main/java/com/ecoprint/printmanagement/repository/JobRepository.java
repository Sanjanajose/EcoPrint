package com.ecoprint.printmanagement.repository;

import com.ecoprint.printmanagement.model.Job;
import com.ecoprint.printmanagement.model.JobHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    // Additional custom queries if needed
}