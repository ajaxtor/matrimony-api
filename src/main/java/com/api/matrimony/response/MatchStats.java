package com.api.matrimony.response;


import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Match Statistics Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchStats {
    
    // Basic match counts
    private Long totalMatches;
    private Long pendingMatches;
    private Long acceptedMatches;
    private Long rejectedMatches;
    private Long mutualMatches;
    
    // Calculated rates and percentages
    private Double acceptanceRate; // (accepted / total) * 100
    private Double mutualMatchRate; // (mutual / accepted) * 100
    private Double responseRate; // ((accepted + rejected) / total) * 100
    
    // Profile engagement stats
    private Integer profileViews;
    private Integer profileLikes;
    private Integer profileViewsToday;
    private Integer profileViewsThisWeek;
    private Integer profileViewsThisMonth;
    
    // Recent activity
    private Integer newMatchesToday;
    private Integer newMatchesThisWeek;
    private Integer newMatchesThisMonth;
    
    // Premium features usage (if applicable)
    private Integer premiumProfileViews;
    private Integer extendedMatchesUsed;
    private Boolean hasPremiumFeatures;
    
    // Time-based statistics
    private String mostActiveDay; // Day of week with most activity
    private String mostActiveTime; // Time of day with most activity
    
    // Recommendation engine stats
    private Double averageMatchScore;
    private Integer totalRecommendations;
    private Integer recommendationsAccepted;
    
    // Subscription related stats
    private String currentSubscriptionTier;
    private Integer remainingMatches; // For limited plans
    private Boolean hasUnlimitedMatches;
}

