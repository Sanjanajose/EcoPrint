package com.ecoprint.printmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.CompletedJob;

@Repository
	public interface CompletedJobRepository extends JpaRepository<CompletedJob, Long> {
	    List<CompletedJob> findByUserId(Long userId); // Example query for completed jobs by user
	}



