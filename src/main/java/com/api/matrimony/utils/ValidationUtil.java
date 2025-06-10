package com.api.matrimony.utils;



import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Validation Utility class for common validation operations
 */
@Slf4j
public class ValidationUtil {

    // Regular expressions for validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    private static final String PHONE_REGEX = "^[6-9]\\d{9}$"; // Indian phone number format
    private static final String NAME_REGEX = "^[A-Za-z\\s]{2,50}$";
    private static final String PINCODE_REGEX = "^[1-9][0-9]{5}$"; // Indian pincode format
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
    private static final Pattern PINCODE_PATTERN = Pattern.compile(PINCODE_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number (Indian format)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Remove any spaces, dashes, or plus signs
        String cleanPhone = phone.replaceAll("[\\s\\-\\+]", "");
        
        // Handle country code
        if (cleanPhone.startsWith("91") && cleanPhone.length() == 12) {
            cleanPhone = cleanPhone.substring(2);
        }
        
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate name (first name, last name)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate pincode (Indian format)
     */
    public static boolean isValidPincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) {
            return false;
        }
        return PINCODE_PATTERN.matcher(pincode.trim()).matches();
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate age range
     */
    public static boolean isValidAgeRange(Integer minAge, Integer maxAge) {
        if (minAge == null || maxAge == null) {
            return false;
        }
        return minAge >= 18 && maxAge <= 80 && minAge <= maxAge;
    }

    /**
     * Validate height (in cm)
     */
    public static boolean isValidHeight(Integer height) {
        if (height == null) {
            return false;
        }
        return height >= 120 && height <= 250; // Reasonable height range
    }

    /**
     * Validate weight (in kg)
     */
    public static boolean isValidWeight(Integer weight) {
        if (weight == null) {
            return false;
        }
        return weight >= 30 && weight <= 200; // Reasonable weight range
    }

    /**
     * Validate income (annual income in INR)
     */
    public static boolean isValidIncome(Double income) {
        if (income == null) {
            return true; // Income is optional
        }
        return income >= 0 && income <= 100000000; // Up to 10 crores
    }

    /**
     * Validate OTP
     */
    public static boolean isValidOtp(String otp) {
        if (otp == null || otp.trim().isEmpty()) {
            return false;
        }
        return otp.trim().matches("^\\d{6}$"); // 6-digit OTP
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return minLength == 0;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validate if string is not null and not empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate enum value
     */
    public static boolean isValidEnum(String value, Class<? extends Enum<?>> enumClass) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            Enum.valueOf((Class<Enum>) enumClass, value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Sanitize string input (remove harmful characters)
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove HTML tags and special characters that could be used for XSS
        return input.replaceAll("<[^>]*>", "")
                   .replaceAll("[<>\"'%;()&+]", "")
                   .trim();
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate file extension
     */
    public static boolean isValidImageExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String extension = filename.toLowerCase();
        return extension.endsWith(".jpg") || 
               extension.endsWith(".jpeg") || 
               extension.endsWith(".png") || 
               extension.endsWith(".gif") || 
               extension.endsWith(".webp");
    }

    /**
     * Validate coordinates (latitude, longitude)
     */
    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }

    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Get password strength description
     */
    public static String getPasswordStrengthDescription(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");
        
        if (!hasLowercase) return "Password must contain at least one lowercase letter";
        if (!hasUppercase) return "Password must contain at least one uppercase letter";
        if (!hasDigit) return "Password must contain at least one digit";
        if (!hasSpecial) return "Password must contain at least one special character (@$!%*?&)";
        
        return "Strong password";
    }

    /**
     * Validate pagination parameters
     */
    public static boolean isValidPageSize(int size) {
        return size > 0 && size <= 100; // Maximum 100 items per page
    }

    public static boolean isValidPageNumber(int page) {
        return page >= 0;
    }
}
