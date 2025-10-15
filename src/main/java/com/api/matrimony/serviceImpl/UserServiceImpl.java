package com.api.matrimony.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.api.matrimony.entity.BlockedUser;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.Gender;
import com.api.matrimony.enums.MaritalStatus;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.BlockedUserRepository;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.repository.UserPreferenceRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.PreferenceRequest;
import com.api.matrimony.request.ProfileUpdateRequest;
import com.api.matrimony.response.PreferenceResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * User Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

	private final UserRepository userRepository;
	private final UserProfileRepository profileRepository;
	private final UserPreferenceRepository preferenceRepository;
	private final UserPhotoRepository photoRepository;
	private final BlockedUserRepository blockedUserRepository;

	@Value("${app.file.upload-dir}")
	private String uploadDir;

	@Value("${app.file.base-url}")
	private String baseUrl;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	@Override
	public UserResponse getUserById(Long userId) {
		log.info("Getting user by ID: {}", userId);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));
		return mapToUserResponse(user);
	}

	@Override
	public UserResponse getCurrentUser(Long userId) {
		log.info("Getting current user: {}", userId);
		return getUserById(userId);
	}

	@Override
	public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
		log.info("Updating profile for user: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

		// Get or create profile
		UserProfile profile = user.getProfile();
		if (profile == null) {
			profile = new UserProfile();
			profile.setUser(user);
		}

		// Update profile fields
		if (request.getFullName() != null) {
			profile.setFullName(request.getFullName());
		}
		if (request.getDateOfBirth() != null) {
			profile.setDateOfBirth(request.getDateOfBirth());
		}
		if (request.getGender() != null) {
			profile.setGender(Gender.valueOf(request.getGender().toUpperCase()));
		}
		if (request.getHeight() != null) {
			profile.setHeight(request.getHeight());
		}
		if (request.getWeight() != null) {
			profile.setWeight(request.getWeight());
		}
		if (request.getMaritalStatus() != null) {
			profile.setMaritalStatus(MaritalStatus.valueOf(request.getMaritalStatus().toUpperCase()));
		}
		if (request.getReligion() != null) {
			profile.setReligion(request.getReligion());
		}
		if (request.getCaste() != null) {
			profile.setCaste(request.getCaste());
		}
		if (request.getSubCaste() != null) {
			profile.setSubCaste(request.getSubCaste());
		}
		if (request.getMotherTongue() != null) {
			profile.setMotherTongue(request.getMotherTongue());
		}
		if (request.getEducation() != null) {
			profile.setEducation(request.getEducation());
		}
		if (request.getOccupation() != null) {
			profile.setOccupation(request.getOccupation());
		}
		if (request.getAnnualIncome() != null) {
			profile.setAnnualIncome(request.getAnnualIncome());
		}
		if (request.getAboutMe() != null) {
			profile.setAboutMe(request.getAboutMe());
		}
		if (request.getFamilyType() != null) {
			profile.setFamilyType(request.getFamilyType());
		}
		if (request.getFamilyValue() != null) {
			profile.setFamilyValue(request.getFamilyValue());
		}
		if (request.getCity() != null) {
			profile.setCity(request.getCity());
		}
		if (request.getState() != null) {
			profile.setState(request.getState());
		}
		if (request.getCountry() != null) {
			profile.setCountry(request.getCountry());
		}
		if (request.getPincode() != null) {
			profile.setPincode(request.getPincode());
		}
		if (request.getProfileCreatedBy() != null) {
			profile.setProfileCreatedBy(request.getProfileCreatedBy());
		}
		
        //  updated key name 
        if (request.getFamilyValue() != null) {
            profile.setFamilyValue(request.getFamilyValue());
        }
		 if (request.getSmokingHabits() != null) {
	            profile.setSmokingHabits(request.getSmokingHabits());
	        }
	        if (request.getDrinkingHabits() != null) {
	            profile.setDrinkingHabits(request.getDrinkingHabits());
	        }
	        if (request.getGothra() != null) {
	            profile.setGothra(request.getGothra());
	        }
	        if (request.getNickName() != null) {
	            profile.setNickName(request.getNickName());
	        }
	        if (request.getManglikStatus() != null) {
	            profile.setManglikStatus(request.getManglikStatus());
	        }
		
		String dietStr = request.getDiet();
		String normalizedDiet = normalizeDiet(dietStr);
		if (normalizedDiet != null) {
			profile.setDiet(normalizedDiet);
		}


		// Save profile
		profileRepository.save(profile);
		user.setProfile(profile);
		userRepository.save(user);

		log.info("Profile updated successfully for user: {}", userId);
		return mapToUserResponse(user);
	}

	@Override
	public UserResponse updatePreferences(Long userId, PreferenceRequest request) {
		log.info("Updating preferences for user: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

		// Get or create preferences
		UserPreference preferences = user.getPreferences();
		if (preferences == null) {
			preferences = new UserPreference();
			preferences.setUser(user);
		}

		log.error("edit prefrernce info -> "+request);
		// Update preference fields
		if (request.getMinAge() != null) {
			preferences.setMinAge(request.getMinAge());
		}
		if (request.getMaxAge() != null) {
			preferences.setMaxAge(request.getMaxAge());
		}
		if (request.getMinHeight() != null) {
			preferences.setMinHeight(request.getMinHeight());
		}
		if (request.getMaxHeight() != null) {
			preferences.setMaxHeight(request.getMaxHeight());
		}
		if (request.getMaritalStatuses() != null) {
			preferences.setMaritalStatuses(request.getMaritalStatuses());
		}
		if (request.getReligions() != null) {
			preferences.setReligions(request.getReligions());
		}
		if (request.getCastes() != null) {
			preferences.setCastes(request.getCastes());
		}
		if (request.getEducations() != null) {
			preferences.setEducation(request.getEducations());
		}
		if (request.getOccupations() != null) {
			preferences.setOccupation(request.getOccupations());
		}
		if (request.getMinIncome() != null) {
			preferences.setMinIncome(request.getMinIncome());
		}
		if (request.getMaxIncome() != null) {
			preferences.setMaxIncome(request.getMaxIncome());
		}
		if (request.getCities() != null) {
			preferences.setCities(request.getCities());
		}
		if (request.getStates() != null) {
			preferences.setStates(request.getStates());
		}
		if (request.getCountries() != null) {
			preferences.setCountries(request.getCountries());
		}
		
		String genderStr = request.getGender();
		if (genderStr != null && !genderStr.trim().isEmpty()) {
		    genderStr = genderStr.trim().toLowerCase();

		    switch (genderStr) {
		        case "male":
		            preferences.setGender("MALE");
		            break;
		        case "female":
		            preferences.setGender("FEMALE");
		            break;
		        case "other":
		        case "others":
		            preferences.setGender("OTHER");
		            break;
		        default:
		        	throw new ApplicationException(ErrorEnum.INVALID_GENDER.toString(), ErrorEnum.INVALID_GENDER.getExceptionError(),
							HttpStatus.OK);
		    }
		}
		preferences.setSubCastes(request.getSubCastes());
		preferences.setMotherTongue(request.getMotherTongue());
		preferences.setFamilyTypes(request.getFamilyTypes());
		
		String dietStr = request.getDiets();
		String normalizedDiet = normalizeDiet(dietStr);
		if (normalizedDiet != null) {
		    preferences.setDiet(normalizedDiet);
		}

		

		// Save preferences
		preferenceRepository.save(preferences);
		user.setPreferences(preferences);
		userRepository.save(user);

		log.info("Preferences updated successfully for user: {}", userId);
		return mapToUserResponse(user);
	}

	 @Override
	    public String uploadPhoto(Long userId, MultipartFile file, Boolean isPrimary) {
	        log.info("Uploading photo for user: {}, isPrimary: {}", userId, isPrimary);

	        validateFile(file);

	        User user = userRepository.findById(userId)
	                .orElseThrow(() -> new ApplicationException(
	                        ErrorEnum.INVALID_USER.toString(),
	                        ErrorEnum.INVALID_USER.getExceptionError(),
	                        HttpStatus.BAD_REQUEST
	                ));

	        try {
	            // Ensure upload directory exists
	            Path uploadPath = Paths.get(uploadDir);
	            Files.createDirectories(uploadPath);

	            // Generate unique file name
	            String filename = generateUniqueFilename(file.getOriginalFilename());
	            Path filePath = uploadPath.resolve(filename);

	            // Copy file safely (replace if already exists)
	            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	            // Handle primary photo update
	            if (Boolean.TRUE.equals(isPrimary)) {
	                resetPrimaryPhotos(userId);
	            }

	            // Save photo record
	            UserPhoto userPhoto = new UserPhoto();
	            userPhoto.setUser(user);
	            userPhoto.setPhotoUrl(baseUrl + filename);
	            userPhoto.setIsPrimary(isPrimary != null && isPrimary);
	            userPhoto.setDisplayOrder(getNextDisplayOrder(userId));

	            photoRepository.save(userPhoto);

	            log.info("Photo uploaded successfully for user: {}", userId);
	            return userPhoto.getPhotoUrl();

	        } catch (IOException e) {
	            log.error("Error uploading photo for user: {}", userId, e);
	            throw new ApplicationException(
	                    ErrorEnum.FAILED_TO_UPLOAD_PHOTO.toString(),
	                    ErrorEnum.FAILED_TO_UPLOAD_PHOTO.getExceptionError(),
	                    HttpStatus.INTERNAL_SERVER_ERROR
	            );
	        }
	    }

	    @Override
	    public List<String> getUserPhotos(Long userId) {
	        log.info("Fetching photos for user: {}", userId);
	        return photoRepository.findByUserIdOrderByDisplayOrderAsc(userId)
	                .stream()
	                .map(UserPhoto::getPhotoUrl)
	                .collect(Collectors.toList());
	    }

	    @Override
	    public void deletePhoto(Long userId, Long photoId) {
	        log.info("Deleting photo: {} for user: {}", photoId, userId);

	        UserPhoto photo = photoRepository.findById(photoId)
	                .orElseThrow(() -> new ApplicationException(
	                        ErrorEnum.PHOTO_CAN_NOT_FOUND.toString(),
	                        ErrorEnum.PHOTO_CAN_NOT_FOUND.getExceptionError(),
	                        HttpStatus.NOT_FOUND
	                ));

	        if (!photo.getUser().getId().equals(userId)) {
	            throw new ApplicationException(
	                    ErrorEnum.ONLY_OWN_PHOTO_CAN_DELETE.toString(),
	                    ErrorEnum.ONLY_OWN_PHOTO_CAN_DELETE.getExceptionError(),
	                    HttpStatus.FORBIDDEN
	            );
	        }

	        // Delete file from storage
	        try {
	            String filename = extractFilename(photo.getPhotoUrl());
	            Path filePath = Paths.get(uploadDir, filename);
	            Files.deleteIfExists(filePath);
	        } catch (IOException e) {
	            log.warn("Failed to delete photo file from storage: {}", e.getMessage());
	        }

	        photoRepository.delete(photo);
	        log.info("Photo deleted successfully: {}", photoId);
	    }

	    // ------------------------- //
	    // 	Photo Helper Methods     //
	    // ------------------------- //

	    private void validateFile(MultipartFile file) {
	        if (file.isEmpty()) {
	            throw new ApplicationException(
	                    ErrorEnum.EMPTY_FILE.toString(),
	                    ErrorEnum.EMPTY_FILE.getExceptionError(),
	                    HttpStatus.BAD_REQUEST
	            );
	        }

	        String contentType = file.getContentType();
	        if (contentType == null || !contentType.startsWith("image/")) {
	            throw new ApplicationException(
	                    ErrorEnum.IMAGE_ALLOW.toString(),
	                    ErrorEnum.IMAGE_ALLOW.getExceptionError(),
	                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
	            );
	        }

	        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
	            throw new ApplicationException(
	                    ErrorEnum.FILE_SIZE_IS_MORE_THAN_REQ.toString(),
	                    ErrorEnum.FILE_SIZE_IS_MORE_THAN_REQ.getExceptionError(),
	                    HttpStatus.PAYLOAD_TOO_LARGE
	            );
	        }
	    }

	    private String generateUniqueFilename(String originalFilename) {
	        String extension = "";
	        if (originalFilename != null && originalFilename.contains(".")) {
	            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
	        }
	        return UUID.randomUUID().toString() + extension;
	    }

	    private void resetPrimaryPhotos(Long userId) {
	        List<UserPhoto> existingPhotos = photoRepository.findByUserIdOrderByDisplayOrderAsc(userId);
	        existingPhotos.forEach(photo -> photo.setIsPrimary(false));
	        photoRepository.saveAll(existingPhotos);
	    }

	    private String extractFilename(String photoUrl) {
	        return photoUrl.substring(photoUrl.lastIndexOf("/") + 1);
	    }

//	    private Integer getNextDisplayOrder(Long userId) {
//	        return photoRepository.findByUserIdOrderByDisplayOrderAsc(userId).size() + 1;
//	    }

	@Override
	public void blockUser(Long blockerId, Long blockedUserId) {
		log.info("User {} blocking user {}", blockerId, blockedUserId);

		if (blockerId.equals(blockedUserId)) {
			throw new ApplicationException(ErrorEnum.YOU_CAN_NOT_BLOCK_URSELF.toString(),
					ErrorEnum.YOU_CAN_NOT_BLOCK_URSELF.getExceptionError(), HttpStatus.OK);
		}

		if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(blockerId, blockedUserId)) {
			throw new ApplicationException(ErrorEnum.USER_ALREADY_BLOCK.toString(),
					ErrorEnum.USER_ALREADY_BLOCK.getExceptionError(), HttpStatus.OK);
		}

		User blocker = userRepository.findById(blockerId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.BLOCK_USER_NOT_FOUND.toString(),
						ErrorEnum.BLOCK_USER_NOT_FOUND.getExceptionError(), HttpStatus.OK));
		User blockedUser = userRepository.findById(blockedUserId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.USER_TO_BLOCK_NOT_FOUND.toString(),
						ErrorEnum.USER_TO_BLOCK_NOT_FOUND.getExceptionError(), HttpStatus.OK));

		BlockedUser blockedUserEntity = new BlockedUser();
		blockedUserEntity.setBlocker(blocker);
		blockedUserEntity.setBlockedUser(blockedUser);

		blockedUserRepository.save(blockedUserEntity);
		log.info("User {} successfully blocked user {}", blockerId, blockedUserId);
	}

	@Override
	public void unblockUser(Long blockerId, Long blockedUserId) {
		log.info("User {} unblocking user {}", blockerId, blockedUserId);

		if (!blockedUserRepository.existsByBlockerIdAndBlockedUserId(blockerId, blockedUserId)) {
			throw new ApplicationException(ErrorEnum.USER_IS_NOT_BLOCK.toString(),
					ErrorEnum.USER_IS_NOT_BLOCK.getExceptionError(), HttpStatus.OK);
		}

		blockedUserRepository.deleteByBlockerIdAndBlockedUserId(blockerId, blockedUserId);
		log.info("User {} successfully unblocked user {}", blockerId, blockedUserId);
	}

	@Override
	public List<UserResponse> getBlockedUsers(Long userId) {
		log.info("Getting blocked users for user: {}", userId);

		List<BlockedUser> blockedUsers = blockedUserRepository.findByBlockerIdOrderByBlockedAtDesc(userId);
		return blockedUsers.stream().map(blockedUser -> mapToUserResponse(blockedUser.getBlockedUser()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isUserBlocked(Long blockerId, Long blockedUserId) {
		return blockedUserRepository.existsByBlockerIdAndBlockedUserId(blockerId, blockedUserId);
	}

	@Override
	public void deactivateUser(Long userId) {
		log.info("Deactivating user: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

		user.setIsActive(false);
		userRepository.save(user);

		log.info("User deactivated successfully: {}", userId);
	}

	@Override
	public void activateUser(Long userId) {
		log.info("Activating user: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

		user.setIsActive(true);
		userRepository.save(user);

		log.info("User activated successfully: {}", userId);
	}

	@Override
	public boolean isUserActive(Long userId) {
		return userRepository.findById(userId).map(User::getIsActive).orElse(false);
	}

	// Helper methods
	private UserResponse mapToUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setEmail(user.getEmail());
		response.setPhone(user.getPhone());
		// response.setUserType(user.getUserType().name());
		response.setIsVerified(user.getIsVerified());
		response.setIsActive(user.getIsActive());
		response.setCreatedAt(user.getCreatedAt());
		response.setLastLogin(user.getLastLogin());

		// Map profile if exists
		if (user.getProfile() != null) {
			response.setProfile(mapToProfileResponse(user.getProfile()));
		}
		
		if(user.getPreferences() != null) {
			response.setPreference(mapToPreferenceResponse(user.getPreferences()));
		}
		return response;
	}

	private ProfileResponse mapToProfileResponse(UserProfile profile) {
		ProfileResponse response = new ProfileResponse();
		response.setUserId(profile.getUser().getId());
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
		response.setAnnualIncome(profile.getAnnualIncome() != null ? profile.getAnnualIncome().name() : null);
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
		response.setHiseStatus(profile.getIsHide());
		
		response.setManglikStatus(profile.getManglikStatus() != null ? profile.getManglikStatus().name() : null); // new field
		response.setSmokingHabits(profile.getSmokingHabits() != null ? profile.getSmokingHabits().name() : null); //  new field
		response.setDrinkingHabits(profile.getDrinkingHabits() != null ? profile.getDrinkingHabits().name() : null); //  new field
		response.setNickName(profile.getNickName()); //  new field
		response.setGothra(profile.getGothra()); //  new field

		// Get photos
		List<UserPhoto> photos = photoRepository.findByUserIdOrderByDisplayOrderAsc(profile.getUser().getId());
		response.setPhotoUrls(photos.stream().map(UserPhoto::getPhotoUrl).collect(Collectors.toList()));

		// Get primary photo
		photos.stream().filter(UserPhoto::getIsPrimary).findFirst()
				.ifPresent(photo -> response.setPrimaryPhotoUrl(photo.getPhotoUrl()));

		return response;
	}

	private Integer getNextDisplayOrder(Long userId) {
		List<UserPhoto> photos = photoRepository.findByUserIdOrderByDisplayOrderAsc(userId);
		return photos.isEmpty() ? 1 : photos.get(photos.size() - 1).getDisplayOrder() + 1;
	}
	
	private PreferenceResponse mapToPreferenceResponse(UserPreference preference) {
	    if (preference == null) {
	        return null;
	    }

	    PreferenceResponse response = new PreferenceResponse();

	    response.setMinAge(preference.getMinAge());
	    response.setMaxAge(preference.getMaxAge());

	    response.setMinHeight(preference.getMinHeight());
	    response.setMaxHeight(preference.getMaxHeight());

	    response.setMaritalStatuses(preference.getMaritalStatuses());
	    response.setReligions(preference.getReligions());
	    response.setCastes(preference.getCastes());
	    response.setEducations(preference.getEducation());
	    response.setOccupations(preference.getOccupation());

	    response.setMinIncome(preference.getMinIncome());
	    response.setMaxIncome(preference.getMaxIncome());

	    response.setCities(preference.getCities());
	    response.setStates(preference.getStates());
	    response.setCountries(preference.getCountries());

	    // ✅ Newly added fields
	    response.setGender(preference.getGender());
	    response.setSubCastes(preference.getSubCastes());
	    response.setMotherTongue(preference.getMotherTongue());
	    response.setFamilyTypes(preference.getFamilyTypes());
	    response.setDiet(preference.getDiet());

	    return response;
	}


	public static String normalizeDiet(String dietStr) {
	    if (dietStr == null || dietStr.trim().isEmpty()) {
	        return null;
	    }

	    switch (dietStr.trim().toLowerCase()) {
	        case "vegetarian":
	        case "veg":
	            return "VEGETARIAN";

	        case "non-vegetarian":
	        case "nonvegetarian":
	        case "non_vegetarian":
	        case "non-veg":
	        case "nonveg":
	            return "NON_VEGETARIAN";

	        case "eggetarian":
	            return "EGGETARIAN";

	        case "vegan":
	            return "VEGAN";

	        case "not req":
	        case "not required":
	        case "notreq":
	            return "NOT_REQ"; // better to keep consistent naming (all caps + underscore)
	            
	        default:
	            throw new ApplicationException(
	                ErrorEnum.INVALID_DIET.toString(),
	                ErrorEnum.INVALID_DIET.getExceptionError(),
	                HttpStatus.OK
	            );
	    }
	}

	
}
