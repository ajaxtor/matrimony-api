package com.api.matrimony.request;

import java.math.BigDecimal;

import com.api.matrimony.enums.MaritalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    private String city;
    private String state;
    private String religion;
    private String caste;
    private String subCaste;
    private Integer minHeight;
    private Integer maxHeight;
    private Integer minWeight;
    private Integer maxWeight;
    private MaritalStatus maritalStatus;
    private String education;
    private String occupation;
    private BigDecimal minIncome;
    private BigDecimal maxIncome;
    private String diet;
    private Integer minAge;
    private Integer maxAge;
}

