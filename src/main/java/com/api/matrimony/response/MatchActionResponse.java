package com.api.matrimony.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchActionResponse {
    private boolean isAccepted;
    private MatchResponse match;
}

