package com.api.matrimony.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceResponse {

	    private Integer minAge;
	    private Integer maxAge;

	    private Integer minHeight;
	    private Integer maxHeight;

	    private String maritalStatus; // comma-separated
	    private String religion;      // comma-separated
	    private String caste;         // comma-separated
	    private String education;     // comma-separated
	    private String occupation;    // comma-separated

	    private BigDecimal minIncome;
	    private BigDecimal maxIncome;

	    private String cities;        // comma-separated
	    private String states;        // comma-separated
	    private String countries ; // default value

	    private String gender;
	    private String subCaste;
	    private String motherTongue;
	    private String familyType;
	    private String diet;
	
}
