package com.api.matrimony.utils;


import java.util.Arrays;
import java.util.List;

import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.entity.UserProfile;

import lombok.extern.slf4j.Slf4j;

/**
 * Matching Algorithm utility for calculating compatibility scores
 */

@Slf4j
public class MatchingAlgorithm {

    // Weight factors for different compatibility criteria
    private static final double AGE_WEIGHT = 0.20;           // 20%
    private static final double LOCATION_WEIGHT = 0.15;      // 15%
    private static final double RELIGION_WEIGHT = 0.25;      // 25%
    private static final double CASTE_WEIGHT = 0.10;         // 10%
    private static final double EDUCATION_WEIGHT = 0.15;     // 15%
    private static final double OCCUPATION_WEIGHT = 0.05;    // 5%
    private static final double MOTHER_TONGUE_WEIGHT = 0.10; // 10%

    /**
     * Calculate overall compatibility score between two profiles
     */
    public static double calculateCompatibilityScore(UserProfile profile1, UserProfile profile2,
                                                   UserPreference pref1, UserPreference pref2) {
        
        if (profile1 == null || profile2 == null) {
            return 0.0;
        }

        double totalScore = 0.0;
        int applicableFactors = 0;

        // Age compatibility
        double ageScore = calculateAgeCompatibility(profile1, profile2, pref1, pref2);
        if (ageScore >= 0) {
            totalScore += ageScore * AGE_WEIGHT;
            applicableFactors++;
        }

        // Location compatibility
        double locationScore = calculateLocationCompatibility(profile1, profile2);
        if (locationScore >= 0) {
            totalScore += locationScore * LOCATION_WEIGHT;
            applicableFactors++;
        }

        // Religion compatibility
        double religionScore = calculateReligionCompatibility(profile1, profile2);
        if (religionScore >= 0) {
            totalScore += religionScore * RELIGION_WEIGHT;
            applicableFactors++;
        }

        // Caste compatibility
        double casteScore = calculateCasteCompatibility(profile1, profile2);
        if (casteScore >= 0) {
            totalScore += casteScore * CASTE_WEIGHT;
            applicableFactors++;
        }

        // Education compatibility
        double educationScore = calculateEducationCompatibility(profile1, profile2);
        if (educationScore >= 0) {
            totalScore += educationScore * EDUCATION_WEIGHT;
            applicableFactors++;
        }

        // Occupation compatibility
        double occupationScore = calculateOccupationCompatibility(profile1, profile2);
        if (occupationScore >= 0) {
            totalScore += occupationScore * OCCUPATION_WEIGHT;
            applicableFactors++;
        }

        // Mother tongue compatibility
        double motherTongueScore = calculateMotherTongueCompatibility(profile1, profile2);
        if (motherTongueScore >= 0) {
            totalScore += motherTongueScore * MOTHER_TONGUE_WEIGHT;
            applicableFactors++;
        }

        // Normalize score based on applicable factors
        double finalScore = applicableFactors > 0 ? (totalScore / (applicableFactors * getMaxWeightSum(applicableFactors))) * 100 : 0.0;
        
        log.debug("Compatibility score calculated: {} for profiles {} and {}", 
                 finalScore, profile1.getId(), profile2.getId());
        
        return Math.min(100.0, Math.max(0.0, finalScore));
    }

    /**
     * Calculate age compatibility
     */
    public static double calculateAgeCompatibility(UserProfile profile1, UserProfile profile2,
                                                 UserPreference pref1, UserPreference pref2) {
        
        if (profile1.getDateOfBirth() == null || profile2.getDateOfBirth() == null) {
            return -1; // Cannot calculate
        }

        int age1 = DateUtil.calculateAge(profile1.getDateOfBirth());
        int age2 = DateUtil.calculateAge(profile2.getDateOfBirth());

        double score = 0.0;
        int checks = 0;

        // Check if profile2's age is within profile1's preference
        if (pref1 != null && pref1.getMinAge() != null && pref1.getMaxAge() != null) {
            if (age2 >= pref1.getMinAge() && age2 <= pref1.getMaxAge()) {
                score += 100.0;
            } else {
                // Partial score based on how close the age is
                int deviation = Math.min(Math.abs(age2 - pref1.getMinAge()), Math.abs(age2 - pref1.getMaxAge()));
                score += Math.max(0, 100 - (deviation * 10)); // 10 points deduction per year deviation
            }
            checks++;
        }

        // Check if profile1's age is within profile2's preference
        if (pref2 != null && pref2.getMinAge() != null && pref2.getMaxAge() != null) {
            if (age1 >= pref2.getMinAge() && age1 <= pref2.getMaxAge()) {
                score += 100.0;
            } else {
                int deviation = Math.min(Math.abs(age1 - pref2.getMinAge()), Math.abs(age1 - pref2.getMaxAge()));
                score += Math.max(0, 100 - (deviation * 10));
            }
            checks++;
        }

        return checks > 0 ? score / checks : 50.0; // Default score if no preferences set
    }

    /**
     * Calculate location compatibility
     */
    public static double calculateLocationCompatibility(UserProfile profile1, UserProfile profile2) {
        if (profile1.getCity() == null || profile2.getCity() == null) {
            return -1;
        }

        // Same city - highest score
        if (profile1.getCity().equalsIgnoreCase(profile2.getCity())) {
            return 100.0;
        }

        // Same state - high score
        if (profile1.getState() != null && profile2.getState() != null &&
            profile1.getState().equalsIgnoreCase(profile2.getState())) {
            return 75.0;
        }

        // Same country - medium score
        if (profile1.getCountry() != null && profile2.getCountry() != null &&
            profile1.getCountry().equalsIgnoreCase(profile2.getCountry())) {
            return 50.0;
        }

        // Different countries - low score
        return 25.0;
    }

    /**
     * Calculate religion compatibility
     */
    public static double calculateReligionCompatibility(UserProfile profile1, UserProfile profile2) {
        if (profile1.getReligion() == null || profile2.getReligion() == null) {
            return -1;
        }

        if (profile1.getReligion().equalsIgnoreCase(profile2.getReligion())) {
            return 100.0;
        }

        return 0.0; // Different religions
    }

    /**
     * Calculate caste compatibility
     */
    public static double calculateCasteCompatibility(UserProfile profile1, UserProfile profile2) {
        if (profile1.getCaste() == null || profile2.getCaste() == null) {
            return 50.0; // Neutral if caste not specified
        }

        if (profile1.getCaste().equalsIgnoreCase(profile2.getCaste())) {
            return 100.0;
        }

        // Check sub-caste compatibility
        if (profile1.getSubCaste() != null && profile2.getSubCaste() != null &&
            profile1.getSubCaste().equalsIgnoreCase(profile2.getSubCaste())) {
            return 75.0;
        }

        return 25.0; // Different castes
    }

    /**
     * Calculate education compatibility
     */
    public static double calculateEducationCompatibility(UserProfile profile1, UserProfile profile2) {
        if (profile1.getEducation() == null || profile2.getEducation() == null) {
            return -1;
        }

        String edu1 = profile1.getEducation().toLowerCase();
        String edu2 = profile2.getEducation().toLowerCase();

        if (edu1.equals(edu2)) {
            return 100.0;
        }

        // Define education levels
        List<String> graduateLevel = Arrays.asList("graduate", "bachelor", "b.tech", "b.com", "b.sc", "ba");
        List<String> postGraduateLevel = Arrays.asList("post graduate", "master", "m.tech", "mba", "m.com", "m.sc", "ma");
        List<String> doctorateLevel = Arrays.asList("doctorate", "phd", "md");

        int level1 = getEducationLevel(edu1, graduateLevel, postGraduateLevel, doctorateLevel);
        int level2 = getEducationLevel(edu2, graduateLevel, postGraduateLevel, doctorateLevel);

        int difference = Math.abs(level1 - level2);
        
        // Same level - high score
        if (difference == 0) {
            return 80.0;
        }
        // One level difference - medium score
        else if (difference == 1) {
            return 60.0;
        }
        // Two or more levels difference - low score
        else {
            return 30.0;
        }
    }

    /**
     * Calculate occupation compatibility
     */
    public static double calculateOccupationCompatibility(UserProfile profile1, UserProfile profile2) {
        if (profile1.getOccupation() == null || profile2.getOccupation() == null) {
            return -1;
        }

        if (profile1.getOccupation().equalsIgnoreCase(profile2.getOccupation())) {
            return 100.0;
        }

        // Check if both are in similar fields (basic categorization)
        String occ1 = profile1.getOccupation().toLowerCase();
        String occ2 = profile2.getOccupation().toLowerCase();

        if (areSimilarOccupations(occ1, occ2)) {
            return 70.0;
        }

        return 30.0; // Different occupations
    }

    /**
     * Calculate mother tongue compatibility
     */
    public static double calculateMotherTongueCompatibility(UserProfile profile1, UserProfile profile2) {
        if (profile1.getMotherTongue() == null || profile2.getMotherTongue() == null) {
            return -1;
        }

        if (profile1.getMotherTongue().equalsIgnoreCase(profile2.getMotherTongue())) {
            return 100.0;
        }

        return 20.0; // Different mother tongues
    }

    // Helper methods
    private static int getEducationLevel(String education, List<String> graduate, 
                                       List<String> postGraduate, List<String> doctorate) {
        for (String level : doctorate) {
            if (education.contains(level)) return 3;
        }
        for (String level : postGraduate) {
            if (education.contains(level)) return 2;
        }
        for (String level : graduate) {
            if (education.contains(level)) return 1;
        }
        return 0; // Below graduate
    }

    private static boolean areSimilarOccupations(String occ1, String occ2) {
        // Tech fields
        List<String> techFields = Arrays.asList("software", "engineer", "developer", "programmer", "it", "tech");
        boolean isTech1 = techFields.stream().anyMatch(occ1::contains);
        boolean isTech2 = techFields.stream().anyMatch(occ2::contains);
        
        if (isTech1 && isTech2) return true;

        // Medical fields
        List<String> medicalFields = Arrays.asList("doctor", "nurse", "medical", "physician", "surgeon");
        boolean isMedical1 = medicalFields.stream().anyMatch(occ1::contains);
        boolean isMedical2 = medicalFields.stream().anyMatch(occ2::contains);
        
        if (isMedical1 && isMedical2) return true;

        // Business fields
        List<String> businessFields = Arrays.asList("business", "manager", "executive", "analyst", "consultant");
        boolean isBusiness1 = businessFields.stream().anyMatch(occ1::contains);
        boolean isBusiness2 = businessFields.stream().anyMatch(occ2::contains);
        
        return isBusiness1 && isBusiness2;
    }

    private static double getMaxWeightSum(int applicableFactors) {
        // This should return the sum of weights for applicable factors
        // For simplicity, returning 1.0 as we normalize by number of factors
        return 1.0;
    }
}
