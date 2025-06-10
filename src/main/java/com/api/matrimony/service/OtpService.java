package com.api.matrimony.service;

import com.api.matrimony.enums.OtpPurpose;

/**
 * OTP Service Interface
 */

public interface OtpService {
    void sendOtp(String contact, OtpPurpose purpose);
    boolean verifyOtp(String contact, String otp, OtpPurpose purpose);
    void resendOtp(String contact, OtpPurpose purpose);
    void cleanupExpiredOtps();
    boolean isOtpValid(String contact, String otp, OtpPurpose purpose);
    int getRemainingAttempts(String contact, OtpPurpose purpose);
}

