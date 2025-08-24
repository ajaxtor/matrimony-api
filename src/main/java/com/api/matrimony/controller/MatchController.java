package com.api.matrimony.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.entity.User;
import com.api.matrimony.request.MatchActionRequest;
import com.api.matrimony.response.APIResonse;
import com.api.matrimony.response.GetMatchResponce;
import com.api.matrimony.response.MatchActionResponse;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.MatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Match Controller for handling matchmaking operations
 */
@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MatchController {

	@Autowired
	private MatchService matchService;

	/**
	 * Get matches for the current user
	 */
	@GetMapping("/findBestMatches")
	public ResponseEntity<APIResonse<List<MatchResponse>>> findBestMatches(
			@AuthenticationPrincipal User currentUser) {

		log.info("Getting matches for user: {}, page: {}, size: {}, status: {}", currentUser.getId());
		APIResonse<List<MatchResponse>> response = new APIResonse<>();
		List<MatchResponse> matches = matchService.findBestMatches(currentUser.getId());
		response.setData(matches);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Get mutual matches for the current user
	 */
	@GetMapping("/mutual")
	public ResponseEntity<APIResonse<List<MatchResponse>>> getMutualMatches(@AuthenticationPrincipal User currentUser) {

		log.info("Getting mutual matches for user: {}", currentUser.getId());
		APIResonse<List<MatchResponse>> response = new APIResonse<>();
		List<MatchResponse> mutualMatches = matchService.getMutualMatches(currentUser.getId());
		response.setData(mutualMatches);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Accept or reject a match
	 */
	@PostMapping("/action")
	public ResponseEntity<APIResonse<MatchActionResponse>> handleMatchAction(@AuthenticationPrincipal User currentUser,
			@Valid @RequestBody MatchActionRequest request) {

		log.info("Match action by user: {}, matchId: {}, action: {}", currentUser.getId(), request.getMatchId(),
				request.getAction());
		APIResonse<MatchActionResponse> response = new APIResonse<>();
		MatchActionResponse result = matchService.handleMatchAction(currentUser.getId(), request.getMatchId(),
				request.getAction());
		response.setData(result);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping({"/findSearch", "/findSearch/{name}"})
	public ResponseEntity<APIResonse<List<ProfileResponse>>> searchProfiles(@AuthenticationPrincipal User currentUser,
			 @PathVariable(required = false) String name) {
		APIResonse<List<ProfileResponse>> response = new APIResonse<>();
		List<ProfileResponse> result = matchService.searchFilterProfiles(currentUser.getId(), name);
		response.setData(result);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Get recommended matches for user
	 */
	@GetMapping("/recommendations")
	public ResponseEntity<APIResonse<List<MatchResponse>>> getRecommendations(
			@AuthenticationPrincipal User currentUser) {

		log.info("Getting recommendations for user: {}, limit: {}", currentUser.getId());
		APIResonse<List<MatchResponse>> response = new APIResonse<>();
		List<MatchResponse> recommendations = matchService.getRecommendations(currentUser.getId());
		response.setData(recommendations);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

//
	/**
	 * Get match details by ID
	 */
	@GetMapping("matchDetailsByID/{matchId}")
	public ResponseEntity<APIResonse<MatchResponse>> getMatchDetails(@AuthenticationPrincipal User currentUser,
			@PathVariable Long matchId) {

		log.info("Getting match details for user: {}, matchId: {}", currentUser.getId(), matchId);
		APIResonse<MatchResponse> response = new APIResonse<>();
		MatchResponse matchDetails = matchService.getMatchDetails(currentUser.getId(), matchId);
		response.setData(matchDetails);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
//
//    /**
//     * Generate new matches for user (admin or scheduled operation)
//     */
//    @PostMapping("/generateMatch")
//    public ResponseEntity<APIResonse<String>> generateMatches(
//            @AuthenticationPrincipal User currentUser) {
//        
//        log.info("Generating new matches for user: {}", currentUser.getId());
//        APIResonse<String> response = new APIResonse<>();
//            String result = matchService.generateMatchesForUser(currentUser.getId());
//            response.setData(result);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//    }

}
