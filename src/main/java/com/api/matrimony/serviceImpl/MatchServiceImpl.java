 package com.api.matrimony.serviceImpl;


import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.BlockedUser;
import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.enums.UserType;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.mapper.MatchMapper;
import com.api.matrimony.repository.MatchRepository;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.repository.UserPreferenceRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.RecommendationScore;
import com.api.matrimony.request.SearchRequest;
import com.api.matrimony.response.GetMatchResponce;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.MatchService;
import com.api.matrimony.service.NotificationService;
import com.api.matrimony.utils.MatchingAlgorithm;
import com.api.matrimony.utils.ProfileSpecification;
import com.api.matrimony.utils.RecommendationCal;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
	
	private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final MatchingAlgorithm matchingAlgorithm;
    private final MatchRepository matchRepository;
    private final RecommendationCal recommendationCal;
    private final UserPhotoRepository photoRepository;
    private final ProfileSpecification profileSpecification;
    NotificationService notificationService;
    
    @Override
    @Transactional
    public List<GetMatchResponce> findBestMatches(Long loginUserId) {
        log.info("Finding best matches for user: {}", loginUserId);
        
        // Get user preferences
        UserPreference preferences = userPreferenceRepository.findByUserId(loginUserId)
                .orElseThrow(() ->  new ApplicationException(ErrorEnum.NO_MATCH_FUND_BTWN_USER.toString(),
    					ErrorEnum.NO_MATCH_FUND_BTWN_USER.getExceptionError(), HttpStatus.OK));
        
        User loginUserInfo = getUser(loginUserId);
        
        // Get all candidate user profiles (excluding the login user)
        
        List<UserProfile> candidateProfiles = userProfileRepository.findAllByUserIdNot(loginUserId);
        
        // Calculate match scores and filter
        List<GetMatchResponce> matches = candidateProfiles.stream()
                .map(candidate -> matchingAlgorithm.calculateMatchScore(candidate, preferences))
                .filter(match -> match.getMatchScore() > 0) // Filter out 0% matches
                .sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore())) // Sort desc
                .limit(10)
                .collect(Collectors.toList());
        
        log.info("Found {} matches for user: {}", matches.size(), loginUserId);
//        List<Match> setMatches = MatchMapper.toEntityList(matches, loginUserInfo, userRepository);
//        matchRepository.saveAllAndFlush(setMatches);
//        log.info("Save matches to DB for user: {}", setMatches.size(), setMatches);
        return matches;
    }

    public List<MatchResponse> getMutualMatches(Long userId) {
        log.error("Getting mutual matches for user: {}", userId);
        List<Match> mutualMatches = matchRepository.findMutualMatchesByUserId(userId);
        List<MatchResponse> matches = MatchMapper.toResponseList(mutualMatches);
        log.error("List of mutual matches -> "+matches);
        return matches;
    }
    

    @Override
    public String handleMatchAction(Long userId, Long matchId, String action) {
        log.info("Handling match action for user: {}, matchId: {}, action: {}", userId, matchId, action);
        
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
    					ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));

        if (!match.getUser().getId().equals(userId)) {
            throw new ApplicationException(ErrorEnum.ONLY_ACT_OWN_MATCH.toString(),
					ErrorEnum.ONLY_ACT_OWN_MATCH.getExceptionError(), HttpStatus.OK);
        }

        MatchStatus newStatus = action.equalsIgnoreCase("ACCEPT") ? MatchStatus.ACCEPTED : MatchStatus.REJECTED;
        match.setStatus(newStatus);
        matchRepository.save(match);

        // If accepted, check if it's mutual
        if (newStatus == MatchStatus.ACCEPTED) {
            Optional<Match> reverseMatch = matchRepository.findByUserIdAndMatchedUserId(
                    match.getMatchedUser().getId(), match.getUser().getId());
            
            if (reverseMatch.isPresent() && reverseMatch.get().getStatus() == MatchStatus.ACCEPTED) {
                // Update both matches to MUTUAL
                match.setStatus(MatchStatus.MUTUAL);
                reverseMatch.get().setStatus(MatchStatus.MUTUAL);
                matchRepository.save(match);
                matchRepository.save(reverseMatch.get());
                
                // Send mutual match notification
                notificationService.notifyMutualMatch(userId, match.getMatchedUser().getId());
                
                return "Congratulations! It's a mutual match!";
            } else {
                // Send match acceptance notification
                notificationService.notifyNewMatch(match.getMatchedUser().getId(), userId);
                return "Match accepted successfully";
            }
        }

        return "Match " + action.toLowerCase() + "ed successfully";
    }
    
    public User getUser(Long loginUserId) {
    User loginUserInfo = userRepository.findById(loginUserId)
            .orElseThrow(() ->  new ApplicationException(ErrorEnum.INVALID_USER.toString(),
					ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));
    
    return loginUserInfo;
    }
    
    public UserProfile getUserProfileForLogInUser(Long loginUserId) {
    	UserProfile userProfile = getUser(loginUserId).getProfile();
        return userProfile;
        }
    
    public UserPreference getUserPreferenceForLogInUser(Long loginUserId) {
    	UserPreference getUserPreference = getUser(loginUserId).getPreferences();
        return getUserPreference;
        }


    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getRecommendations(Long userId) {
        log.info("Getting recommendations for user: {}", userId);
        
        // Get user's profile
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        
        // Get all candidate profiles
        List<UserProfile> candidateProfiles = userProfileRepository.findAllByUserIdNot(userId);
        
        // Filter out already matched/connected users
        Set<Long> excludedUserIds = getExcludedUserIds(userId);
        candidateProfiles = candidateProfiles.stream()
                .filter(profile -> !excludedUserIds.contains(profile.getUser().getId()))
                .collect(Collectors.toList());
        
        // Calculate recommendation scores
        List<RecommendationScore> recommendations = candidateProfiles.stream()
                .map(candidate -> recommendationCal.calculateRecommendationScore(userProfile, candidate))
                .filter(score -> score.getTotalScore() > 0)
                .sorted((r1, r2) -> Double.compare(r2.getTotalScore(), r1.getTotalScore()))
                .limit(10) // Return top 20 recommendations
                .collect(Collectors.toList());
        
        // Convert to MatchResponse
        List<MatchResponse> matchResponses = recommendations.stream()
                .map(rec -> recommendationCal.buildMatchResponse(rec.getProfile(), rec.getTotalScore()))
                .collect(Collectors.toList());
        
        log.info("Found {} recommendations for user: {}", matchResponses.size(), userId);
        return matchResponses;
    }
    
    private Set<Long> getExcludedUserIds(Long userId) {
        Set<Long> excludedIds = new HashSet<>();
        
        // Exclude users already matched
        List<Match> existingMatches = matchRepository.findByUserId(userId);
        excludedIds.addAll(existingMatches.stream()
                .map(match -> match.getMatchedUser().getId())
                .collect(Collectors.toSet()));
        
        // Also exclude users who matched with current user
        List<Match> reverseMatches = matchRepository.findByMatchedUserId(userId);
        excludedIds.addAll(reverseMatches.stream()
                .map(match -> match.getUser().getId())
                .collect(Collectors.toSet()));
        
        return excludedIds;
    }
    
    @Override
    public MatchResponse getMatchDetails(Long userId, Long matchId) {
        log.info("Getting match details for user: {}, matchId: {}", userId, matchId);
        
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
						ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));

        if (!match.getUser().getId().equals(userId) && !match.getMatchedUser().getId().equals(userId)) {
            throw new ApplicationException(ErrorEnum.ONLY_VIEW_OWN_MATCH.toString(),
					ErrorEnum.ONLY_VIEW_OWN_MATCH.getExceptionError(), HttpStatus.OK);
        }

        MatchResponse setMatches = MatchMapper.toResponse(match);
        log.error("Getting match details for user: "+setMatches);
        return setMatches;
    }

    @Override
    public PagedResponse<ProfileResponse> searchFilterProfiles(Long userId, SearchRequest criteria, Pageable pageable) {
        log.info("Searching profiles for user: {}, criteria: {}", userId, criteria);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorEnum.INVALID_USER.toString(),
                        ErrorEnum.INVALID_USER.getExceptionError(),
                        HttpStatus.OK
                ));

        UserType oppositeType = currentUser.getUserType() == UserType.BRIDE ? UserType.GROOM : UserType.BRIDE;

        // Build base specification
       // Specification<UserProfile> spec = searchRepository.searchProfiles(userId, oppositeType, criteria);
        Specification<UserProfile> spec = profileSpecification.searchProfiles(userId, oppositeType, criteria);

        // Exclude blocked users at DB level
        spec = spec.and((root, query, cb) -> {
            Subquery<Long> blockedSubQuery = query.subquery(Long.class);
            Root<BlockedUser> blockedRoot = blockedSubQuery.from(BlockedUser.class);
            blockedSubQuery.select(blockedRoot.get("blockedUser").get("id"))
                    .where(cb.equal(blockedRoot.get("blocker").get("id"), userId));

            Subquery<Long> blockedByOthersSubQuery = query.subquery(Long.class);
            Root<BlockedUser> blockedByOthersRoot = blockedByOthersSubQuery.from(BlockedUser.class);
            blockedByOthersSubQuery.select(blockedByOthersRoot.get("blocker").get("id"))
                    .where(cb.equal(blockedByOthersRoot.get("blockedUser").get("id"), userId));

            return cb.and(
                    cb.not(root.get("user").get("id").in(blockedSubQuery)),
                    cb.not(root.get("user").get("id").in(blockedByOthersSubQuery))
            );
        });

        // Query DB with pagination
        Page<UserProfile> profilePage = userProfileRepository.findAll(spec, pageable);

        // Map results
        List<ProfileResponse> responses = profilePage.getContent().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProfileResponse>builder()
                .content(responses)
                .page(profilePage.getNumber())
                .size(profilePage.getSize())
                .totalElements(profilePage.getTotalElements())
                .totalPages(profilePage.getTotalPages())
                .first(profilePage.isFirst())
                .last(profilePage.isLast())
                .empty(profilePage.isEmpty())
                .build();
    }

    public ProfileResponse mapToProfileResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setFullName(profile.getFullName() );
        response.setDateOfBirth(profile.getDateOfBirth());
        if (profile.getDateOfBirth() != null) {
            response.setAge(Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears());
        }
        
        response.setGender(profile.getGender() != null ? profile.getGender().name() : null);
        response.setHeight(profile.getHeight());
        response.setWeight(profile.getWeight());
        response.setMaritalStatus(profile.getMaritalStatus() != null ? 
                                 profile.getMaritalStatus().name() : null);
        response.setReligion(profile.getReligion());
        response.setCaste(profile.getCaste());
        response.setSubCaste(profile.getSubCaste());
        response.setMotherTongue(profile.getMotherTongue());
        response.setEducation(profile.getEducation());
        response.setOccupation(profile.getOccupation());
        response.setAnnualIncome(profile.getAnnualIncome());
        response.setAboutMe(profile.getAboutMe());
        response.setFamilyType(profile.getFamilyType());
        response.setFamilyValue(profile.getFamilyValue());
        response.setCity(profile.getCity());
        response.setState(profile.getState());
        response.setCountry(profile.getCountry());
        response.setPincode(profile.getPincode());
        response.setProfileCreatedBy(profile.getProfileCreatedBy());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        response.setDiet(profile.getDiet());

        // Get photos
        List<UserPhoto> photos = photoRepository.findByUserIdOrderByDisplayOrderAsc(profile.getUser().getId());
        response.setPhotoUrls(photos.stream().map(UserPhoto::getPhotoUrl).collect(Collectors.toList()));
        
        // Get primary photo
        photos.stream()
                .filter(UserPhoto::getIsPrimary)
                .findFirst()
                .ifPresent(photo -> response.setPrimaryPhotoUrl(photo.getPhotoUrl()));

        return response;
    }
}