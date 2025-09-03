package com.api.matrimony.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@AllArgsConstructor
@NoArgsConstructor
public class PlanFeatures {

    private int chatLimit;
    private boolean manualSearch;
    private int matchesPerWeek;
    private boolean sendInterestOption;

}

