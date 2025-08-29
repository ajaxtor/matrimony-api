package com.api.matrimony.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {
    
    private Long userId;
    private String fullName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private Integer age;
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
    private BigDecimal annualIncome;
    private String aboutMe;
    private String familyType;
    private String familyValue;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String diet;
    private String profileCreatedBy;
    private List<String> photoUrls;
    private String primaryPhotoUrl;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;
}
