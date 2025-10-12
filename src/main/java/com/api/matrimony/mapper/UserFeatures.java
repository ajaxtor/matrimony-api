package com.api.matrimony.mapper;

import lombok.Data;

@Data
public class UserFeatures {

    private Integer chatLimit;
    private boolean manualSearch;
    private Integer matchesPerWeek;
    private boolean sendInterestOption;
}

