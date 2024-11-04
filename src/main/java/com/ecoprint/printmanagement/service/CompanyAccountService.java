package com.ecoprint.printmanagement.service;

import java.util.Optional;

import com.ecoprint.printmanagement.model.CompanyDetails;

public interface CompanyAccountService {

	CompanyDetails createCompanyAccount(CompanyDetails companyDetails, Long adminId);
    CompanyDetails updateCompanyAccount(Long companyId, CompanyDetails CompanyDetails);
    void deleteCompanyAccount(Long companyId);
    CompanyDetails deactivateAccount(Long companyId);
    CompanyDetails reactivateAccount(Long companyId);
    Optional<CompanyDetails> getCompanyAccountById(Long companyId);
   
    

}
