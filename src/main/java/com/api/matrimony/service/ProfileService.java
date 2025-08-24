package com.api.matrimony.service;

import java.util.List;

import com.api.matrimony.request.ProfileUpdateRequest;
import com.api.matrimony.response.ProfileResponse;

public interface ProfileService {

	 ProfileResponse getProfileByUserId(Long userId);
	    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);
	    ProfileResponse getPublicProfile(Long userId, Long viewerId);
	    List<ProfileResponse> getRecentProfiles(int limit);
	    List<ProfileResponse> getFeaturedProfiles(int limit);
	    void incrementProfileView(Long profileId, Long viewerId);
	    Long getProfileViewCount(Long profileId);
	    boolean isProfileComplete(Long userId);
	    List<ProfileResponse> getSimilarProfiles(Long userId, int limit);
		Boolean hideProfile(Long id);
	
}
