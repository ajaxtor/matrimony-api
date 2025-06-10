package com.api.matrimony.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.api.matrimony.entity.OtpVerification;
import com.api.matrimony.enums.OtpPurpose;

/**
 * OTP Verification Repository
 */
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    Optional<OtpVerification> findByPhoneAndPurposeAndIsVerifiedFalse(String phone, OtpPurpose purpose);
    
    Optional<OtpVerification> findByEmailAndPurposeAndIsVerifiedFalse(String email, OtpPurpose purpose);
    
    @Query("SELECT o FROM OtpVerification o WHERE " +
           "(o.phone = :contact OR o.email = :contact) AND o.purpose = :purpose AND o.isVerified = false " +
           "ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpVerification> findLatestOtpByContactAndPurpose(@Param("contact") String contact, @Param("purpose") OtpPurpose purpose);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    @Query("SELECT COUNT(o) FROM OtpVerification o WHERE " +
           "(o.phone = :contact OR o.email = :contact) AND o.createdAt >= :fromTime")
    Long countOtpAttempts(@Param("contact") String contact, @Param("fromTime") LocalDateTime fromTime);
}