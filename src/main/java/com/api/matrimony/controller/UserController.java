package com.api.matrimony.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.api.matrimony.entity.User;
import com.api.matrimony.request.PreferenceRequest;
import com.api.matrimony.request.ProfileUpdateRequest;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * User Controller for user management operations
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting current user profile: {}", currentUser.getId());
        
        try {
            UserResponse userResponse = userService.getCurrentUser(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Profile retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting current user profile: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user profile by ID
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("Getting user profile: {} by user: {}", userId, currentUser.getId());
        
        try {
            UserResponse userResponse = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Profile retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting user profile: {} by user: {}", userId, currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProfileUpdateRequest request) {
        
        log.info("Updating profile for user: {}", currentUser.getId());
        
        try {
            UserResponse userResponse = userService.updateProfile(currentUser.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Profile updated successfully"));
        } catch (Exception e) {
            log.error("Error updating profile for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update user preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserResponse>> updatePreferences(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PreferenceRequest request) {
        
        log.info("Updating preferences for user: {}", currentUser.getId());
        
        try {
            UserResponse userResponse = userService.updatePreferences(currentUser.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Preferences updated successfully"));
        } catch (Exception e) {
            log.error("Error updating preferences for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserResponse>> getPreferences(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting preferences for user: {}", currentUser.getId());
        
        try {
            UserResponse userResponse = userService.getCurrentUser(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(userResponse, "Preferences retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting preferences for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Upload user photo
     */
    @PostMapping("/photos")
    public ResponseEntity<ApiResponse<String>> uploadPhoto(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("photo") MultipartFile file,
            @RequestParam(defaultValue = "false") Boolean isPrimary) {
        
        log.info("Uploading photo for user: {}, isPrimary: {}", currentUser.getId(), isPrimary);
        
        try {
            String photoUrl = userService.uploadPhoto(currentUser.getId(), file, isPrimary);
            return ResponseEntity.ok(ApiResponse.success(photoUrl, "Photo uploaded successfully"));
        } catch (Exception e) {
            log.error("Error uploading photo for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user photos
     */
    @GetMapping("/photos")
    public ResponseEntity<ApiResponse<List<String>>> getUserPhotos(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting photos for user: {}", currentUser.getId());
        
        try {
            List<String> photos = userService.getUserPhotos(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(photos, "Photos retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting photos for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete user photo
     */
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<ApiResponse<String>> deletePhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long photoId) {
        
        log.info("Deleting photo: {} for user: {}", photoId, currentUser.getId());
        
        try {
            userService.deletePhoto(currentUser.getId(), photoId);
            return ResponseEntity.ok(ApiResponse.success("Success", "Photo deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting photo: {} for user: {}", photoId, currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Block user
     */
    @PostMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<String>> blockUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("User {} blocking user {}", currentUser.getId(), userId);
        
        try {
            userService.blockUser(currentUser.getId(), userId);
            return ResponseEntity.ok(ApiResponse.success("Success", "User blocked successfully"));
        } catch (Exception e) {
            log.error("Error blocking user: {} by user: {}", userId, currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Unblock user
     */
    @DeleteMapping("/{userId}/unblock")
    public ResponseEntity<ApiResponse<String>> unblockUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("User {} unblocking user {}", currentUser.getId(), userId);
        
        try {
            userService.unblockUser(currentUser.getId(), userId);
            return ResponseEntity.ok(ApiResponse.success("Success", "User unblocked successfully"));
        } catch (Exception e) {
            log.error("Error unblocking user: {} by user: {}", userId, currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get blocked users
     */
    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getBlockedUsers(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting blocked users for user: {}", currentUser.getId());
        
        try {
            List<UserResponse> blockedUsers = userService.getBlockedUsers(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(blockedUsers, "Blocked users retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting blocked users for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

