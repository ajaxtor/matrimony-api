package com.api.matrimony.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.api.matrimony.response.APIResonse;
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
    public ResponseEntity<APIResonse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting current user profile: {}", currentUser.getId());
        APIResonse<UserResponse> response = new APIResonse<>();
       
            UserResponse userResponse = userService.getCurrentUser(currentUser.getId());
            response.setData(userResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user profile by ID
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<APIResonse<UserResponse>> getUserById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("Getting user profile: {} by user: {}", userId, currentUser.getId());
        APIResonse<UserResponse> response = new APIResonse<>();
       
            UserResponse userResponse = userService.getUserById(userId);
            response.setData(userResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update user profile
     */
    @PutMapping("/editProfile")
    public ResponseEntity<APIResonse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ProfileUpdateRequest request) {
        
        log.info("Updating profile for user: {}", currentUser.getId());
        APIResonse<UserResponse> response = new APIResonse<>();
       
            UserResponse userResponse = userService.updateProfile(currentUser.getId(), request);
            response.setData(userResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update user preferences
     */
    @PutMapping("/editPreferences")
    public ResponseEntity<APIResonse<UserResponse>> updatePreferences(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PreferenceRequest request) {
        
        log.info("Updating preferences for user: {}", currentUser.getId());
        
        APIResonse<UserResponse> response = new APIResonse<>();
            UserResponse userResponse = userService.updatePreferences(currentUser.getId(), request);
            response.setData(userResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user preferences
     */
    @GetMapping("/getPreferences")
    public ResponseEntity<APIResonse<UserResponse>> getPreferences(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting preferences for user: {}", currentUser.getId());
        
        APIResonse<UserResponse> response = new APIResonse<>();
            UserResponse userResponse = userService.getCurrentUser(currentUser.getId());
            response.setData(userResponse);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Upload user photo
     */
    @PostMapping("/photos")
    public ResponseEntity<APIResonse<String>> uploadPhoto(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("photo") MultipartFile file,
            @RequestParam(defaultValue = "false") Boolean isPrimary) {
        
        log.info("Uploading photo for user: {}, isPrimary: {}", currentUser.getId(), isPrimary);
        
        APIResonse<String> response = new APIResonse<>();
            String photoUrl = userService.uploadPhoto(currentUser.getId(), file, isPrimary);
            response.setData(photoUrl);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user photos
     */
    @GetMapping("/photos")
    public ResponseEntity<APIResonse<List<String>>> getUserPhotos(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting photos for user: {}", currentUser.getId());
        
        APIResonse<List<String>> response = new APIResonse<>();
            List<String> photos = userService.getUserPhotos(currentUser.getId());
            response.setData(photos);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Delete user photo
     */
    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<APIResonse<String>> deletePhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long photoId) {
        
        log.info("Deleting photo: {} for user: {}", photoId, currentUser.getId());
        APIResonse<String> response = new APIResonse<>();
       
            userService.deletePhoto(currentUser.getId(), photoId);
            response.setData("User Deleted");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Block user
     */
    @PostMapping("/{userId}/block")
    public ResponseEntity<APIResonse<String>> blockUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("User {} blocking user {}", currentUser.getId(), userId);
        APIResonse<String> response = new APIResonse<>();
       
            userService.blockUser(currentUser.getId(), userId);
            response.setData("User is Blocked");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Unblock user
     */
    @DeleteMapping("/{userId}/unblock")
    public ResponseEntity<APIResonse<String>> unblockUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("User {} unblocking user {}", currentUser.getId(), userId);
        APIResonse<String> response = new APIResonse<>();
       
            userService.unblockUser(currentUser.getId(), userId);
            response.setData("User Has been unblock");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get blocked users
     */
    @GetMapping("/blocked")
    public ResponseEntity<APIResonse<List<UserResponse>>> getBlockedUsers(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting blocked users for user: {}", currentUser.getId());
        
        APIResonse<List<UserResponse>> response = new APIResonse<>();
            List<UserResponse> blockedUsers = userService.getBlockedUsers(currentUser.getId());
            response.setData(blockedUsers);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    
}
