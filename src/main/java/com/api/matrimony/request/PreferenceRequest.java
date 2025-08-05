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
    private String maritalStatuses; // comma separated
    private String religions; // comma separated
    private String castes; // comma separated
    private String educations; // comma separated
    private String occupations; // comma separated
    private BigDecimal minIncome;
    private BigDecimal maxIncome;
    private String cities; // comma separated
    private String states; // comma separated
    private String countries;
    private String gender;
    private String subCastes;
    private String motherTongue;
    private String familyTypes;
    private String diets;
}


