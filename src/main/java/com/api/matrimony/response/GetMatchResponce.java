package com.api.matrimony.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetMatchResponce {
	private Double matchScore;
	private ProfileResponse profileResponse;
}
