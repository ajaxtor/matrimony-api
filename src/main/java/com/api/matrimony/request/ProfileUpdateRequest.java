package com.api.matrimony.request;

import java.time.LocalDate;

import com.api.matrimony.enums.AnnualIncomeRanges;
import com.api.matrimony.enums.BadHabits;
import com.api.matrimony.enums.ManglikStatus;

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
    private String nickName; // ✅ New field
    private LocalDate dateOfBirth;
    private String gender;
    private Integer height;
    private Integer weight;
    private String maritalStatus;
    private String religion;
    private String caste;
    private String subCaste;
    private String gothra; // ✅ New field
    private ManglikStatus manglikStatus; // ✅ New field (enum)
    private String motherTongue;
    private String education;
    private String occupation;
    private AnnualIncomeRanges annualIncome;
    private String aboutMe;
    private String familyType;
    private String familyValue;
    private String area; // ✅ New field
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String diet;
    private BadHabits smokingHabits; // ✅ New field (enum)
    private BadHabits drinkingHabits; // ✅ New field (enum)
    private String profileCreatedBy;
}
