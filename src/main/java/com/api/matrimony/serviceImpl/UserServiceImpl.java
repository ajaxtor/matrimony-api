package com.api.matrimony.serviceImpl;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
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
import com.api.matrimony.exception.CustomException;
import com.api.matrimony.exception.ResourceNotFoundException;
import com.api.matrimony.repository.BlockedUserRepository;
import com.api.matrimony.repository.UserPhotoRepository;
import com.api.matrimony.repository.UserPreferenceRepository;
import com.api.matrimony.repository.UserProfileRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.PreferenceRequest;
import com.api.matrimony.request.ProfileUpdateRequest;
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get or create profile
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }

        // Update profile fields
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get or create preferences
        UserPreference preferences = user.getPreferences();
        if (preferences == null) {
            preferences = new UserPreference();
            preferences.setUser(user);
        }

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
        if (request.getMaritalStatus() != null) {
            preferences.setMaritalStatus(request.getMaritalStatus());
        }
        if (request.getReligion() != null) {
            preferences.setReligion(request.getReligion());
        }
        if (request.getCaste() != null) {
            preferences.setCaste(request.getCaste());
        }
        if (request.getEducation() != null) {
            preferences.setEducation(request.getEducation());
        }
        if (request.getOccupation() != null) {
            preferences.setOccupation(request.getOccupation());
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
        
        if (file.isEmpty()) {
            throw new CustomException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException("Only image files are allowed");
        }

        // Validate file size (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new CustomException("File size cannot exceed 10MB");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // If this is primary photo, remove primary flag from other photos
            if (isPrimary != null && isPrimary) {
                List<UserPhoto> existingPhotos = photoRepository.findByUserIdOrderByDisplayOrderAsc(userId);
                existingPhotos.forEach(photo -> photo.setIsPrimary(false));
                photoRepository.saveAll(existingPhotos);
            }

            // Save photo record
            UserPhoto userPhoto = new UserPhoto();
            userPhoto.setUser(user);
            userPhoto.setPhotoUrl(baseUrl + filename);
            userPhoto.setIsPrimary(isPrimary != null ? isPrimary : false);
            userPhoto.setDisplayOrder(getNextDisplayOrder(userId));
            
            photoRepository.save(userPhoto);

            log.info("Photo uploaded successfully for user: {}", userId);
            return userPhoto.getPhotoUrl();

        } catch (IOException e) {
            log.error("Error uploading photo for user: {}", userId, e);
            throw new CustomException("Failed to upload photo: " + e.getMessage());
        }
    }

    @Override
    public List<String> getUserPhotos(Long userId) {
        log.info("Getting photos for user: {}", userId);
        return photoRepository.findByUserIdOrderByDisplayOrderAsc(userId)
                .stream()
                .map(UserPhoto::getPhotoUrl)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhoto(Long userId, Long photoId) {
        log.info("Deleting photo: {} for user: {}", photoId, userId);
        
        UserPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));

        if (!photo.getUser().getId().equals(userId)) {
            throw new CustomException("You can only delete your own photos");
        }

        // Delete file from storage
        try {
            String filename = photo.getPhotoUrl().substring(photo.getPhotoUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete photo file: {}", e.getMessage());
        }

        photoRepository.delete(photo);
        log.info("Photo deleted successfully: {}", photoId);
    }

    @Override
    public void blockUser(Long blockerId, Long blockedUserId) {
        log.info("User {} blocking user {}", blockerId, blockedUserId);
        
        if (blockerId.equals(blockedUserId)) {
            throw new CustomException("You cannot block yourself");
        }

        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(blockerId, blockedUserId)) {
            throw new CustomException("User is already blocked");
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker user not found"));
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User to block not found"));

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
            throw new CustomException("User is not blocked");
        }

        blockedUserRepository.deleteByBlockerIdAndBlockedUserId(blockerId, blockedUserId);
        log.info("User {} successfully unblocked user {}", blockerId, blockedUserId);
    }

    @Override
    public List<UserResponse> getBlockedUsers(Long userId) {
        log.info("Getting blocked users for user: {}", userId);
        
        List<BlockedUser> blockedUsers = blockedUserRepository.findByBlockerIdOrderByBlockedAtDesc(userId);
        return blockedUsers.stream()
                .map(blockedUser -> mapToUserResponse(blockedUser.getBlockedUser()))
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
    }

    @Override
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setIsActive(true);
        userRepository.save(user);
        
        log.info("User activated successfully: {}", userId);
    }

    @Override
    public boolean isUserActive(Long userId) {
        return userRepository.findById(userId)
                .map(User::getIsActive)
                .orElse(false);
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

        return response;
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

    private Integer getNextDisplayOrder(Long userId) {
        List<UserPhoto> photos = photoRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return photos.isEmpty() ? 1 : photos.get(photos.size() - 1).getDisplayOrder() + 1;
    }
}
