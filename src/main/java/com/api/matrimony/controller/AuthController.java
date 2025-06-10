package com.api.matrimony.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.request.ForgotPasswordRequest;
import com.api.matrimony.request.LoginRequest;
import com.api.matrimony.request.RefreshTokenRequest;
import com.api.matrimony.request.RegisterRequest;
import com.api.matrimony.request.ResetPasswordRequest;
import com.api.matrimony.request.VerifyOtpRequest;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.LoginResponse;
import com.api.matrimony.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication Controller handling user registration, login, and token management
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        try {
            String result = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(result, "Registration successful. Please verify your account."));
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Verify OTP for account activation
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification attempt for: {}", request.getContact());
        
        try {
            String result = authService.verifyOtp(request);
            return ResponseEntity.ok(ApiResponse.success(result, "Account verified successfully."));
        } catch (Exception e) {
            log.error("OTP verification failed for: {}", request.getContact(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Resend OTP
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestParam String contact, @RequestParam String purpose) {
        log.info("Resend OTP request for: {}", contact);
        
        try {
            String result = authService.resendOtp(contact, purpose);
            return ResponseEntity.ok(ApiResponse.success(result, "OTP sent successfully."));
        } catch (Exception e) {
            log.error("Resend OTP failed for: {}", contact, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());
        
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful."));
        } catch (Exception e) {
            log.error("Login failed for: {}", request.getEmailOrPhone(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        try {
            LoginResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully."));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Forgot password - send reset OTP
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for: {}", request.getEmailOrPhone());
        
        try {
            String result = authService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.success(result, "Password reset OTP sent successfully."));
        } catch (Exception e) {
            log.error("Forgot password failed for: {}", request.getEmailOrPhone(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Reset password with OTP
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt for: {}", request.getEmailOrPhone());
        
        try {
            String result = authService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.success(result, "Password reset successfully."));
        } catch (Exception e) {
            log.error("Password reset failed for: {}", request.getEmailOrPhone(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request");
        
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            authService.logout(token);
            return ResponseEntity.ok(ApiResponse.success("Logged out", "Logout successful."));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        try {
            boolean exists = authService.emailExists(email);
            return ResponseEntity.ok(ApiResponse.success(exists, "Email check completed."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

    /**
     * Check if phone exists
     */
//    @GetMapping("/check-phone")
//    public ResponseEntity<ApiResponse<Boolean>> checkPhone(@RequestParam String phone) {
//        try {
//            boolean exists = authService.phoneExists(phone);
//            return ResponseEntity.ok(ApiResponse.success(exists, "Phone check completed."));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(ApiResponse.error( e.getMessage()));
//}
//}