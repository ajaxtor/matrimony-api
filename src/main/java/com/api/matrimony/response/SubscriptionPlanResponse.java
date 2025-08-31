package com.api.matrimony.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subscription Plan Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlanResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationWeeks;
    private Object features; // JSON object
    private Boolean isActive;
}
