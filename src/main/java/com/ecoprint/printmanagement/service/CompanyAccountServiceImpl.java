package com.ecoprint.printmanagement.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.CompanyAuditLog;
import com.ecoprint.printmanagement.model.CompanyDetails;
import com.ecoprint.printmanagement.repository.CompanyAccountRepository;
import com.ecoprint.printmanagement.repository.CompanyAuditLogRepository;

@Service
public class CompanyAccountServiceImpl implements CompanyAccountService{
	
    @Autowired
    private CompanyAccountRepository companyAccountRepository;
    
    @Autowired
    private CompanyAuditLogRepository auditLogRepository;
    
    private static final Long adminId = 3L;  // Fixed adminId for SuperAdmin



	
    private static final Logger logger = LoggerFactory.getLogger(CompanyAccountServiceImpl.class);

	
	@Override
	public CompanyDetails createCompanyAccount(CompanyDetails companyDetails,Long adminId) {
	    // Validation for unique company name
	    if (companyAccountRepository.findByCompanyName(companyDetails.getCompanyName()).isPresent()) {
	        throw new IllegalArgumentException("Company name must be unique");
	    }

	    CompanyDetails savedAccount = companyAccountRepository.save(companyDetails);
	    logger.info("Created new company account: {}", savedAccount);
        auditLogRepository.save(new CompanyAuditLog(savedAccount.getCompanyId(), "CREATE", "Created company account with ID: " + savedAccount.getCompanyId(), adminId));

	    return savedAccount;
	}
	
	@Override
    public CompanyDetails deactivateAccount(Long companyId) {
	    auditLogRepository.save(new CompanyAuditLog(companyId, "DEACTIVATE", "Deactivated company account with ID: " + companyId, adminId));

		        return updateAccountStatus(companyId, false);
    }

    @Override
    public CompanyDetails reactivateAccount(Long companyId) {
        auditLogRepository.save(new CompanyAuditLog(companyId, "REACTIVATE", "Reactivated company account with ID: " + companyId,adminId));
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
        	auditLogRepository.save(new CompanyAuditLog(companyId, "Updating the Company Details:", "Please Enter the valid Company ID: " + companyId,adminId));
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
            auditLogRepository.save(new CompanyAuditLog(companyId, "UPDATE", "Updated company account " + updatedAccount.getCompanyId(),adminId));
            return updatedAccount;
        } else {
            throw new IllegalArgumentException("Company account not found");
        }

	}	
	
	@Override
	public void deleteCompanyAccount(Long companyId) {
	    logger.info("Attempting to delete company account with ID: {}", companyId);
	    try {
	        companyAccountRepository.deleteById(companyId);
	        logger.info("Successfully deleted company account with ID: {}", companyId);
	        
	        // Audit log entry (if required)
	        auditLogRepository.save(new CompanyAuditLog(companyId, "DELETE", "Deleted company account with ID: " + companyId,adminId));
	    } catch (Exception e) {
	        logger.error("Error occurred while trying to delete company account with ID: {}", companyId, e);
	        throw e; // Re-throw or handle accordingly
	    }
	}

	@Override
	public Optional<CompanyDetails> getCompanyAccountById(Long companyId) {
	    logger.info("Retrieving company account with ID: {}", companyId);
	    
	    Optional<CompanyDetails> companyAccount = companyAccountRepository.findById(companyId);
	    
	    if (companyAccount.isPresent()) {
	        logger.info("Company account found with ID: {}", companyId);
	        
	        // Save audit log entry for retrieval action
	        auditLogRepository.save(new CompanyAuditLog(companyId, "RETRIEVE", 
	            "Retrieved company account with ID: " + companyId,adminId));
	    } else {
	        logger.warn("No company account found with ID: {}", companyId);
	        
	        // Optionally, you can log an audit entry for failed retrieval attempts
	        auditLogRepository.save(new CompanyAuditLog(companyId, "RETRIEVE_FAILED", 
	            "Attempted retrieval of non-existent company account with ID: " + companyId,adminId));
	    }
	    
	    return companyAccount;
	}
	
	

}
