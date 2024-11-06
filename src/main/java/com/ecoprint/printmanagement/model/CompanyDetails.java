package com.ecoprint.printmanagement.model;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "company_details")
public class CompanyDetails {
		
    @Id
    @Column(name = "company_id", unique = true, nullable = false)
    private Long companyId;

    @Column(name = "company_name", unique = true, nullable = false)
    private String companyName;
    
    @Column(name = "email", unique = true)
    @NotBlank(message = "Company Email cannot be null")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
        message = "Invalid email format"
    )
    private String email;
    
    
    
    
    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "contact_number", nullable = false)
    @NotNull(message = "Contact number cannot be null")
    @Pattern(regexp = "^((\\+971)|0)?[5][0-9]{8}$", message = "Invalid UAE contact number")
    private String contactNumber;

 
    @Column(name = "industry")
    private String industry;

    @Column(name = "active")
    private Boolean active;

    // Constructors

    public CompanyDetails() {
    }

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public CompanyDetails(Long companyId, String companyName,
			@NotNull(message = "Contact number cannot be null") @Pattern(regexp = "^((\\+971)|0)?[5][0-9]{8}$", message = "Invalid UAE contact number") String contactNumber,
			String industry, Boolean active) {
		super();
		this.companyId = companyId;
		this.companyName = companyName;
		this.contactNumber = contactNumber;
		this.industry = industry;
		this.active = active;
	}
	
	@Override
	public String toString() {
	    return "CompanyDetails{" +
	            "companyId=" + companyId +
	            ", companyName='" + companyName + '\'' +
	            ", email='" + email + '\'' +
	            ", contactNumber='" + contactNumber + '\'' +
	            ", industry='" + industry + '\'' +
	            ", active=" + active +
	            '}';
	}

    
    

}
