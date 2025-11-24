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
    private String passWord;
   // private String otp; 
    private boolean rememberMe = false;
    
    private String provider;  // GOOGLE, FACEBOOK, APPLE

   // @NotBlank(message = "Provider ID is required")
    private String providerId;  

    private String fullName;
    private String profilePic;

//    private Gender gender;
//    private LocalDate dateOfBirth;
//    private Gender lookingFor;
}
