package com.api.matrimony.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.api.matrimony.request.SearchCriteria;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.MatchStats;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.ProfileResponse;

/**
 * Match Service Interface
 */

public interface MatchService {
    PagedResponse<MatchResponse> getMatchesForUser(Long userId, String status, Pageable pageable);
    List<MatchResponse> getMutualMatches(Long userId);
    String handleMatchAction(Long userId, Long matchId, String action);
    PagedResponse<ProfileResponse> searchProfiles(Long userId, SearchCriteria criteria, Pageable pageable);
    List<MatchResponse> getRecommendations(Long userId, int limit);
    MatchResponse getMatchDetails(Long userId, Long matchId);
    String generateMatchesForUser(Long userId);
    MatchStats getMatchStats(Long userId);
    void processMatchingAlgorithm();
    Double calculateMatchScore(Long userId1, Long userId2);
}


