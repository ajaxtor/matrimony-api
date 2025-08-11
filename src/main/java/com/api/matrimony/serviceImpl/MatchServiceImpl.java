 package com.api.matrimony.serviceImpl;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.UserPreferenceRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.service.MatchService;
import com.api.matrimony.utils.MatchingAlgorithm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Match Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchServiceImpl implements MatchService {
	
	  
    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final MatchingAlgorithm matchingAlgorithm;
    
    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> findBestMatches(Long loginUserId) {
        log.info("Finding best matches for user: {}", loginUserId);
        
        // Get user preferences
        UserPreference preferences = userPreferenceRepository.findByUserId(loginUserId)
                .orElseThrow(() ->  new ApplicationException(ErrorEnum.NO_MATCH_FUND_BTWN_USER.toString(),
    					ErrorEnum.NO_MATCH_FUND_BTWN_USER.getExceptionError(), HttpStatus.OK));
        
        // Get all candidate user profiles (excluding the login user)
        
        List<UserProfile> candidateProfiles = userProfileRepository.findAllByUserIdNot(loginUserId);
        
        // Calculate match scores and filter
        List<MatchResponse> matches = candidateProfiles.stream()
                .map(candidate -> matchingAlgorithm.calculateMatchScore(candidate, preferences))
                .filter(match -> match.getMatchScore() > 0) // Filter out 0% matches
                .sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore())) // Sort desc
                .limit(10)
                .collect(Collectors.toList());
        
        log.info("Found {} matches for user: {}", matches.size(), loginUserId);
        return matches;
    }
	
}