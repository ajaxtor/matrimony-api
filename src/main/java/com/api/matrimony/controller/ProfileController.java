package com.api.matrimony.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.entity.User;
import com.api.matrimony.request.ProfileUpdateRequest;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
* Profile Controller for profile-specific operations
*/
@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProfileController {

 private final ProfileService profileService;

 /**
  * Get profile by user ID
  */
 @GetMapping("/{userId}")
 public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
         @AuthenticationPrincipal User currentUser,
         @PathVariable Long userId) {
     
     log.info("Getting profile: {} by user: {}", userId, currentUser.getId());
     
     try {
         ProfileResponse profile = profileService.getPublicProfile(userId, currentUser.getId());
         return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
     } catch (Exception e) {
         log.error("Error getting profile: {} by user: {}", userId, currentUser.getId(), e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }

 /**
  * Update profile
  */
 @PutMapping
 public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
         @AuthenticationPrincipal User currentUser,
         @Valid @RequestBody ProfileUpdateRequest request) {
     
     log.info("Updating profile for user: {}", currentUser.getId());
     
     try {
         ProfileResponse profile = profileService.updateProfile(currentUser.getId(), request);
         return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
     } catch (Exception e) {
         log.error("Error updating profile for user: {}", currentUser.getId(), e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }

 /**
  * Get recent profiles
  */
 @GetMapping("/recent")
 public ResponseEntity<ApiResponse<List<ProfileResponse>>> getRecentProfiles(
         @RequestParam(defaultValue = "10") int limit) {
     
     log.info("Getting recent profiles, limit: {}", limit);
     
     try {
         List<ProfileResponse> profiles = profileService.getRecentProfiles(limit);
         return ResponseEntity.ok(ApiResponse.success(profiles, "Recent profiles retrieved successfully"));
     } catch (Exception e) {
         log.error("Error getting recent profiles", e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }

 /**
  * Get featured profiles
  */
 @GetMapping("/featured")
 public ResponseEntity<ApiResponse<List<ProfileResponse>>> getFeaturedProfiles(
         @RequestParam(defaultValue = "10") int limit) {
     
     log.info("Getting featured profiles, limit: {}", limit);
     
     try {
         List<ProfileResponse> profiles = profileService.getFeaturedProfiles(limit);
         return ResponseEntity.ok(ApiResponse.success(profiles, "Featured profiles retrieved successfully"));
     } catch (Exception e) {
         log.error("Error getting featured profiles", e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }

 /**
  * Get similar profiles
  */
 @GetMapping("/similar")
 public ResponseEntity<ApiResponse<List<ProfileResponse>>> getSimilarProfiles(
         @AuthenticationPrincipal User currentUser,
         @RequestParam(defaultValue = "10") int limit) {
     
     log.info("Getting similar profiles for user: {}, limit: {}", currentUser.getId(), limit);
     
     try {
         List<ProfileResponse> profiles = profileService.getSimilarProfiles(currentUser.getId(), limit);
         return ResponseEntity.ok(ApiResponse.success(profiles, "Similar profiles retrieved successfully"));
     } catch (Exception e) {
         log.error("Error getting similar profiles for user: {}", currentUser.getId(), e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }

 /**
  * Check if profile is complete
  */
 @GetMapping("/completeness")
 public ResponseEntity<ApiResponse<Boolean>> checkProfileCompleteness(
         @AuthenticationPrincipal User currentUser) {
     
     log.info("Checking profile completeness for user: {}", currentUser.getId());
     
     try {
         boolean isComplete = profileService.isProfileComplete(currentUser.getId());
         return ResponseEntity.ok(ApiResponse.success(isComplete, "Profile completeness checked"));
     } catch (Exception e) {
         log.error("Error checking profile completeness for user: {}", currentUser.getId(), e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }

 /**
  * Get profile view count
  */
 @GetMapping("/views")
 public ResponseEntity<ApiResponse<Long>> getProfileViewCount(
         @AuthenticationPrincipal User currentUser) {
     
     log.info("Getting profile view count for user: {}", currentUser.getId());
     
     try {
         Long viewCount = profileService.getProfileViewCount(currentUser.getId());
         return ResponseEntity.ok(ApiResponse.success(viewCount, "Profile view count retrieved"));
     } catch (Exception e) {
         log.error("Error getting profile view count for user: {}", currentUser.getId(), e);
         return ResponseEntity.badRequest()
                 .body(ApiResponse.error(e.getMessage()));
     }
 }
}

