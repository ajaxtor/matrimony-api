package com.api.matrimony.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Search Criteria DTO for profile search functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    
    private String city;
    private String state;
    private String religion;
    private String caste;
    private String subCaste;
    private String education;
    private String occupation;
    private Integer minAge;
    private Integer maxAge;
    private Integer minHeight;
    private Integer maxHeight;
    private String maritalStatus;
    private String motherTongue;
    private String familyType;
    private String familyValues;
    private Double minIncome;
    private Double maxIncome;
    private String profileCreatedBy;
    
    // Additional search parameters
    private String sortBy; // age, height, income, created_date
    private String sortOrder; // asc, desc
    private Boolean hasPhoto;
    private Boolean isVerified;
    private String country;
    private String[] cities; // Multiple cities
    private String[] states; // Multiple states
    private String[] religions; // Multiple religions
}

