package com.api.matrimony.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
	
	 private String countryCode;
    
    @NotBlank(message = "Email or phone is required")
    private String emailOrPhone;
    private String otp; 
    private boolean rememberMe = false;
}
