package com.api.matrimony.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Match Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private Long userId;
    private String name;
    private Integer age;
    private String gender;
    private LocationResponce location;
    private String education;
    private String occupation;
    private Boolean hasPhotos;
    private Double matchScore;
}