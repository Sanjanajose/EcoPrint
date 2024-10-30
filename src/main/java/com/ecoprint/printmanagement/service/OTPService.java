package com.ecoprint.printmanagement.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecoprint.printmanagement.model.OTP;
import com.ecoprint.printmanagement.repository.OTPRepository;
import com.ecoprint.printmanagement.util.Util;

@Service
public class OTPService {
	
    @Autowired
    private OTPRepository otpRepository;

    public String generateAndSaveOTP(Long userId) {
        // Generate OTP
        String otpCode = Util.generateOTP();
        
        // Set expiration time (5 minutes from now)
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

        // Save OTP to database
        OTP otp = new OTP(userId, userId, otpCode, expiryTime);
        otp.setUserId(userId);
        otp.setOtpCode(otpCode);
        otp.setExpiryTime(expiryTime); 
        otpRepository.save(otp);
        return otpCode;
    }
/*
    public boolean validateOTP(Long userId, String otpCode) {
        Optional<OTP> otp = otpRepository.findByUserIdAndOtpCode(userId, otpCode);
    
        if (otp.getExpiryTime().isAfter(LocalDateTime.now())) {
            otpRepository.deleteById(otp); // Optional: delete OTP after successful validation
            return true;
        }
        return false;
    }*/


}
