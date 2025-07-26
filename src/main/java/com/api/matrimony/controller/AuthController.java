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
import com.api.matrimony.response.APIResonse;
import com.api.matrimony.response.LoginResponse;
import com.api.matrimony.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication Controller handling user registration, login, and token management
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<APIResonse<String>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());
        
        APIResonse<String> response = new APIResonse<>();
            String result = authService.register(request);
            response.setData(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Verify OTP for account activation
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<APIResonse<String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification attempt for: {}", request.getEmail());
        
        APIResonse<String> response = new APIResonse<>();
            String result = authService.verifyOtp(request);
            response.setData(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Resend OTP
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<APIResonse<String>> resendOtp(@RequestParam String contact, @RequestParam String purpose) {
        log.info("Resend OTP request for: {}", contact);
        
        APIResonse<String> response = new APIResonse<>();
            String result = authService.resendOtp(contact, purpose);
            response.setData(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<APIResonse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());
        
        APIResonse<LoginResponse> response = new APIResonse<>();
            LoginResponse logInRes = authService.login(request);
            response.setData(logInRes);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Refresh JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<APIResonse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        
        APIResonse<LoginResponse> response = new APIResonse<>();
            LoginResponse refreshToken = authService.refreshToken(request.getRefreshToken());
            response.setData(refreshToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Forgot password - send reset OTP
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<APIResonse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for: {}", request.getEmailOrPhone());
        
        APIResonse<String> response = new APIResonse<>();
            String result = authService.forgotPassword(request);
            response.setData(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Reset password with OTP
     */
    @PostMapping("/reset-password")
    public ResponseEntity<APIResonse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset attempt for: {}", request.getEmailOrPhone());
        
        APIResonse<String> response = new APIResonse<>();
            String result = authService.resetPassword(request);
            response.setData(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<APIResonse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request");
        
        APIResonse<String> response = new APIResonse<>();
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            authService.logout(token);
            response.setData("User successfully Logout user");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<APIResonse<Boolean>> checkEmail(@RequestParam String email) {
    	APIResonse<Boolean> response = new APIResonse<>();
            boolean exists = authService.emailExists(email);
            response.setData(exists);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

  