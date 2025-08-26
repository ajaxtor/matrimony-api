package com.api.matrimony.serviceImpl;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.MatchesAction;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.mapper.MatchMapper;
import com.api.matrimony.repository.MatchRepository;
import com.api.matrimony.repository.MatchesActionRepo;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.repository.UserPreferenceRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.RecommendationScore;
import com.api.matrimony.response.MatchActionResponse;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.MatchService;
import com.api.matrimony.service.NotificationService;
import com.api.matrimony.utils.MatchingAlgorithm;
import com.api.matrimony.utils.ProfileSpecification;
import com.api.matrimony.utils.RecommendationCal;

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
	private final NotificationService notificationService;
	private final MatchMapper matchMapper;
	private final MatchesActionRepo matchesActionRepo;

	@Override
	@Transactional
	public List<MatchResponse> findBestMatches(Long loginUserId) {
		log.info("Finding best matches for user: {}", loginUserId);

		// Get preferences
		UserPreference preferences = userPreferenceRepository.findByUserId(loginUserId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.NO_MATCH_FUND_BTWN_USER.toString(),
						ErrorEnum.NO_MATCH_FUND_BTWN_USER.getExceptionError(), HttpStatus.OK));

		User loginUserInfo = getUser(loginUserId);

		// Candidate profiles
		List<UserProfile> candidateProfiles = userProfileRepository.findAllByUserIdNotAndIsHideFalse(loginUserId);

		// Calculate matches
		List<MatchResponse> rawMatches = candidateProfiles.stream()
				.map(candidate -> matchingAlgorithm.calculateMatchScore(candidate, preferences))
				.filter(match -> match.getMatchScore() > 0)
				.sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore())).limit(10).toList();

		// Convert to entities
		List<Match> setMatches = matchMapper.toEntityList(rawMatches, loginUserInfo, userRepository);
		List<Long> matchedUserIds = setMatches.stream().map(m -> m.getMatchedUser().getId()).toList();

		// Fetch existing matches in one go
		List<Match> existingMatches = matchRepository.findExistingMatches(loginUserInfo.getId(), matchedUserIds);
		Map<Long, Match> existingMap = existingMatches.stream()
				.collect(Collectors.toMap(m -> m.getMatchedUser().getId(), m -> m));

		// Final responses
		List<MatchResponse> finalResponses = new ArrayList<>();

		for (MatchResponse response : rawMatches) {
			Match existing = existingMap.get(response.getUserId());
			if (existing != null) {
				// Use existing matchId and status
				response.setMatchId(existing.getMatchId());
				response.setMatchStatus(existing.getStatus()); // <-- set status from DB
			} else {
				// This is new → save it
				Match newMatch = matchMapper.toEntity(response, loginUserInfo, userRepository
						.findById(response.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
				matchRepository.save(newMatch);
				response.setMatchId(newMatch.getMatchId());
				response.setMatchStatus(newMatch.getStatus()); // <-- set status for new match
			}
			finalResponses.add(response);
		}

		log.info("Final {} matches returned for user {}", finalResponses.size(), loginUserId);
		return finalResponses;
	}

	public List<ProfileResponse> getMutualMatches(Long userId) {
		log.error("Getting mutual matches for user: {}", userId);
		List<MatchesAction> mutualMatches = matchesActionRepo.findByFromUserId(userId);
		List<ProfileResponse> mutualProfileRes = mutualMatches.stream()
		        .map(x -> mapToProfileResponse(x.getToUser().getProfile()))
		        .collect(Collectors.toList());

		log.error("List of mutual matches -> " + mutualProfileRes);
		return mutualProfileRes;
	}

	@Override
	public MatchActionResponse handleMatchAction(Long userId, String matchId, String action) {
		log.info("Handling match action for user: {}, matchId: {}, action: {}", userId, matchId, action);

		Match match = matchRepository.findByMatchId(matchId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
						ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));

		MatchStatus newStatus = action.equalsIgnoreCase("ACCEPT") ? MatchStatus.ACCEPTED : MatchStatus.REJECTED;

		// If accepted, check if it's mutual
		if (newStatus == MatchStatus.ACCEPTED) {
			match.setStatus(MatchStatus.MUTUAL);
			matchRepository.save(match);
			MatchesAction matchAction = matchesActionRepo.findByMatchId(matchId)
					.orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
							ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));
			matchAction.setStatus(MatchStatus.MUTUAL);
			matchesActionRepo.save(matchAction);

			// for another user

			MatchesAction oppoSitAction = new MatchesAction();
			oppoSitAction.setMatchId(matchId);
			oppoSitAction.setFromUser(matchAction.getToUser());
			oppoSitAction.setToUser(matchAction.getFromUser());
			oppoSitAction.setStatus(MatchStatus.MUTUAL);
			matchesActionRepo.saveAndFlush(oppoSitAction);

			return new MatchActionResponse(true, "ACCEPTED ", matchMapper.toResponse(match));
		} else {
			match.setStatus(MatchStatus.MATCH);
			matchRepository.save(match);
			MatchesAction matchAction = matchesActionRepo.findByMatchId(matchId)
					.orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
							ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));
			matchAction.setStatus(MatchStatus.REJECTED);
			matchesActionRepo.save(matchAction);
			return new MatchActionResponse(false, "REJECTED ", matchMapper.toResponse(match));
		}

	}

	public User getUser(Long loginUserId) {
		User loginUserInfo = userRepository.findById(loginUserId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
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
		UserProfile userProfile = userProfileRepository.findByUserIdAndIsHideFalse(userId)
				.orElseThrow(() -> new RuntimeException("User profile not found"));

		// Get all candidate profiles
		List<UserProfile> candidateProfiles = userProfileRepository.findAllByUserIdNotAndIsHideFalse(userId);

		// Filter out already matched/connected users
		Set<Long> excludedUserIds = getExcludedUserIds(userId);
		candidateProfiles = candidateProfiles.stream()
				.filter(profile -> !excludedUserIds.contains(profile.getUser().getId())).collect(Collectors.toList());

		// Calculate recommendation scores
		List<RecommendationScore> recommendations = candidateProfiles.stream()
				.map(candidate -> recommendationCal.calculateRecommendationScore(userProfile, candidate))
				.filter(score -> score.getTotalScore() > 0)
				.sorted((r1, r2) -> Double.compare(r2.getTotalScore(), r1.getTotalScore())).limit(10) // Return top 20
																										// recommendations
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
		excludedIds.addAll(
				existingMatches.stream().map(match -> match.getMatchedUser().getId()).collect(Collectors.toSet()));

		// Also exclude users who matched with current user
		List<Match> reverseMatches = matchRepository.findByMatchedUserId(userId);
		excludedIds.addAll(reverseMatches.stream().map(match -> match.getUser().getId()).collect(Collectors.toSet()));

		return excludedIds;
	}

	@Override
	public MatchResponse getMatchDetails(Long userId, String matchId) {
		log.info("Getting match details for user: {}, matchId: {}", userId, matchId);

		Match match = matchRepository.findByMatchId(matchId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
						ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));

		if (!match.getUser().getId().equals(userId) && !match.getMatchedUser().getId().equals(userId)) {
			throw new ApplicationException(ErrorEnum.ONLY_VIEW_OWN_MATCH.toString(),
					ErrorEnum.ONLY_VIEW_OWN_MATCH.getExceptionError(), HttpStatus.OK);
		}

		MatchResponse setMatches = matchMapper.toResponse(match);
		log.error("Getting match details for user: " + setMatches);
		return setMatches;
	}

	@Override
	public List<ProfileResponse> searchFilterProfiles(Long userId, String name) {
		log.info("Searching profiles for user: {}, criteria: {}", userId, name);

		UserPreference fetchUserInfo = getUserProfileByUserId(userId);

		if (name != null && !name.isBlank()) {
			List<UserProfile> fetchUser = userProfileRepository.searchByFullNameAndGender(name,
					fetchUserInfo.getGender());
			if (fetchUser != null && !fetchUser.isEmpty()) {
				return convertProfiles(fetchUser);
			} else {
				throw new ApplicationException(ErrorEnum.USER_NOT_FOUND.toString(),
						ErrorEnum.USER_NOT_FOUND.getExceptionError(), HttpStatus.OK);
			}
		} else {

			// Get user preferences
			UserPreference preferences = userPreferenceRepository.findByUserId(userId)
					.orElseThrow(() -> new ApplicationException(ErrorEnum.NO_MATCH_FUND_BTWN_USER.toString(),
							ErrorEnum.NO_MATCH_FUND_BTWN_USER.getExceptionError(), HttpStatus.OK));

			User loginUserInfo = getUser(userId);

			// Get all candidate user profiles (excluding the login user)
			List<UserProfile> candidateProfiles = userProfileRepository.findAllByUserIdNotAndIsHideFalse(userId);

			// Calculate match scores and filter
			List<ProfileResponse> profileResponses = candidateProfiles.stream()
					.map(candidate -> matchingAlgorithm.calculateMatchScore(candidate, preferences))
					.filter(match -> match.getMatchScore() > 0) // Filter out 0% matches
					.sorted((m1, m2) -> Double.compare(m2.getMatchScore(), m1.getMatchScore())) // Sort desc
					.limit(10).map(MatchResponse::getProfileResponse) // ✅ extract only ProfileResponse
					.collect(Collectors.toList());

			return profileResponses;
		}
	}

	public ProfileResponse mapToProfileResponse(UserProfile profile) {
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
		response.setMaritalStatus(profile.getMaritalStatus() != null ? profile.getMaritalStatus().name() : null);
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
		photos.stream().filter(UserPhoto::getIsPrimary).findFirst()
				.ifPresent(photo -> response.setPrimaryPhotoUrl(photo.getPhotoUrl()));

		return response;

	}

	public List<ProfileResponse> convertProfiles(List<UserProfile> userProfiles) {
		return userProfiles.stream().map(this::mapToProfileResponse).collect(Collectors.toList());
	}

	public UserPreference getUserProfileByUserId(Long userId) {
		return userPreferenceRepository.findByUserId(userId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.USER_NOT_FOUND.toString(),
						ErrorEnum.USER_NOT_FOUND.getExceptionError(), HttpStatus.OK));
	}

	@Override
	public MatchResponse sendRequest(Long id, String matchId) {
		log.info("Request send by user: {}, matchId: {}", id, matchId);

		// Find match
		Match match = matchRepository.findByMatchId(matchId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.MATCH_NOT_FOUND.toString(),
						ErrorEnum.MATCH_NOT_FOUND.getExceptionError(), HttpStatus.OK));

		// Update match status
		match.setStatus(MatchStatus.SEND);
		Match savedMatch = matchRepository.saveAndFlush(match);

		log.info("Request updated -> userId: {}, matchId: {}, status: {}", savedMatch.getUser().getId(),
				savedMatch.getMatchId(), savedMatch.getStatus());

		// Save action
		MatchesAction action = new MatchesAction();
		action.setMatchId(matchId);
		action.setFromUser(savedMatch.getUser());
		action.setToUser(savedMatch.getMatchedUser());
		action.setStatus(MatchStatus.PENDING);
		matchesActionRepo.saveAndFlush(action);
		// notificationService.notifyNewMatch(match.getMatchedUser().getId(), userId);
		// Return response DTO
		return matchMapper.toResponse(savedMatch);
	}

	@Override
	public List<ProfileResponse> getSendRequestList(Long id) {
				log.error("Get Send Request List for user: {}", id);
		List<MatchesAction> mutualMatches = matchesActionRepo.findPendingRequestsByFromUser(id);
		List<ProfileResponse> mutualProfileRes = mutualMatches.stream()
		        .map(x -> mapToProfileResponse(x.getToUser().getProfile()))
		        .collect(Collectors.toList());

		log.error("get Send Request List -> " + mutualProfileRes);
		return mutualProfileRes;
	}

	@Override
	public List<ProfileResponse> getReceiveRequests(Long id) {
		log.error("get Receive Requests for user: {}", id);
		List<MatchesAction> mutualMatches = matchesActionRepo.findPendingRequestsByToUser(id);
		List<ProfileResponse> mutualProfileRes = mutualMatches.stream()
		        .map(x -> mapToProfileResponse(x.getFromUser().getProfile()))
		        .collect(Collectors.toList());

		log.error("get Receive Requests -> " + mutualProfileRes);
		return mutualProfileRes;
	}

}