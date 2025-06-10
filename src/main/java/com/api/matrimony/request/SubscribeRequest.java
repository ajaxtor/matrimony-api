package com.api.matrimony.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subscribe Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
    
    @NotBlank(message = "Payment ID is required")
    private String paymentId;
}
