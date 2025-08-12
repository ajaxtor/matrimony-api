package com.api.matrimony.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.request.RecommendationScore;
import com.api.matrimony.response.LocationResponce;
import com.api.matrimony.response.MatchResponse;

@Component
public class RecommendationCal {
    
    public RecommendationScore calculateRecommendationScore(UserProfile userProfile, UserProfile candidateProfile) {
        RecommendationScore score = new RecommendationScore(candidateProfile);
        
        // 1. Location-based scoring (same city/state gets higher score)
        double locationScore = calculateLocationScore(userProfile, candidateProfile);
        score.addScore("location", locationScore * 25);
        
        // 2. Age proximity scoring
        double ageScore = calculateAgeProximityScore(userProfile, candidateProfile);
        score.addScore("age", ageScore * 20);
        
        // 3. Education similarity
        double educationScore = calculateEducationScore(userProfile, candidateProfile);
        score.addScore("education", educationScore * 15);
        
        // 4. Professional similarity (occupation)
        double professionalScore = calculateProfessionalScore(userProfile, candidateProfile);
        score.addScore("professional", professionalScore * 15);
        
        // 5. Cultural similarity (religion, caste, mother tongue)
        double culturalScore = calculateCulturalScore(userProfile, candidateProfile);
        score.addScore("cultural", culturalScore * 15);
        
        // 6. Lifestyle similarity (diet, family type)
        double lifestyleScore = calculateLifestyleScore(userProfile, candidateProfile);
        score.addScore("lifestyle", lifestyleScore * 10);
        
        // 7. Profile completeness bonus
        double completenessScore = calculateProfileCompletenessScore(candidateProfile);
        score.addScore("completeness", completenessScore * 5);
        
        // 8. Activity score (recently active profiles)
        double activityScore = calculateActivityScore(candidateProfile);
        score.addScore("activity", activityScore * 5);
        
        return score;
    }
    
    public double calculateLocationScore(UserProfile profile1, UserProfile profile2) {
        double score = 0.0;
        
        // Same city - highest score
        if (profile1.getCity() != null && profile2.getCity() != null &&
            profile1.getCity().equalsIgnoreCase(profile2.getCity())) {
            return 1.0;
        }
        
        // Same state - medium score
        if (profile1.getState() != null && profile2.getState() != null &&
            profile1.getState().equalsIgnoreCase(profile2.getState())) {
            return 0.7;
        }
        
        // Same country - low score
        if (profile1.getCountry() != null && profile2.getCountry() != null &&
            profile1.getCountry().equalsIgnoreCase(profile2.getCountry())) {
            return 0.3;
        }
        
        return score;
    }
    
    public double calculateAgeProximityScore(UserProfile profile1, UserProfile profile2) {
        Integer age1 = calculateAge(profile1.getDateOfBirth());
        Integer age2 = calculateAge(profile2.getDateOfBirth());
        
        if (age1 == null || age2 == null) return 0.0;
        
        int ageDiff = Math.abs(age1 - age2);
        
        // Score based on age difference
        if (ageDiff <= 2) return 1.0;
        if (ageDiff <= 5) return 0.8;
        if (ageDiff <= 8) return 0.6;
        if (ageDiff <= 12) return 0.4;
        if (ageDiff <= 15) return 0.2;
        
        return 0.0;
    }
    
    public double calculateEducationScore(UserProfile profile1, UserProfile profile2) {
        if (profile1.getEducation() == null || profile2.getEducation() == null) {
            return 0.0;
        }
        
        // Same education level
        if (profile1.getEducation().equalsIgnoreCase(profile2.getEducation())) {
            return 1.0;
        }
        
        // Similar education levels (you can expand this logic)
        Set<String> higherEducation = Set.of("POST_GRADUATE", "DOCTORATE", "MASTERS", "PHD");
        Set<String> graduateEducation = Set.of("GRADUATE", "BACHELOR", "ENGINEERING");
        
        boolean both1Higher = higherEducation.contains(profile1.getEducation().toUpperCase());
        boolean both2Higher = higherEducation.contains(profile2.getEducation().toUpperCase());
        
        if (both1Higher && both2Higher) return 0.8;
        
        boolean both1Graduate = graduateEducation.contains(profile1.getEducation().toUpperCase());
        boolean both2Graduate = graduateEducation.contains(profile2.getEducation().toUpperCase());
        
        if (both1Graduate && both2Graduate) return 0.8;
        
        return 0.3;
    }
    
    public double calculateProfessionalScore(UserProfile profile1, UserProfile profile2) {
        if (profile1.getOccupation() == null || profile2.getOccupation() == null) {
            return 0.0;
        }
        
        // Same occupation
        if (profile1.getOccupation().equalsIgnoreCase(profile2.getOccupation())) {
            return 1.0;
        }
        
        // Similar professional fields
        Map<String, Set<String>> professionalGroups = new HashMap<>();
        professionalGroups.put("TECH", Set.of("ENGINEER", "SOFTWARE", "IT", "DEVELOPER", "PROGRAMMER"));
        professionalGroups.put("MEDICAL", Set.of("DOCTOR", "NURSE", "MEDICAL", "HEALTHCARE"));
        professionalGroups.put("BUSINESS", Set.of("BUSINESS", "ENTREPRENEUR", "MANAGER", "CEO"));
        professionalGroups.put("EDUCATION", Set.of("TEACHER", "PROFESSOR", "EDUCATOR", "LECTURER"));
        
        String occupation1Upper = profile1.getOccupation().toUpperCase();
        String occupation2Upper = profile2.getOccupation().toUpperCase();
        
        for (Set<String> group : professionalGroups.values()) {
            boolean inGroup1 = group.stream().anyMatch(occupation1Upper::contains);
            boolean inGroup2 = group.stream().anyMatch(occupation2Upper::contains);
            
            if (inGroup1 && inGroup2) {
                return 0.7;
            }
        }
        
        return 0.0;
    }
    
    public double calculateCulturalScore(UserProfile profile1, UserProfile profile2) {
        double score = 0.0;
        int factors = 0;
        
        // Religion match
        if (profile1.getReligion() != null && profile2.getReligion() != null) {
            if (profile1.getReligion().equalsIgnoreCase(profile2.getReligion())) {
                score += 1.0;
            }
            factors++;
        }
        
        // Mother tongue match
        if (profile1.getMotherTongue() != null && profile2.getMotherTongue() != null) {
            if (profile1.getMotherTongue().equalsIgnoreCase(profile2.getMotherTongue())) {
                score += 1.0;
            }
            factors++;
        }
        
        // Caste match (less weight)
        if (profile1.getCaste() != null && profile2.getCaste() != null) {
            if (profile1.getCaste().equalsIgnoreCase(profile2.getCaste())) {
                score += 0.5;
            }
            factors++;
        }
        
        return factors > 0 ? score / factors : 0.0;
    }
    
    public double calculateLifestyleScore(UserProfile profile1, UserProfile profile2) {
        double score = 0.0;
        int factors = 0;
        
        // Diet match
        if (profile1.getDiet() != null && profile2.getDiet() != null) {
            if (profile1.getDiet().equalsIgnoreCase(profile2.getDiet())) {
                score += 1.0;
            }
            factors++;
        }
        
        // Family type match
        if (profile1.getFamilyType() != null && profile2.getFamilyType() != null) {
            if (profile1.getFamilyType().equalsIgnoreCase(profile2.getFamilyType())) {
                score += 1.0;
            }
            factors++;
        }
        
        return factors > 0 ? score / factors : 0.0;
    }
    
    public double calculateProfileCompletenessScore(UserProfile profile) {
        int filledFields = 0;
        int totalFields = 15; // Adjust based on important fields
        
        if (profile.getFullName() != null) filledFields++;
        if (profile.getDateOfBirth() != null) filledFields++;
        if (profile.getHeight() != null) filledFields++;
        if (profile.getEducation() != null) filledFields++;
        if (profile.getOccupation() != null) filledFields++;
        if (profile.getCity() != null) filledFields++;
        if (profile.getState() != null) filledFields++;
        if (profile.getReligion() != null) filledFields++;
        if (profile.getMotherTongue() != null) filledFields++;
        if (profile.getAboutMe() != null && !profile.getAboutMe().isEmpty()) filledFields++;
        if (profile.getAnnualIncome() != null) filledFields++;
        if (profile.getMaritalStatus() != null) filledFields++;
        if (profile.getDiet() != null) filledFields++;
        if (profile.getFamilyType() != null) filledFields++;
        if (checkIfUserHasPhotos(profile.getUser().getId())) filledFields++;
        
        return (double) filledFields / totalFields;
    }
    
    public double calculateActivityScore(UserProfile profile) {
        if (profile.getUpdatedAt() == null) return 0.0;
        
        long daysSinceUpdate = Period.between(
            profile.getUpdatedAt().toLocalDate(),
            LocalDate.now()
        ).getDays();
        
        // Active in last 7 days
        if (daysSinceUpdate <= 7) return 1.0;
        // Active in last 30 days
        if (daysSinceUpdate <= 30) return 0.7;
        // Active in last 90 days
        if (daysSinceUpdate <= 90) return 0.4;
        
        return 0.1;
    }
    
    public Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    
    public MatchResponse buildMatchResponse(UserProfile profile, double score) {
        LocationResponce location = new LocationResponce();
        location.setCountry(profile.getCountry());
        location.setState(profile.getState());
        location.setCity(profile.getCity());
        
        MatchResponse response = new MatchResponse();
        response.setUserId(profile.getUser().getId());
        response.setName(profile.getFullName());
        response.setAge(calculateAge(profile.getDateOfBirth()));
        response.setGender(profile.getGender() != null ? profile.getGender().toString().toLowerCase() : null);
        response.setLocation(location);
        response.setEducation(profile.getEducation());
        response.setOccupation(profile.getOccupation());
        response.setHasPhotos(checkIfUserHasPhotos(profile.getUser().getId()));
        response.setMatchScore(Math.round(score * 10.0) / 10.0); // Round to 1 decimal place
        
        return response;
    }
    
    public Boolean checkIfUserHasPhotos(Long userId) {
        // TODO: Implement actual photo check logic
        return false;
    }
    
}
