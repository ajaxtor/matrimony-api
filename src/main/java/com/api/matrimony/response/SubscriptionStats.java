package com.api.matrimony.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Subscription Stats Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionStats {
    
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Double conversionRate;
    private Double renewalRate;
}


