package com.ecoprint.printmanagement.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecoprint.printmanagement.model.CompanyDetails;
import com.ecoprint.printmanagement.service.CompanyAccountService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

//import com.ecoprint.printmanagement.repository.CompanyDetailsRepository;

@RestController
@PreAuthorize("hasRole('ROLE_SUPERADMIN')")
@RequestMapping("/api/company-account")
public class CompanyAccountManagementController {
	
    @Autowired
    private CompanyAccountService companyAccountService;

    
    @PostMapping("/create")
    @Operation(summary = "Create a company account", description = "Creates a new company account and returns the created account details.")
    public ResponseEntity<CompanyDetails> createCompanyAccount(@Valid @RequestBody CompanyDetails companyDetails) {
        try {
        	CompanyDetails savedAccount = companyAccountService.createCompanyAccount(companyDetails);
            return new ResponseEntity<>(savedAccount, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    
    
    @PutMapping("/update/{id}")
    @Operation(summary = "Update a company account", description = "Updates the details of an existing company account by its ID.")
     public ResponseEntity<CompanyDetails> updateCompanyAccount(@PathVariable Long id, @Valid @RequestBody CompanyDetails companyDetails) {
    	try {
        	 System.out.println(id+"::::id");
        	 System.out.println(companyDetails+"::::companyDetails");

        	 CompanyDetails updatedAccount = companyAccountService.updateCompanyAccount(id, companyDetails);
             return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
         } catch (IllegalArgumentException e) {
             return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
         }
     }
    
   
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete a company account", description = "Deletes an existing company account by its ID.")
    public ResponseEntity<HttpStatus> deleteCompanyAccount(@PathVariable Long id) {
        companyAccountService.deleteCompanyAccount(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    
    @GetMapping("/{id}")
    @Operation(summary = "Get a company account by ID", description = "Retrieves the details of a company account by its ID.")
    public ResponseEntity<CompanyDetails> getCompanyAccountById(@PathVariable Long id) {
        Optional<CompanyDetails> companyAccount = companyAccountService.getCompanyAccountById(id);
        return companyAccount.map(account -> new ResponseEntity<>(account, HttpStatus.OK))
                             .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

   
    @PatchMapping("/deactivate/{id}")
    @Operation(summary = "Deactivate a company account", description = "Deactivates an existing company account by its ID.")
    public ResponseEntity<CompanyDetails> deactivateCompanyAccount(@PathVariable Long id) {
        try {
        	CompanyDetails deactivatedAccount = companyAccountService.deactivateAccount(id);
            return new ResponseEntity<>(deactivatedAccount, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

 
    @PatchMapping("/reactivate/{id}")
    @Operation(summary = "Reactivate a company account", description = "Reactivates an existing company account by its ID.")
    public ResponseEntity<CompanyDetails> reactivateCompanyAccount(@PathVariable Long id) {
        try {
            CompanyDetails reactivatedAccount = companyAccountService.reactivateAccount(id);
            return new ResponseEntity<>(reactivatedAccount, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


}
