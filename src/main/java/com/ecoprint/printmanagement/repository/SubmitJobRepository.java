package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoprint.printmanagement.model.SubmittedJobs;

public interface SubmitJobRepository extends JpaRepository<SubmittedJobs, Long>{

}
