package com.api.matrimony.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.api.matrimony.request.PreferenceRequest;
import com.api.matrimony.request.ProfileUpdateRequest;
import com.api.matrimony.response.UserResponse;

/**
 * User Service Interface
 */

public interface UserService {
    UserResponse getUserById(Long userId);
    UserResponse getCurrentUser(Long userId);
    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);
    UserResponse updatePreferences(Long userId, PreferenceRequest request);
    String uploadPhoto(Long userId, MultipartFile file, Boolean isPrimary);
    List<String> getUserPhotos(Long userId);
    void deletePhoto(Long userId, Long photoId);
    void blockUser(Long blockerId, Long blockedUserId);
    void unblockUser(Long blockerId, Long blockedUserId);
    List<UserResponse> getBlockedUsers(Long userId);
    boolean isUserBlocked(Long blockerId, Long blockedUserId);
    void deactivateUser(Long userId);
    void activateUser(Long userId);
    boolean isUserActive(Long userId);
}
