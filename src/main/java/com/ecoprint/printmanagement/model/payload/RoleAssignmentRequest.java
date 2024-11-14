package com.ecoprint.printmanagement.model.payload;

import java.util.Set;

public class RoleAssignmentRequest {
	
	
	    private Set<String> roleNames; // A set of role names to be assigned

	    // Getters and Setters
	    public Set<String> getRoleNames() {
	        return roleNames;
	    }

	    public void setRoleNames(Set<String> roleNames) {
	        this.roleNames = roleNames;
	    }
	}





