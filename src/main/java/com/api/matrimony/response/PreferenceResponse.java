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

	    private String maritalStatuses; // comma-separated
	    private String religions;      // comma-separated
	    private String castes;         // comma-separated
	    private String educations;     // comma-separated
	    private String occupations;    // comma-separated

	    private BigDecimal minIncome;
	    private BigDecimal maxIncome;

	    private String cities;        // comma-separated
	    private String states;        // comma-separated
	    private String countries ; // default value

	    private String gender;
	    private String subCastes;
	    private String motherTongue;
	    private String familyTypes;
	    private String diets;
	
}
