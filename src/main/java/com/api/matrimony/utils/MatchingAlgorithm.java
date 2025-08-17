package com.api.matrimony.utils;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.Gender;
import com.api.matrimony.response.GetMatchResponce;
import com.api.matrimony.response.MatchResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.serviceImpl.ProfileServiceImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * Matching Algorithm utility for calculating compatibility scores
 */

@Component
@Slf4j
public class MatchingAlgorithm {
	
	@Autowired
	ProfileServiceImpl profileServiceImpl;
	
	 public GetMatchResponce calculateMatchScore(UserProfile candidate, UserPreference preferences) {
	        double totalScore = 0.0;
	        double totalWeight = 0.0;
	        
	        // 1. Gender match (mandatory - 0 score if no match)
	        if (!matchesGender(candidate.getGender(), preferences.getGender())) {
	            return buildMatchResponse(candidate, 0.0);
	        }
	        
	        // 2. Age match (weight: 15)
	        Integer age = calculateAge(candidate.getDateOfBirth());
	        double ageScore = calculateAgeScore(age, preferences);
	        totalScore += ageScore * 15;
	        totalWeight += 15;
	        
	        // 3. Height match (weight: 10)
	        double heightScore = calculateHeightScore(candidate.getHeight(), preferences);
	        totalScore += heightScore * 10;
	        totalWeight += 10;
	        
	        // 4. Marital status match (weight: 15)
	        double maritalScore = calculateEnumScore(
	                candidate.getMaritalStatus() != null ? candidate.getMaritalStatus().toString() : null, 
	                preferences.getMaritalStatuses());
	        totalScore += maritalScore * 15;
	        totalWeight += 15;
	        
	        // 5. Religion match (weight: 12)
	        double religionScore = calculateEnumScore(candidate.getReligion(), 
	                preferences.getReligions());
	        totalScore += religionScore * 12;
	        totalWeight += 12;
	        
	        // 6. Caste match (weight: 10)
	        double casteScore = calculateEnumScore(candidate.getCaste(), 
	                preferences.getCastes());
	        totalScore += casteScore * 10;
	        totalWeight += 10;
	        
	        // 7. Education match (weight: 12)
	        double educationScore = calculateEnumScore(candidate.getEducation(), 
	                preferences.getEducation());
	        totalScore += educationScore * 12;
	        totalWeight += 12;
	        
	        // 8. Occupation match (weight: 10)
	        double occupationScore = calculateEnumScore(candidate.getOccupation(), 
	                preferences.getOccupation());
	        totalScore += occupationScore * 10;
	        totalWeight += 10;
	        
	        // 9. Income match (weight: 8)
	        double incomeScore = calculateIncomeScore(candidate.getAnnualIncome(), preferences);
	        totalScore += incomeScore * 8;
	        totalWeight += 8;
	        
	        // 10. Location match (weight: 8)
	        double locationScore = calculateLocationScore(candidate, preferences);
	        totalScore += locationScore * 8;
	        totalWeight += 8;
	        
	        // Calculate final percentage
	        double matchPercentage = (totalScore / totalWeight) * 100;
	        
	        return buildMatchResponse(candidate, matchPercentage);
	    }
	    
	    public Integer calculateAge(LocalDate dateOfBirth) {
	        if (dateOfBirth == null) return null;
	        return Period.between(dateOfBirth, LocalDate.now()).getYears();
	    }
	    
	    public boolean matchesGender(Gender candidateGender, String preferredGender) {
	        if (candidateGender == null || preferredGender == null) {
	            return false;
	        }
	        return candidateGender.toString().equalsIgnoreCase(preferredGender);
	    }
	    
	    public double calculateAgeScore(Integer age, UserPreference preferences) {
	        if (age == null) return 0.0;
	        
	        if (preferences.getMinAge() != null && age < preferences.getMinAge()) {
	            return 0.0;
	        }
	        if (preferences.getMaxAge() != null && age > preferences.getMaxAge()) {
	            return 0.0;
	        }
	        return 1.0;
	    }
	    
	    public double calculateHeightScore(Integer height, UserPreference preferences) {
	        if (height == null) return 0.5; // Neutral score if height not provided
	        
	        if (preferences.getMinHeight() != null && height < preferences.getMinHeight()) {
	            return 0.0;
	        }
	        if (preferences.getMaxHeight() != null && height > preferences.getMaxHeight()) {
	            return 0.0;
	        }
	        return 1.0;
	    }
	    
	    public double calculateEnumScore(String candidateValue, String preferenceValues) {
	        if (candidateValue == null || preferenceValues == null || preferenceValues.isEmpty()) {
	            return 0.5; // Neutral score if not specified
	        }
	        
	        Set<String> preferences = Arrays.stream(preferenceValues.split(","))
	                .map(String::trim)
	                .map(String::toUpperCase)
	                .collect(Collectors.toSet());
	        
	        return preferences.contains(candidateValue.toUpperCase()) ? 1.0 : 0.0;
	    }
	    
	    public double calculateIncomeScore(BigDecimal income, UserPreference preferences) {
	        if (income == null) return 0.5; // Neutral score if income not provided
	        
	        if (preferences.getMinIncome() != null && income.compareTo(preferences.getMinIncome()) < 0) {
	            return 0.0;
	        }
	        if (preferences.getMaxIncome() != null && income.compareTo(preferences.getMaxIncome()) > 0) {
	            return 0.0;
	        }
	        return 1.0;
	    }
	    
	    public double calculateLocationScore(UserProfile profile, UserPreference preferences) {
	        double score = 0.0;
	        int factors = 0;
	        
	        // Country match
	        if (preferences.getCountries() != null && profile.getCountry() != null) {
	            if (matchesLocation(profile.getCountry(), preferences.getCountries())) {
	                score += 1.0;
	            }
	            factors++;
	        }
	        
	        // State match
	        if (preferences.getStates() != null && profile.getState() != null) {
	            if (matchesLocation(profile.getState(), preferences.getStates())) {
	                score += 1.0;
	            }
	            factors++;
	        }
	        
	        // City match
	        if (preferences.getCities() != null && profile.getCity() != null) {
	            if (matchesLocation(profile.getCity(), preferences.getCities())) {
	                score += 1.0;
	            }
	            factors++;
	        }
	        
	        return factors > 0 ? score / factors : 0.5;
	    }
	    
	    public boolean matchesLocation(String candidateLocation, String preferenceLocations) {
	        Set<String> locations = Arrays.stream(preferenceLocations.split(","))
	                .map(String::trim)
	                .map(String::toUpperCase)
	                .collect(Collectors.toSet());
	        
	        return locations.contains(candidateLocation.toUpperCase());
	    }
	    
	    public GetMatchResponce buildMatchResponse(UserProfile profile, double matchScore) {
	    	GetMatchResponce response = new GetMatchResponce();
	    	ProfileResponse profileResponse = profileServiceImpl.mapToProfileResponse(profile);
	    	response.setProfileResponse(profileResponse);
	        response.setMatchScore(Math.round(matchScore * 10.0) / 10.0); // Round to 1 decimal place
	        log.error(" Match profile responce is -> "+response);
	        return response;
	    }
	    
	    public Boolean checkIfUserHasPhotos(Long userId) {
	        // TODO: Implement logic to check if user has photos
	        // This might involve checking a user_photos table or a field in user profile
	        // For now, returning false as default
	        return false;
	    }
}
