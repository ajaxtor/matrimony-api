 package com.api.matrimony.serviceImpl;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.api.matrimony.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.enums.UserType;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.BlockedUserRepository;
import com.api.matrimony.repository.MatchRepository;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.repository.UserPreferenceRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.SearchCriteria;
import com.api.matrimony.service.MatchService;
import com.api.matrimony.service.NotificationService;

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
    private final UserProfileRepository profileRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final MatchRepository matchRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserPhotoRepository photoRepository;
    private final NotificationService notificationService;

    @Override
    public PagedResponse<MatchResponse> getMatchesForUser(Long userId, String status, Pageable pageable) {
        log.info("Getting matches for user: {}, status: {}", userId, status);
        
        Page<Match> matchPage;
        
        if (status != null && !status.isEmpty()) {
            MatchStatus matchStatus = MatchStatus.valueOf(status.toUpperCase());
            matchPage = matchRepository.findByUserIdAndStatusOrderByMatchedAtDesc(userId, matchStatus, pageable);
        } else {
            // Fix: Get all matches as a list, then create a manual page
            List<Match> allMatches = matchRepository.findByUserIdOrderByMatchedAtDesc(userId);
            
            // Calculate pagination manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allMatches.size());
            List<Match> pageContent = allMatches.subList(start, end);
            
            // Create Page object manually
            matchPage = new PageImpl<>(pageContent, pageable, allMatches.size());
        }

        List<MatchResponse> matchResponses = matchPage.getContent().stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());

        return PagedResponse.<MatchResponse>builder()
                .content(matchResponses)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(matchPage.getTotalElements())
                .totalPages(matchPage.getTotalPages())
                .first(matchPage.isFirst())
                .last(matchPage.isLast())
                .empty(matchPage.isEmpty())
                .build();
    }

    @Override
    public List<MatchResponse> getMutualMatches(Long userId) {
        log.info("Getting mutual matches for user: {}", userId);
        
        List<Match> mutualMatches = matchRepository.findMutualMatchesByUserId(userId);
        return mutualMatches.stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());
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

        MatchStatus newStatus = MatchStatus.valueOf(action.toUpperCase());
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

    @Override
    public PagedResponse<ProfileResponse> searchProfiles(Long userId, SearchCriteria criteria, Pageable pageable) {
        log.info("Searching profiles for user: {}, criteria: {}", userId, criteria);
        
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

        // Get opposite gender profiles
        UserType oppositeType = currentUser.getUserType() == UserType.BRIDE ? UserType.GROOM : UserType.BRIDE;
        
        Page<UserProfile> profilePage = profileRepository.findProfilesWithFilters(
                criteria.getCity(),
                criteria.getState(),
                criteria.getReligion(),
                criteria.getMinAge(),
                criteria.getMaxAge(),
                pageable);

        // Filter profiles based on additional criteria and exclude blocked users
        List<ProfileResponse> filteredProfiles = profilePage.getContent().stream()
                .filter(profile -> !profile.getUser().getId().equals(userId)) // Exclude self
                .filter(profile -> profile.getUser().getUserType() == oppositeType) // Opposite gender
                .filter(profile -> !blockedUserRepository.existsByBlockerIdAndBlockedUserId(userId, profile.getUser().getId())) // Not blocked by current user
                .filter(profile -> !blockedUserRepository.existsByBlockerIdAndBlockedUserId(profile.getUser().getId(), userId)) // Current user not blocked by profile owner
                .filter(profile -> matchesCriteria(profile, criteria))
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProfileResponse>builder()
                .content(filteredProfiles)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements((long) filteredProfiles.size())
                .totalPages((int) Math.ceil((double) filteredProfiles.size() / pageable.getPageSize()))
                .first(pageable.getPageNumber() == 0)
                .last(filteredProfiles.size() <= (pageable.getPageNumber() + 1) * pageable.getPageSize())
                .empty(filteredProfiles.isEmpty())
                .build();
    }

    @Override
    public List<MatchResponse> getRecommendations(Long userId, int limit) {
        log.info("Getting recommendations for user: {}, limit: {}", userId, limit);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

        UserPreference preferences = user.getPreferences();
        if (preferences == null) {
            // Generate basic recommendations without preferences
            return generateBasicRecommendations(userId, limit);
        }

        // Get potential matches based on preferences
        List<UserProfile> potentialMatches = findPotentialMatches(user, preferences, limit * 2); // Get more to filter

        // Calculate match scores and sort
        List<Match> recommendations = potentialMatches.stream()
                .map(profile -> {
                    Double score = calculateMatchScore(userId, profile.getUser().getId());
                    return createMatchRecommendation(user, profile.getUser(), score);
                })
                .sorted((m1, m2) -> m2.getMatchScore().compareTo(m1.getMatchScore()))
                .limit(limit)
                .collect(Collectors.toList());

        // Save recommendations
        matchRepository.saveAll(recommendations);

        return recommendations.stream()
                .map(this::mapToMatchResponse)
                .collect(Collectors.toList());
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

        return mapToMatchResponse(match);
    }

    @Override
    public String generateMatchesForUser(Long userId) {
        log.info("Generating matches for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

        List<MatchResponse> recommendations = getRecommendations(userId, 6);
        
        if (!recommendations.isEmpty()) {
            notificationService.sendMatchRecommendations(userId);
        }

        return String.format("Generated %d new matches for user", recommendations.size());
    }

    @Override
    public MatchStats getMatchStats(Long userId) {
        log.info("Getting match statistics for user: {}", userId);
        
        // Fix: Get counts properly using custom queries or manual counting
        List<Match> userMatches = matchRepository.findByUserIdOrderByMatchedAtDesc(userId);
        
        Long totalMatches = (long) userMatches.size();
        Long pendingMatches = userMatches.stream()
                .mapToLong(match -> match.getStatus() == MatchStatus.PENDING ? 1 : 0)
                .sum();
        Long acceptedMatches = userMatches.stream()
                .mapToLong(match -> match.getStatus() == MatchStatus.ACCEPTED ? 1 : 0)
                .sum();
        Long rejectedMatches = userMatches.stream()
                .mapToLong(match -> match.getStatus() == MatchStatus.REJECTED ? 1 : 0)
                .sum();
        Long mutualMatches = userMatches.stream()
                .mapToLong(match -> match.getStatus() == MatchStatus.MUTUAL ? 1 : 0)
                .sum();

        Double acceptanceRate = totalMatches > 0 ? 
                (acceptedMatches.doubleValue() / totalMatches.doubleValue()) * 100 : 0.0;

        return MatchStats.builder()
                .totalMatches(totalMatches)
                .pendingMatches(pendingMatches)
                .acceptedMatches(acceptedMatches)
                .rejectedMatches(rejectedMatches)
                .mutualMatches(mutualMatches)
                .acceptanceRate(acceptanceRate)
                .profileViews(0) // Placeholder
                .profileLikes(acceptedMatches.intValue())
                .build();
    }

    @Override
    @Scheduled(cron = "0 0 9 * * MON,WED,FRI") // Run at 9 AM on Monday, Wednesday, Friday
    public void processMatchingAlgorithm() {
        log.info("Processing matching algorithm for all users");
        
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        
        for (User user : activeUsers) {
            try {
                generateMatchesForUser(user.getId());
            } catch (Exception e) {
                log.error("Error generating matches for user: {}", user.getId(), e);
            }
        }
        
        log.info("Completed matching algorithm processing for {} users", activeUsers.size());
    }

    @Override
    public Double calculateMatchScore(Long userId1, Long userId2) {
        log.debug("Calculating match score between users: {} and {}", userId1, userId2);
        
        UserProfile profile1 = profileRepository.findByUserId(userId1).orElse(null);
        UserProfile profile2 = profileRepository.findByUserId(userId2).orElse(null);
        UserPreference pref1 = preferenceRepository.findByUserId(userId1).orElse(null);
        UserPreference pref2 = preferenceRepository.findByUserId(userId2).orElse(null);

        if (profile1 == null || profile2 == null) {
            return 0.0;
        }

        double score = 0.0;
        int factors = 0;

        // Age compatibility (20%)
        if (profile1.getDateOfBirth() != null && profile2.getDateOfBirth() != null) {
            int age1 = Period.between(profile1.getDateOfBirth(), LocalDate.now()).getYears();
            int age2 = Period.between(profile2.getDateOfBirth(), LocalDate.now()).getYears();
            
            if (pref1 != null && pref1.getMinAge() != null && pref1.getMaxAge() != null && 
                age2 >= pref1.getMinAge() && age2 <= pref1.getMaxAge()) {
                score += 20;
            }
            if (pref2 != null && pref2.getMinAge() != null && pref2.getMaxAge() != null && 
                age1 >= pref2.getMinAge() && age1 <= pref2.getMaxAge()) {
                score += 20;
            }
            factors += 2;
        }

        // Location compatibility (15%)
        if (profile1.getCity() != null && profile2.getCity() != null) {
            if (profile1.getCity().equalsIgnoreCase(profile2.getCity())) {
                score += 15;
            } else if (profile1.getState() != null && profile2.getState() != null && 
                      profile1.getState().equalsIgnoreCase(profile2.getState())) {
                score += 10;
            }
            factors++;
        }

        // Religion compatibility (25%)
        if (profile1.getReligion() != null && profile2.getReligion() != null) {
            if (profile1.getReligion().equalsIgnoreCase(profile2.getReligion())) {
                score += 25;
                
                // Caste compatibility (10%)
                if (profile1.getCaste() != null && profile2.getCaste() != null &&
                    profile1.getCaste().equalsIgnoreCase(profile2.getCaste())) {
                    score += 10;
                }
            }
            factors++;
        }

        // Education compatibility (15%)
        if (profile1.getEducation() != null && profile2.getEducation() != null) {
            if (profile1.getEducation().equalsIgnoreCase(profile2.getEducation())) {
                score += 15;
            }
            factors++;
        }

        // Mother tongue compatibility (10%)
        if (profile1.getMotherTongue() != null && profile2.getMotherTongue() != null) {
            if (profile1.getMotherTongue().equalsIgnoreCase(profile2.getMotherTongue())) {
                score += 10;
            }
            factors++;
        }

        // Marital status compatibility (5%)
        if (profile1.getMaritalStatus() != null && profile2.getMaritalStatus() != null) {
            if (profile1.getMaritalStatus() == profile2.getMaritalStatus()) {
                score += 5;
            }
            factors++;
        }

        return factors > 0 ? score / factors : 0.0;
    }

    // Helper methods
    private List<MatchResponse> generateBasicRecommendations(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new ArrayList<>();

        UserType oppositeType = user.getUserType() == UserType.BRIDE ? UserType.GROOM : UserType.BRIDE;
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<User> users = userRepository.findActiveUsersByType(oppositeType, pageable);
        
        return users.getContent().stream()
                .filter(u -> !u.getId().equals(userId))
                .filter(u -> !blockedUserRepository.existsByBlockerIdAndBlockedUserId(userId, u.getId()))
                .map(u -> {
                    Double score = calculateMatchScore(userId, u.getId());
                    return mapToMatchResponse(createMatchRecommendation(user, u, score));
                })
                .collect(Collectors.toList());
    }

    private List<UserProfile> findPotentialMatches(User user, UserPreference preferences, int limit) {
        // This is a simplified implementation
        // In a real application, you would use more sophisticated querying
        
        Pageable pageable = PageRequest.of(0, limit);
        return profileRepository.findProfilesWithFilters(
                extractPreferredCities(preferences.getCities()),
                extractPreferredStates(preferences.getStates()),
                extractPreferredReligions(preferences.getReligion()),
                preferences.getMinAge(),
                preferences.getMaxAge(),
                pageable).getContent();
    }

    private Match createMatchRecommendation(User user, User matchedUser, Double score) {
        Match match = new Match();
        match.setUser(user);
        match.setMatchedUser(matchedUser);
        match.setStatus(MatchStatus.PENDING);
        match.setMatchScore(BigDecimal.valueOf(score != null ? score : 0.0));
        return match;
    }

    private boolean matchesCriteria(UserProfile profile, SearchCriteria criteria) {
        // Implement additional filtering logic here
        if (criteria.getMaritalStatus() != null && profile.getMaritalStatus() != null) {
            if (!criteria.getMaritalStatus().contains(profile.getMaritalStatus().name())) {
                return false;
            }
        }

        if (criteria.getEducation() != null && profile.getEducation() != null) {
            if (!criteria.getEducation().toLowerCase().contains(profile.getEducation().toLowerCase())) {
                return false;
            }
        }

        if (criteria.getOccupation() != null && profile.getOccupation() != null) {
            if (!criteria.getOccupation().toLowerCase().contains(profile.getOccupation().toLowerCase())) {
                return false;
            }
        }

        if (criteria.getMinHeight() != null && profile.getHeight() != null) {
            if (profile.getHeight() < criteria.getMinHeight()) {
                return false;
            }
        }

        if (criteria.getMaxHeight() != null && profile.getHeight() != null) {
            if (profile.getHeight() > criteria.getMaxHeight()) {
                return false;
            }
        }

        return true;
    }

    private String extractPreferredCities(String cities) {
        return cities != null && !cities.isEmpty() ? cities.split(",")[0].trim() : null;
    }

    private String extractPreferredStates(String states) {
        return states != null && !states.isEmpty() ? states.split(",")[0].trim() : null;
    }

    private String extractPreferredReligions(String religions) {
        return religions != null && !religions.isEmpty() ? religions.split(",")[0].trim() : null;
    }

    private MatchResponse mapToMatchResponse(Match match) {
        MatchResponse response = new MatchResponse();
        response.setId(match.getId());
        response.setUserId(match.getUser().getId());
        response.setMatchedUserId(match.getMatchedUser().getId());
        response.setStatus(match.getStatus().name());
        response.setMatchScore(match.getMatchScore());
        response.setMatchedAt(match.getMatchedAt());
        response.setCanChat(match.getStatus() == MatchStatus.MUTUAL);

        // Add matched user profile
        if (match.getMatchedUser().getProfile() != null) {
            response.setMatchedUserProfile(mapToMatchProfileResponse(match.getMatchedUser().getProfile()));
        }

        return response;
    }

    private MatchProfileResponse mapToMatchProfileResponse(UserProfile profile) {
        MatchProfileResponse response = new MatchProfileResponse();

        response.setId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setDateOfBirth(profile.getDateOfBirth());
        response.setPhone(profile.getUser().getPhone());
        response.setEmail(profile.getUser().getEmail());
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
        response.setFamilyValues(profile.getFamilyValues());
        response.setCity(profile.getCity());
        response.setState(profile.getState());
        response.setCountry(profile.getCountry());
        response.setPincode(profile.getPincode());
        response.setProfileCreatedBy(profile.getProfileCreatedBy());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

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

    private ProfileResponse mapToProfileResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setFullName(profile.getFullName());
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
        response.setFamilyValues(profile.getFamilyValues());
        response.setCity(profile.getCity());
        response.setState(profile.getState());
        response.setCountry(profile.getCountry());
        response.setPincode(profile.getPincode());
        response.setProfileCreatedBy(profile.getProfileCreatedBy());
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());

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