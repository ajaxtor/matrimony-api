package com.api.matrimony.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.api.matrimony.response.APIResonse;
import com.api.matrimony.response.APIResonse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
* Profile Controller for profile-specific operations
*/
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProfileController {

 private final ProfileService profileService;

 /**
  * Get profile by user ID
  */
 @GetMapping("/{userId}")
 public ResponseEntity<APIResonse<ProfileResponse>> getProfile(
         @AuthenticationPrincipal User currentUser,
         @PathVariable Long userId) {
     
     log.info("Getting profile: {} by user: {}", userId, currentUser.getId());
     
     APIResonse<ProfileResponse> response = new APIResonse<>();
         ProfileResponse profile = profileService.getPublicProfile(userId, currentUser.getId());
         response.setData(profile);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }

 /**
  * Update profile
  */
 @PutMapping
 public ResponseEntity<APIResonse<ProfileResponse>> updateProfile(
         @AuthenticationPrincipal User currentUser,
         @Valid @RequestBody ProfileUpdateRequest request) {
     
     log.info("Updating profile for user: {}", currentUser.getId());
     
     APIResonse<ProfileResponse> response = new APIResonse<>();
         ProfileResponse profile = profileService.updateProfile(currentUser.getId(), request);
         response.setData(profile);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }

 /**
  * Get recent profiles
  */
 @GetMapping("/recent")
 public ResponseEntity<APIResonse<List<ProfileResponse>>> getRecentProfiles(
         @RequestParam(defaultValue = "10") int limit) {
     
     log.info("Getting recent profiles, limit: {}", limit);
     APIResonse<List<ProfileResponse>> response = new APIResonse<>();
         List<ProfileResponse> profiles = profileService.getRecentProfiles(limit);
         response.setData(profiles);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }

 /**
  * Get featured profiles
  */
 @GetMapping("/featuredProfiles")
 public ResponseEntity<APIResonse<List<ProfileResponse>>> getFeaturedProfiles(
         @RequestParam(defaultValue = "10") int limit) {
     
     log.info("Getting featured profiles, limit: {}", limit);
     APIResonse<List<ProfileResponse>> response = new APIResonse<>();
         List<ProfileResponse> profiles = profileService.getFeaturedProfiles(limit);
         response.setData(profiles);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }

 /**
  * Get similar profiles
  */
 @GetMapping("/similarProfiles")
 public ResponseEntity<APIResonse<List<ProfileResponse>>> getSimilarProfiles(
         @AuthenticationPrincipal User currentUser,
         @RequestParam(defaultValue = "10") int limit) {
     
     log.info("Getting similar profiles for user: {}, limit: {}", currentUser.getId(), limit);
     APIResonse< List<ProfileResponse>> response = new APIResonse<>();
         List<ProfileResponse> profiles = profileService.getSimilarProfiles(currentUser.getId(), limit);
         response.setData(profiles);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }

 /**
  * Check if profile is complete
  */
 @GetMapping("/completeness")
 public ResponseEntity<APIResonse<Boolean>> checkProfileCompleteness(
         @AuthenticationPrincipal User currentUser) {
     
     log.info("Checking profile completeness for user: {}", currentUser.getId());
     APIResonse<Boolean> response = new APIResonse<>();
         boolean isComplete = profileService.isProfileComplete(currentUser.getId());
         response.setData(isComplete);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }

 /**
  * Get profile view count
  */
 @GetMapping("/profileViewCount")
 public ResponseEntity<APIResonse<Long>> getProfileViewCount(
         @AuthenticationPrincipal User currentUser) {
     
     log.info("Getting profile view count for user: {}", currentUser.getId());
     APIResonse<Long> response = new APIResonse<>();
         Long viewCount = profileService.getProfileViewCount(currentUser.getId());
         response.setData(viewCount);
         return new ResponseEntity<>(response, HttpStatus.OK);
 }
}

