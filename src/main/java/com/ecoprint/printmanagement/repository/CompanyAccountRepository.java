package com.ecoprint.printmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.CompanyDetails;

@Repository
public interface CompanyAccountRepository extends JpaRepository<CompanyDetails, Long>{
	
    Optional<CompanyDetails> findByCompanyName(String companyName);


}
