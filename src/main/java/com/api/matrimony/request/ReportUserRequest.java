package com.api.matrimony.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Report User Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportUserRequest {
    
    @NotNull(message = "Reported user ID is required")
    private Long reportedUserId;
    
    @NotBlank(message = "Reason is required")
    private String reason;
    
    private String description;
}

