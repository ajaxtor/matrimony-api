package com.api.matrimony.serviceImpl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.matrimony.entity.OtpVerification;
import com.api.matrimony.enums.OtpPurpose;
import com.api.matrimony.exception.CustomException;
import com.api.matrimony.repository.OtpVerificationRepository;
import com.api.matrimony.service.NotificationService;
import com.api.matrimony.service.OtpService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OTP Service Implementation
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final NotificationService notificationService;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    @Override
    public void sendOtp(String contact, OtpPurpose purpose) {
        log.info("Sending OTP to contact: {}, purpose: {}", contact, purpose);
        
        // Check rate limiting
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long attempts = otpRepository.countOtpAttempts(contact, oneHourAgo);
        
        if (attempts >= maxAttempts) {
            throw new CustomException("Too many OTP attempts. Please try again later.");
        }

        // Generate OTP
        String otp = generateOtp();
        
        // Save OTP
        OtpVerification otpVerification = new OtpVerification();
        if (contact.contains("@")) {
            otpVerification.setEmail(contact);
        } else {
            otpVerification.setPhone(contact);
        }
        otpVerification.setOtp(otp);
        otpVerification.setPurpose(purpose);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        
        otpRepository.save(otpVerification);

        // Send OTP via SMS or Email
        if (contact.contains("@")) {
            notificationService.sendEmailNotification(contact, 
                    "Your OTP for " + purpose.name(), 
                    "Your OTP is: " + otp + ". Valid for " + expirationMinutes + " minutes.");
        } else {
            notificationService.sendSmsNotification(contact, 
                    "Your OTP is: " + otp + ". Valid for " + expirationMinutes + " minutes.");
        }

        log.info("OTP sent successfully to: {}", contact);
    }

    @Override
    public boolean verifyOtp(String contact, String otp, OtpPurpose purpose) {
        log.info("Verifying OTP for contact: {}, purpose: {}", contact, purpose);
        
        Optional<OtpVerification> otpVerificationOpt = otpRepository.findLatestOtpByContactAndPurpose(contact, purpose);
        
        if (otpVerificationOpt.isEmpty()) {
            log.warn("No OTP found for contact: {}, purpose: {}", contact, purpose);
            return false;
        }

        OtpVerification otpVerification = otpVerificationOpt.get();

        // Check if OTP is expired
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for contact: {}", contact);
            return false;
        }

        // Check if OTP is already verified
        if (otpVerification.getIsVerified()) {
            log.warn("OTP already used for contact: {}", contact);
            return false;
        }

        // Verify OTP
        if (otpVerification.getOtp().equals(otp)) {
            otpVerification.setIsVerified(true);
            otpRepository.save(otpVerification);
            log.info("OTP verified successfully for contact: {}", contact);
            return true;
        }

        log.warn("Invalid OTP for contact: {}", contact);
        return false;
    }

    @Override
    public void resendOtp(String contact, OtpPurpose purpose) {
        log.info("Resending OTP to contact: {}, purpose: {}", contact, purpose);
        sendOtp(contact, purpose);
    }

    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredOtps() {
        log.info("Cleaning up expired OTPs");
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    public boolean isOtpValid(String contact, String otp, OtpPurpose purpose) {
        return verifyOtp(contact, otp, purpose);
    }

    @Override
    public int getRemainingAttempts(String contact, OtpPurpose purpose) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long attempts = otpRepository.countOtpAttempts(contact, oneHourAgo);
        return Math.max(0, maxAttempts - attempts.intValue());
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}
