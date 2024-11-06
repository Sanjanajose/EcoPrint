package com.ecoprint.printmanagement.model;

public class LoginRequest2FA {

	

	    private String username;
	    private String password;
	    private String otp; // Optional field for 2FA

	    // Getters and Setters

	    public String getUsername() {
	        return username;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }

	    public String getOtp() {
	        return otp;
	    }

	    public void setOtp(String otp) {
	        this.otp = otp;
	    }
	

}
