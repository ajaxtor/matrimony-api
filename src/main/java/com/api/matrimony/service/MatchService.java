package com.api.matrimony.service;

import java.util.List;

import com.api.matrimony.response.MatchResponse;

/**
 * Match Service Interface
 */

public interface MatchService {
	 List<MatchResponse> findBestMatches(Long loginUserId);
	 
//    List<MatchResponse> getMutualMatches(Long userId);
//    String handleMatchAction(Long userId, Long matchId, String action);
//    PagedResponse<ProfileResponse> searchProfiles(Long userId, SearchCriteria criteria, Pageable pageable);
//    List<MatchResponse> getRecommendations(Long userId, int limit);
//    MatchResponse getMatchDetails(Long userId, Long matchId);
//    String generateMatchesForUser(Long userId);
//    MatchStats getMatchStats(Long userId);
//    void processMatchingAlgorithm();
//    Double calculateMatchScore(Long userId1, Long userId2);
}


