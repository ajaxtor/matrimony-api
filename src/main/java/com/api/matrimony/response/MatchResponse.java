package com.api.matrimony.response;

import com.api.matrimony.enums.MatchStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Match Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
	private String matchId;
	private Long userId;
	private Double matchScore;
	private MatchStatus matchStatus;
    private ProfileResponse profileResponse;
}