package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.JobProgress;
@Repository
public interface JobProgressRepository extends JpaRepository<JobProgress, Long> {

    JobProgress findByJobId(long jobId); 

}
