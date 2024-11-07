package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ecoprint.printmanagement.model.PrintJob;

public interface PrintJobRepository extends JpaRepository<PrintJob, Long>{
/*
	public PrintJob findPrintJobById(Long id) {
	    return printJobRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + id));
	}
*/
}
