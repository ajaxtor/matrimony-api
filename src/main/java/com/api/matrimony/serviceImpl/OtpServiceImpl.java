package com.api.matrimony.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.api.matrimony.entity.OtpVerification;
import com.api.matrimony.enums.OtpPurpose;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.OtpVerificationRepository;
import com.api.matrimony.request.EmailModel;
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

    @Value("${app.otp.max-attempts:6}")
    private int maxAttempts;

//    @Override
//    public void sendOtp(String phone,EmailModel model, OtpPurpose purpose) {
//        log.info("Sending OTP to contact: {}, purpose: {}", phone, purpose);
//        
//        List<OtpVerification> saveVerification = new ArrayList<>();
//        // Check rate limiting
//        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
//        Long attempts = otpRepository.countOtpAttempts(phone, oneHourAgo);
//        
//        if (attempts >= maxAttempts) {
//        	throw new ApplicationException(ErrorEnum.TOO_MANY_APTEMT_FOR_OTP.toString(),
//					ErrorEnum.TOO_MANY_APTEMT_FOR_OTP.getExceptionError(), HttpStatus.OK);
//        }
//
//        // Generate OTP
//        String otpForEmail = generateOtp();
//        String otpForPhone = generateOtp();
//        
//        // Save OTP
//        OtpVerification otpVerificationForEmail = new OtpVerification();
//        if (model.getTo().contains("@")) {
//        	otpVerificationForEmail.setEmail(model.getTo());
//        } else {
//            otpVerification.setPhone(phone);
//        }
//        otpVerificationForEmail.setOtp(otp);
//        otpVerificationForEmail.setPurpose(purpose);
//        otpVerificationForEmail.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
//        
//        OtpVerification verifyBody = otpRepository.save(otpVerification);
//
//        
//        // Send OTP via SMS or Email
//        
//        if (model.getTo().contains("@")) {
//            notificationService.sendEmailNotification(verifyBody);
//        }
//            notificationService.sendSmsNotification(phone,otp);
//
//        log.info("OTP sent successfully to: {}", model.getTo() +" or "+phone);
//    }
    
    
    public void sendOtp(String phone, EmailModel model, OtpPurpose purpose) {
        log.info("Sending OTP to contact: {}, purpose: {}", phone, purpose);

        // 1. Check rate limiting (based on phone)
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long attempts = otpRepository.countOtpAttempts(phone, oneHourAgo);

        if (attempts >= maxAttempts) {
            throw new ApplicationException(
                    ErrorEnum.TOO_MANY_APTEMT_FOR_OTP.toString(),
                    ErrorEnum.TOO_MANY_APTEMT_FOR_OTP.getExceptionError(),
                    HttpStatus.OK
            );
        }

        // 2. Generate separate OTPs
        String otpForEmail = generateOtp();
        String otpForPhone = generateOtp();

        List<OtpVerification> otpRecords = new ArrayList<>();

        // 3. Create OTP record for Email (if valid email)
        if (model != null && model.getTo() != null && model.getTo().contains("@")) {
            OtpVerification emailOtp = new OtpVerification();
            emailOtp.setEmail(model.getTo());
            emailOtp.setOtp(otpForEmail);
            emailOtp.setPurpose(purpose);
            emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
            otpRecords.add(emailOtp);
        }

        // 4. Create OTP record for Phone
        if (phone != null && !phone.isBlank()) {
            OtpVerification phoneOtp = new OtpVerification();
            phoneOtp.setPhone(phone);
            phoneOtp.setOtp(otpForPhone);
            phoneOtp.setPurpose(purpose);
            phoneOtp.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
            otpRecords.add(phoneOtp);
        }

        // 5. Save all OTP records
        otpRepository.saveAll(otpRecords);

        // 6. Send OTP via Email (if applicable)
        if (model != null && model.getTo() != null && model.getTo().contains("@")) {
            try {
                notificationService.sendEmailNotification(otpRecords.get(0));
                log.info("Email OTP sent successfully to: {}", model.getTo());
            } catch (Exception ex) {
                log.error("Failed to send email OTP to {}: {}", model.getTo(), ex.getMessage());
            }
        }

        // 7. Send OTP via SMS (if applicable)
        if (phone != null && !phone.isBlank()) {
            try {
                notificationService.sendSmsNotification(phone, otpForPhone);
                log.info("Phone OTP sent successfully to: {}", phone);
            } catch (Exception ex) {
                log.error("Failed to send phone OTP to {}: {}", phone, ex.getMessage());
            }
        }

        log.info("OTP process completed for purpose: {}", purpose);
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

//    @Override
//    public void resendOtp(String phone,String email, OtpPurpose purpose) {
//        log.info("Resending OTP to contact: {}, purpose: {}", email, purpose);
//        sendOtp(phone,email, purpose);
//    }

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
