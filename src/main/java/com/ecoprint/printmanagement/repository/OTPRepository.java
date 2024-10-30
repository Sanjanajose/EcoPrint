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
    Optional<OTP> findByUserIdAndOtpCode(Long userId, String otpCode);

    /**
     * Delete all expired OTPs.
     *
     * @param expiryTime Time threshold for expiry.
     */
    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);


}
