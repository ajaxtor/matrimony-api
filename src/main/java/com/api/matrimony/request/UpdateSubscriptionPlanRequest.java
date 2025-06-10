package com.api.matrimony.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update Subscription Plan Request DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionPlanRequest {
    
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMonths;
    private String features; // JSON string
    private Boolean isActive;
}


