package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoprint.printmanagement.model.FailedJob;

public interface FailedJobRepository extends JpaRepository<FailedJob, Long>{

}
