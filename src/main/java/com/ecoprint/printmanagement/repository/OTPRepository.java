package com.ecoprint.printmanagement.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecoprint.printmanagement.model.OTP;
@Repository
public interface OTPRepository extends JpaRepository<OTP, Long>{
    /**
     * Find an OTP by userId and otpCode.
     *
     * @param userId The ID of the user associated with the OTP.
     * @param otpCode The one-time password (OTP) code.
     * @return An Optional containing the OTP if found, or empty if not found.
     */
    // Find an OTP by user ID and OTP code
    Optional<OTP> findByUserIdAndOtpCode(Long userId, String otpCode);

    // Optional: Delete OTPs for a specific user (if you want to clear old OTPs after successful validation)
    void deleteByUserId(Long userId);

    // Optional: Find OTPs that have expired
    // Note: expiryTime should be a field in the OTP entity
    void deleteByExpiryTimeBefore(LocalDateTime currentDateTime);


}
