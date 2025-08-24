package com.api.matrimony.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Match Action Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchActionRequest {
    
    @NotNull(message = "Match ID is required")
    private String matchId;
    
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "ACCEPT|REJECT", message = "Action must be either ACCEPT or REJECT")
    private String action;
}
