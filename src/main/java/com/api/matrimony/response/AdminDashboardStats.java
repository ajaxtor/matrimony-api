package com.api.matrimony.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin Dashboard Stats Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDashboardStats {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long totalMatches;
    private Long mutualMatches;
    private Long totalReports;
    private Long pendingReports;
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Double userGrowthRate;
    private Double matchSuccessRate;
}
