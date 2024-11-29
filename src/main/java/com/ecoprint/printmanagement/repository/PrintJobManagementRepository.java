package com.ecoprint.printmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoprint.printmanagement.model.PrintEvent;

public interface PrintJobManagementRepository extends JpaRepository<PrintEvent, Long>{

}
