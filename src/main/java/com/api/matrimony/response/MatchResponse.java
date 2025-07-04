package com.api.matrimony.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Match Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchResponse {
    
    private Long id;
    private Long userId;
    private Long matchedUserId;
    private String status;
    private BigDecimal matchScore;
    private LocalDateTime matchedAt;
    private ProfileResponse matchedUserProfile;
    private Boolean canChat;
}
