package com.api.matrimony.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.api.matrimony.request.SearchCriteria;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.MatchStats;
import com.api.matrimony.response.PagedResponse;
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
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> getMatches(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        log.info("Getting matches for user: {}, page: {}, size: {}, status: {}", 
                currentUser.getId(), page, size, status);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponse<MatchResponse> matches = matchService.getMatchesForUser(
                    currentUser.getId(), status, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(matches, "Matches retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting matches for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get mutual matches for the current user
     */
    @GetMapping("/mutual")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMutualMatches(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting mutual matches for user: {}", currentUser.getId());
        
        try {
            List<MatchResponse> mutualMatches = matchService.getMutualMatches(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(mutualMatches, 
                    "Mutual matches retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting mutual matches for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Accept or reject a match
     */
    @PostMapping("/action")
    public ResponseEntity<ApiResponse<String>> handleMatchAction(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody MatchActionRequest request) {
        
        log.info("Match action by user: {}, matchId: {}, action: {}", 
                currentUser.getId(), request.getMatchId(), request.getAction());
        
        try {
            String result = matchService.handleMatchAction(currentUser.getId(), 
                    request.getMatchId(), request.getAction());
            
            return ResponseEntity.ok(ApiResponse.success(result, "Match action processed successfully"));
        } catch (Exception e) {
            log.error("Error processing match action for user: {}, matchId: {}", 
                    currentUser.getId(), request.getMatchId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Search profiles based on criteria
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProfileResponse>>> searchProfiles(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String religion,
            @RequestParam(required = false) String caste,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Integer minHeight,
            @RequestParam(required = false) Integer maxHeight,
            @RequestParam(required = false) String maritalStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Profile search by user: {}, filters - city: {}, religion: {}, minAge: {}, maxAge: {}", 
                currentUser.getId(), city, religion, minAge, maxAge);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Create search criteria object
            SearchCriteria criteria = SearchCriteria.builder()
                    .city(city)
                    .state(state)
                    .religion(religion)
                    .caste(caste)
                    .education(education)
                    .occupation(occupation)
                    .minAge(minAge)
                    .maxAge(maxAge)
                    .minHeight(minHeight)
                    .maxHeight(maxHeight)
                    .maritalStatus(maritalStatus)
                    .build();
            
            PagedResponse<ProfileResponse> profiles = matchService.searchProfiles(
                    currentUser.getId(), criteria, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(profiles, "Profile search completed"));
        } catch (Exception e) {
            log.error("Error searching profiles for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get recommended matches for user
     */
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getRecommendations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "6") int limit) {
        
        log.info("Getting recommendations for user: {}, limit: {}", currentUser.getId(), limit);
        
        try {
            List<MatchResponse> recommendations = matchService.getRecommendations(
                    currentUser.getId(), limit);
            
            return ResponseEntity.ok(ApiResponse.success(recommendations, 
                    "Recommendations retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting recommendations for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get match details by ID
     */
    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchResponse>> getMatchDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long matchId) {
        
        log.info("Getting match details for user: {}, matchId: {}", currentUser.getId(), matchId);
        
        try {
            MatchResponse matchDetails = matchService.getMatchDetails(currentUser.getId(), matchId);
            return ResponseEntity.ok(ApiResponse.success(matchDetails, 
                    "Match details retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting match details for user: {}, matchId: {}", 
                    currentUser.getId(), matchId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Generate new matches for user (admin or scheduled operation)
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateMatches(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Generating new matches for user: {}", currentUser.getId());
        
        try {
            String result = matchService.generateMatchesForUser(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(result, "New matches generated successfully"));
        } catch (Exception e) {
            log.error("Error generating matches for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get match statistics for user
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MatchStats>> getMatchStats(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting match statistics for user: {}", currentUser.getId());
        
        try {
            MatchStats stats = matchService.getMatchStats(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(stats, 
                    "Match statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting match stats for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
