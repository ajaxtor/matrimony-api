package com.api.matrimony.service;

import com.api.matrimony.request.ForgotPasswordRequest;
import com.api.matrimony.request.LoginRequest;
import com.api.matrimony.request.RegisterRequest;
import com.api.matrimony.request.ResetPasswordRequest;
import com.api.matrimony.request.VerifyOtpRequest;
import com.api.matrimony.response.LoginResponse;

/**
 * Authentication Service Interface
 */
public interface AuthService {
    
    String register(RegisterRequest request);
    String verifyOtp(VerifyOtpRequest request);
    String resendOtp(String contact, String purpose);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
    void logout(String token);
    boolean emailExists(String email);
    boolean phoneExists(String phone);
}