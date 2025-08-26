package com.api.matrimony.service;

import java.util.List;

import com.api.matrimony.response.MatchActionResponse;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.ProfileResponse;

/**
 *  Match Service Interface
 */

public interface MatchService {
	List<MatchResponse> findBestMatches(Long loginUserId);

	List<ProfileResponse> getMutualMatches(Long userId);

	MatchActionResponse handleMatchAction(Long userId, String matchId, String action);

	List<MatchResponse> getRecommendations(Long userId);

	MatchResponse getMatchDetails(Long userId, String matchId);
//    String generateMatchesForUser(Long userId);
//    MatchStats getMatchStats(Long userId);
//    void processMatchingAlgorithm();
//    Double calculateMatchScore(Long userId1, Long userId2);

	List<ProfileResponse> searchFilterProfiles(Long userId, String name);

	MatchResponse sendRequest(Long id, String matchId);

	List<ProfileResponse> getSendRequestList(Long id);

	List<ProfileResponse> getReceiveRequests(Long id);
}
