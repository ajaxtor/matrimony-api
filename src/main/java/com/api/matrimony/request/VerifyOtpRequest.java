package com.api.matrimony.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Verify OTP Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    
//   // @NotBlank(message = "Contact (email or phone) is required")
//    private String contact;
    
    @NotBlank(message = "email (email or phone) is required")
    private String email;
    
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;
    
    @NotBlank(message = "Purpose is required")
    private String purpose; // REGISTRATION, LOGIN, PASSWORD_RESET
}
