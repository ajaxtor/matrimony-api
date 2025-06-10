package com.api.matrimony.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Report Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {
    
    @NotBlank(message = "Status is required")
    private String status;
    
    private String adminNotes;
}
