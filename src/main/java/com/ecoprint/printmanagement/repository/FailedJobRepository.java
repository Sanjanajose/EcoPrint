package com.ecoprint.printmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.FailedJob;

@Repository
public interface FailedJobRepository extends JpaRepository<FailedJob, Long>{
	
    List<FailedJob> findAll(); // This method is available by default

    @Query("SELECT f FROM FailedJob f JOIN FETCH f.printer p")
    List<FailedJob> findAllWithPrinters();

}
