package com.api.matrimony.request;

import java.util.HashMap;
import java.util.Map;

import com.api.matrimony.entity.UserProfile;

import lombok.Data;

@Data
public class RecommendationScore {
  private final UserProfile profile;
  private final Map<String, Double> scoreBreakdown = new HashMap<>();
  private double totalScore = 0.0;
  
  public RecommendationScore(UserProfile profile) {
      this.profile = profile;
  }
  
  public void addScore(String category, double score) {
      scoreBreakdown.put(category, score);
      totalScore += score;
  }
}