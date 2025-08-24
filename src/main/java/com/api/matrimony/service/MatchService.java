package com.api.matrimony.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.api.matrimony.response.GetMatchResponce;
import com.api.matrimony.response.MatchActionResponse;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.ProfileResponse;

/**
 * Match Service Interface
 */

public interface MatchService {
	 List<MatchResponse> findBestMatches(Long loginUserId);
	 
    List<MatchResponse> getMutualMatches(Long userId);
    MatchActionResponse handleMatchAction(Long userId, String matchId, String action);
    List<MatchResponse> getRecommendations(Long userId);
   MatchResponse getMatchDetails(Long userId, Long matchId);
//    String generateMatchesForUser(Long userId);
//    MatchStats getMatchStats(Long userId);
//    void processMatchingAlgorithm();
//    Double calculateMatchScore(Long userId1, Long userId2);

List<ProfileResponse> searchFilterProfiles(Long userId, String name);
}


