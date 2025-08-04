package com.api.matrimony.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Preference Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceRequest {
    
    @Min(value = 18, message = "Minimum age must be at least 18")
    @Max(value = 80, message = "Minimum age cannot exceed 80")
    private Integer minAge;
    
    @Min(value = 18, message = "Maximum age must be at least 18")
    @Max(value = 80, message = "Maximum age cannot exceed 80")
    private Integer maxAge;
    
    private Integer minHeight;
    private Integer maxHeight;
    private String maritalStatus; // comma separated
    private String religion; // comma separated
    private String caste; // comma separated
    private String education; // comma separated
    private String occupation; // comma separated
    private BigDecimal minIncome;
    private BigDecimal maxIncome;
    private String cities; // comma separated
    private String states; // comma separated
    private String countries;
    private String gender;
    private String subCaste;
    private String motherTongue;
    private String familyType;
    private String diet;
}


