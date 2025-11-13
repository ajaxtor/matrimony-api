package com.api.matrimony.request;

import java.time.LocalDate;

import com.api.matrimony.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Register Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number format")
    private String phone;
    @NotBlank(message = "country Code is required")
    private String countryCode;
    
//    @NotBlank(message = "Password is required")
//    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
 //   private String password;
    
    @NotNull(message = "User type is required")
    private Gender gender;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String fullName;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth; 
    @NotNull(message = "looking For is required")
    private Gender lookingFor;
    
}

