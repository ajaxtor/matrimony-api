package com.api.matrimony.request;

import java.time.LocalDate;

import com.api.matrimony.enums.AnnualIncomeRanges;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile Update Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private Integer height;
    private Integer weight;
    private String maritalStatus;
    private String religion;
    private String caste;
    private String subCaste;
    private String motherTongue;
    private String education;
    private String occupation;
    private AnnualIncomeRanges annualIncome;
    private String aboutMe;
    private String familyType;
    private String familyValue;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String diet;
    private String profileCreatedBy;
}
