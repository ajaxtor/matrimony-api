package com.api.matrimony.serviceImpl;


import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.Gender;
import com.api.matrimony.enums.MaritalStatus;
import com.api.matrimony.exception.ResourceNotFoundException;
import com.api.matrimony.repository.BlockedUserRepository;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.ProfileUpdateRequest;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Profile Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserPhotoRepository photoRepository;
    private final BlockedUserRepository blockedUserRepository;

    @Override
    public ProfileResponse getProfileByUserId(Long userId) {
        log.info("Getting profile for user: {}", userId);
        
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
        
        return mapToProfileResponse(profile);
    }

    @Override
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        // Update profile fields (similar to UserServiceImpl)
        updateProfileFields(profile, request);
        
        profileRepository.save(profile);
        log.info("Profile updated successfully for user: {}", userId);
        
        return mapToProfileResponse(profile);
    }

    @Override
    public ProfileResponse getPublicProfile(Long userId, Long viewerId) {
        log.info("Getting public profile for user: {} viewed by: {}", userId, viewerId);
        
        // Check if viewer is blocked by the profile owner
        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(userId, viewerId)) {
            throw new ResourceNotFoundException("Profile not accessible");
        }

        // Check if profile owner is blocked by viewer
        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(viewerId, userId)) {
            throw new ResourceNotFoundException("Profile not accessible");
        }

        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // Increment profile view count
        incrementProfileView(userId, viewerId);

        ProfileResponse response = mapToProfileResponse(profile);
        
        // Remove sensitive information for public view
      //  response.setEmail(null);
       // response.setPhone(null);
        
        return response;
    }

    @Override
    public List<ProfileResponse> getRecentProfiles(int limit) {
        log.info("Getting recent profiles, limit: {}", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        return profileRepository.findActiveProfiles(pageable)
                .getContent()
                .stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileResponse> getFeaturedProfiles(int limit) {
        log.info("Getting featured profiles, limit: {}", limit);
        
        // For now, return recent profiles
        // In future, implement logic for featured profiles based on subscription, activity, etc.
        return getRecentProfiles(limit);
    }

    @Override
    public void incrementProfileView(Long profileId, Long viewerId) {
        log.debug("Incrementing profile view for profile: {} by viewer: {}", profileId, viewerId);
        
        // Don't count self-views
        if (profileId.equals(viewerId)) {
            return;
        }

        // Implementation for profile view tracking can be added here
        // For now, we'll just log it
        log.debug("Profile view recorded: profile={}, viewer={}", profileId, viewerId);
    }

    @Override
    public Long getProfileViewCount(Long profileId) {
        log.info("Getting profile view count for profile: {}", profileId);
        
        // Implementation for getting view count
        // For now, return 0 as placeholder
        return 0L;
    }

    @Override
    public boolean isProfileComplete(Long userId) {
        log.info("Checking if profile is complete for user: {}", userId);
        
        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);
        
        if (profile == null) {
            return false;
        }

        // Check required fields
        boolean hasBasicInfo = profile.getFirstName() != null && 
                              profile.getLastName() != null &&
                              profile.getDateOfBirth() != null &&
                              profile.getGender() != null;

        boolean hasLocationInfo = profile.getCity() != null && 
                                 profile.getState() != null;

        boolean hasPersonalInfo = profile.getReligion() != null &&
                                 profile.getEducation() != null &&
                                 profile.getOccupation() != null;

        // Check if user has at least one photo
        boolean hasPhotos = !photoRepository.findByUserIdOrderByDisplayOrderAsc(userId).isEmpty();

        return hasBasicInfo && hasLocationInfo && hasPersonalInfo && hasPhotos;
    }

    @Override
    public List<ProfileResponse> getSimilarProfiles(Long userId, int limit) {
        log.info("Getting similar profiles for user: {}, limit: {}", userId, limit);
        
        UserProfile userProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // Find profiles with similar criteria
        Pageable pageable = PageRequest.of(0, limit);
        
        return profileRepository.findProfilesWithFilters(
                userProfile.getCity(),
                userProfile.getState(), 
                userProfile.getReligion(),
                null, // minAge
                null, // maxAge
                pageable)
                .getContent()
                .stream()
                .filter(profile -> !profile.getUser().getId().equals(userId)) // Exclude self
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void updateProfileFields(UserProfile profile, ProfileUpdateRequest request) {
        if (request.getFirstName() != null) {
            profile.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            profile.setLastName(request.getLastName());
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
        if (request.getFamilyValues() != null) {
            profile.setFamilyValues(request.getFamilyValues());
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
    }

    private ProfileResponse mapToProfileResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setFullName(profile.getFirstName() + " " + profile.getLastName());
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
