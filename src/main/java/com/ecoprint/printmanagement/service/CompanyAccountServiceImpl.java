package com.ecoprint.printmanagement.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.CompanyDetails;
import com.ecoprint.printmanagement.repository.CompanyAccountRepository;

@Service
public class CompanyAccountServiceImpl implements CompanyAccountService{
	
    @Autowired
    private CompanyAccountRepository companyAccountRepository;

	
    private static final Logger logger = LoggerFactory.getLogger(CompanyAccountServiceImpl.class);

	
	@Override
	public CompanyDetails createCompanyAccount(CompanyDetails companyDetails) {
	    // Validation for unique company name
	    if (companyAccountRepository.findByCompanyName(companyDetails.getCompanyName()).isPresent()) {
	        throw new IllegalArgumentException("Company name must be unique");
	    }

	    CompanyDetails savedAccount = companyAccountRepository.save(companyDetails);
	    logger.info("Created new company account: {}", savedAccount);
	    return savedAccount;
	}
	@Override
    public CompanyDetails deactivateAccount(Long companyId) {
        return updateAccountStatus(companyId, false);
    }

    @Override
    public CompanyDetails reactivateAccount(Long companyId) {
        return updateAccountStatus(companyId, true);
    }
    
    private CompanyDetails updateAccountStatus(Long companyId, boolean isActive) {
        Optional<CompanyDetails> existingAccountOpt = companyAccountRepository.findById(companyId);

        if (existingAccountOpt.isPresent()) {
        	CompanyDetails account = existingAccountOpt.get();
            account.setActive(isActive);
            CompanyDetails updatedAccount = companyAccountRepository.save(account);
            String action = isActive ? "Reactivated" : "Deactivated";
            logger.info("{} company account: {}", action, updatedAccount);
            return updatedAccount;
        } else {
            throw new IllegalArgumentException("Company account not found");
        }
    }

	@Override
	public CompanyDetails updateCompanyAccount(Long companyId, CompanyDetails companyDetails) {
        Optional<CompanyDetails> existingAccountOpt = companyAccountRepository.findById(companyId);
        if (existingAccountOpt.isPresent()) {
        	CompanyDetails existingAccount = existingAccountOpt.get();
            existingAccount.setCompanyName(companyDetails.getCompanyName());
    	    existingAccount.setContactNumber(companyDetails.getContactNumber());
            existingAccount.setIndustry(companyDetails.getIndustry());
            existingAccount.setEmail(companyDetails.getEmail());
            CompanyDetails updatedAccount = companyAccountRepository.save(existingAccount);
            logger.info("Updated company account: {}", updatedAccount);
            return updatedAccount;
        } else {
            throw new IllegalArgumentException("Company account not found");
        }

	}	
	
    @Override
    public void deleteCompanyAccount(Long companyId) {
        companyAccountRepository.deleteById(companyId);
        logger.info("Deleted company account with ID: {}", companyId);
    }

	@Override
    public Optional<CompanyDetails> getCompanyAccountById(Long companyId) {
        return companyAccountRepository.findById(companyId);
    }

}
