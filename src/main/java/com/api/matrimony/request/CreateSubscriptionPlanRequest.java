package com.api.matrimony.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Subscription Plan Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionPlanRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Price is required")
    private BigDecimal price;
    
    @NotNull(message = "Duration in months is required")
    private Integer durationMonths;
    
    private String features; // JSON string
}
