package com.api.matrimony.serviceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.matrimony.config.JwtUtil;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.Gender;
import com.api.matrimony.enums.OtpPurpose;
import com.api.matrimony.exception.CustomException;
import com.api.matrimony.exception.ResourceNotFoundException;
import com.api.matrimony.repository.OtpVerificationRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.ForgotPasswordRequest;
import com.api.matrimony.request.LoginRequest;
import com.api.matrimony.request.RegisterRequest;
import com.api.matrimony.request.ResetPasswordRequest;
import com.api.matrimony.request.VerifyOtpRequest;
import com.api.matrimony.response.LoginResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.service.AuthService;
import com.api.matrimony.service.OtpService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authentication Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Override
    public String register(RegisterRequest request) {
        log.info("Starting registration process for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email is already registered");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new CustomException("Phone number is already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        user.setIsVerified(false);
        user.setIsActive(false);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getUserType().name().equals("BRIDE") ? Gender.FEMALE : Gender.MALE);

        user.setProfile(profile);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Send OTP for verification
        otpService.sendOtp(request.getPhone(), OtpPurpose.REGISTRATION);

        return "Registration successful. Please verify your phone number with the OTP sent.";
    }

    @Override
    public String verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for contact: {}", request.getContact());

        // Verify OTP
        boolean isValid = otpService.verifyOtp(request.getContact(), request.getOtp(), 
                                             OtpPurpose.valueOf(request.getPurpose()));

        if (!isValid) {
            throw new CustomException("Invalid or expired OTP");
        }

        // Update user verification status
        User user = findUserByEmailOrPhone(request.getContact());
        
        if (request.getPurpose().equals("REGISTRATION")) {
            user.setIsVerified(true);
            user.setIsActive(true);
            user.setPhoneVerified(true);
            user.setEmailVerified(true);
        }

        userRepository.save(user);
        log.info("User verified successfully: {}", user.getId());

        return "Account verified successfully";
    }

    @Override
    public String resendOtp(String contact, String purpose) {
        log.info("Resending OTP for contact: {}", contact);

        // Check if user exists
        User user = findUserByEmailOrPhone(contact);
        
        // Send OTP
        otpService.sendOtp(contact, OtpPurpose.valueOf(purpose));

        return "OTP sent successfully";
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        // Find user
        User user = findUserByEmailOrPhone(request.getEmailOrPhone());

        if (!user.getIsActive()) {
            throw new CustomException("Account is not active. Please contact support.");
        }

        if (!user.getIsVerified()) {
            throw new CustomException("Account is not verified. Please verify your account first.");
        }

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Prepare response
        UserResponse userResponse = mapToUserResponse(user);
        
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getExpirationTime() / 1000); // Convert to seconds
        response.setUser(userResponse);

        log.info("Login successful for user: {}", user.getId());
        return response;
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException("Invalid or expired refresh token");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new CustomException("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate new tokens
        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        UserResponse userResponse = mapToUserResponse(user);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtUtil.getExpirationTime() / 1000);
        response.setUser(userResponse);

        return response;
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for: {}", request.getEmailOrPhone());

        // Find user
        User user = findUserByEmailOrPhone(request.getEmailOrPhone());

        // Send OTP
        String contact = request.getEmailOrPhone().contains("@") ? 
                        user.getPhone() : request.getEmailOrPhone();
        
        otpService.sendOtp(contact, OtpPurpose.PASSWORD_RESET);

        return "Password reset OTP sent successfully";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt for: {}", request.getEmailOrPhone());

        // Find user
        User user = findUserByEmailOrPhone(request.getEmailOrPhone());

        // Verify OTP
        String contact = request.getEmailOrPhone().contains("@") ? 
                        user.getPhone() : request.getEmailOrPhone();
        
        boolean isValid = otpService.verifyOtp(contact, request.getOtp(), OtpPurpose.PASSWORD_RESET);

        if (!isValid) {
            throw new CustomException("Invalid or expired OTP");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getId());
        return "Password reset successfully";
    }

    @Override
    public void logout(String token) {
        log.info("User logout");
        // In a production system, you might want to blacklist the token
        // For now, we'll just log the logout event
        // You can implement token blacklisting using Redis or database
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    // Helper methods
    private User findUserByEmailOrPhone(String emailOrPhone) {
        Optional<User> userOpt;
        
        if (emailOrPhone.contains("@")) {
            userOpt = userRepository.findByEmail(emailOrPhone);
        } else {
            userOpt = userRepository.findByPhone(emailOrPhone);
        }

        return userOpt.orElseThrow(() -> 
                new ResourceNotFoundException("User not found with provided email or phone"));
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        //response.setUserType(user.getUserType().name());
        response.setIsVerified(user.getIsVerified());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLogin(user.getLastLogin());

        // Map profile if exists
        if (user.getProfile() != null) {
            ProfileResponse profileResponse = new ProfileResponse();
            UserProfile profile = user.getProfile();
            
            profileResponse.setId(profile.getId());
            profileResponse.setFirstName(profile.getFirstName());
            profileResponse.setLastName(profile.getLastName());
            profileResponse.setFullName(profile.getFirstName() + " " + profile.getLastName());
            profileResponse.setDateOfBirth(profile.getDateOfBirth());
            
            if (profile.getDateOfBirth() != null) {
                profileResponse.setAge(java.time.Period.between(
                    profile.getDateOfBirth(), java.time.LocalDate.now()).getYears());
            }
            
            profileResponse.setGender(profile.getGender() != null ? profile.getGender().name() : null);
            profileResponse.setHeight(profile.getHeight());
            profileResponse.setWeight(profile.getWeight());
            profileResponse.setMaritalStatus(profile.getMaritalStatus() != null ? 
                                           profile.getMaritalStatus().name() : null);
            profileResponse.setReligion(profile.getReligion());
            profileResponse.setCaste(profile.getCaste());
            profileResponse.setSubCaste(profile.getSubCaste());
            profileResponse.setMotherTongue(profile.getMotherTongue());
            profileResponse.setEducation(profile.getEducation());
            profileResponse.setOccupation(profile.getOccupation());
            profileResponse.setAnnualIncome(profile.getAnnualIncome());
            profileResponse.setAboutMe(profile.getAboutMe());
            profileResponse.setFamilyType(profile.getFamilyType());
            profileResponse.setFamilyValues(profile.getFamilyValues());
            profileResponse.setCity(profile.getCity());
            profileResponse.setState(profile.getState());
            profileResponse.setCountry(profile.getCountry());
            profileResponse.setPincode(profile.getPincode());
            profileResponse.setProfileCreatedBy(profile.getProfileCreatedBy());
            profileResponse.setCreatedAt(profile.getCreatedAt());
            profileResponse.setUpdatedAt(profile.getUpdatedAt());
            
            response.setProfile(profileResponse);
        }

        return response;
    }
}
