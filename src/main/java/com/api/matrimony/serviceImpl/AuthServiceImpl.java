package com.api.matrimony.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.api.matrimony.entity.UserPreference;
import com.api.matrimony.response.PreferenceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.matrimony.config.JwtUtil;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserPhoto;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.Gender;
import com.api.matrimony.enums.OtpPurpose;
import com.api.matrimony.enums.UserType;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.OtpVerificationRepository;
import com.api.matrimony.repository.UserPhotoRepository;
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
	private final UserPhotoRepository photoRepository;

	@Override
	public String register(RegisterRequest request) {
		log.info("Starting registration process for email: {}", request.getEmail());

		// Check if user already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new ApplicationException(ErrorEnum.EMAIL_ALREADY_EXIST.toString(),
					ErrorEnum.EMAIL_ALREADY_EXIST.getExceptionError(), HttpStatus.OK);
		}

		if (userRepository.existsByPhone(request.getPhone())) {
			throw new ApplicationException(ErrorEnum.NUMBER_ALREADY_EXIST.toString(),
					ErrorEnum.NUMBER_ALREADY_EXIST.getExceptionError(), HttpStatus.OK);
		}

		// Create new user
		User user = new User();
		user.setEmail(request.getEmail());
		user.setPhone(request.getPhone());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setUserType(request.getGender().name().equalsIgnoreCase("MALE")? UserType.GROOM : UserType.GROOM);
		user.setIsVerified(false);
		user.setIsActive(false);
		user.setEmailVerified(false);
		user.setPhoneVerified(false);

		// Create user profile
		UserProfile profile = new UserProfile();
		profile.setUser(user);
		profile.setFullName(request.getFullName());
		profile.setGender(request.getGender());

		user.setProfile(profile);

		// Save user
		User savedUser = userRepository.save(user);
		log.info("User registered successfully with ID: {}", savedUser.getId());

		// Send OTP for verification
		otpService.sendOtp(request.getPhone(), request.getEmail(), OtpPurpose.REGISTRATION);

		return "Registration successful. Please verify your phone number / Email  with the OTP sent.";
	}

	@Override
	public String verifyOtp(VerifyOtpRequest request) {
		log.info("Verifying OTP for contact: {}", request.getEmail());

		// Verify OTP
		boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp(),
				OtpPurpose.valueOf(request.getPurpose()));

		if (!isValid) {
			throw new ApplicationException(ErrorEnum.INVALID_OTP.toString(), ErrorEnum.INVALID_OTP.getExceptionError(),
					HttpStatus.OK);
		}

		// Update user verification status
		User user = findUserByEmailOrPhone(request.getEmail());

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
		User user = findUserByEmailOrPhone(contact);
		otpService.sendOtp(user.getPhone(), user.getEmail(), OtpPurpose.valueOf(purpose));

		return "OTP sent successfully";
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		log.info("Login attempt for: {}", request.getEmailOrPhone());

		// Find user
		User user = findUserByEmailOrPhone(request.getEmailOrPhone());

		if (!user.getIsActive()) {
			throw new ApplicationException(ErrorEnum.INACTIVE_ACCOUNT.toString(),
					ErrorEnum.INACTIVE_ACCOUNT.getExceptionError(), HttpStatus.OK);
		}

		if (!user.getIsVerified()) {
			throw new ApplicationException(ErrorEnum.ACCOUNT_IS_NOT_VERIFIED.toString(),
					ErrorEnum.ACCOUNT_IS_NOT_VERIFIED.getExceptionError(), HttpStatus.OK);
		}

		// Authenticate user
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword()));

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
			throw new ApplicationException(ErrorEnum.REFRESH_TOKEN_EXP.toString(),
					ErrorEnum.REFRESH_TOKEN_EXP.getExceptionError(), HttpStatus.OK);
		}

		if (!jwtUtil.isRefreshToken(refreshToken)) {
			throw new ApplicationException(ErrorEnum.REFRESH_TOKEN_EXP.toString(),
					ErrorEnum.REFRESH_TOKEN_EXP.getExceptionError(), HttpStatus.OK);
		}

		String username = jwtUtil.extractUsername(refreshToken);
		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

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
		User user = findUserByEmailOrPhone(request.getEmailOrPhone());
		otpService.sendOtp(user.getPhone(), user.getEmail(), OtpPurpose.PASSWORD_RESET);

		return "Password reset OTP sent successfully";
	}

	@Override
	public String resetPassword(ResetPasswordRequest request) {
		log.info("Password reset attempt for: {}", request.getEmailOrPhone());

		// Find user
		User user = findUserByEmailOrPhone(request.getEmailOrPhone());

		// Verify OTP
		String contact = request.getEmailOrPhone().contains("@") ? user.getPhone() : request.getEmailOrPhone();

		boolean isValid = otpService.verifyOtp(contact, request.getOtp(), OtpPurpose.PASSWORD_RESET);

		if (!isValid) {
			throw new ApplicationException(ErrorEnum.INVALID_OTP.toString(), ErrorEnum.INVALID_OTP.getExceptionError(),
					HttpStatus.OK);
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

		return userOpt.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
				ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));
	}

	private UserResponse mapToUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setEmail(user.getEmail());
		response.setPhone(user.getPhone());
		// response.setUserType(user.getUserType().name());
		response.setIsVerified(user.getIsVerified());
		response.setIsActive(user.getIsActive());
		response.setCreatedAt(user.getCreatedAt());
		response.setLastLogin(user.getLastLogin());

		// Map profile if exists
		if (user.getProfile() != null) {
			ProfileResponse profileResponse = new ProfileResponse();
			UserProfile profile = user.getProfile();

			profileResponse.setId(profile.getId());
			profileResponse.setFullName(profile.getFullName());
			profileResponse.setDateOfBirth(profile.getDateOfBirth());

			if (profile.getDateOfBirth() != null) {
				profileResponse.setAge(
						java.time.Period.between(profile.getDateOfBirth(), java.time.LocalDate.now()).getYears());
			}
			List<UserPhoto> photos = photoRepository.findByUserIdOrderByDisplayOrderAsc(profile.getUser().getId());
			profileResponse.setPhotoUrls(photos.stream().map(UserPhoto::getPhotoUrl).collect(Collectors.toList()));

			// Get primary photo
			photos.stream().filter(UserPhoto::getIsPrimary).findFirst()
					.ifPresent(photo -> profileResponse.setPrimaryPhotoUrl(photo.getPhotoUrl()));

			profileResponse.setGender(profile.getGender() != null ? profile.getGender().name() : null);
			profileResponse.setHeight(profile.getHeight());
			profileResponse.setWeight(profile.getWeight());
			profileResponse
					.setMaritalStatus(profile.getMaritalStatus() != null ? profile.getMaritalStatus().name() : null);
			profileResponse.setReligion(profile.getReligion());
			profileResponse.setCaste(profile.getCaste());
			profileResponse.setSubCaste(profile.getSubCaste());
			profileResponse.setMotherTongue(profile.getMotherTongue());
			profileResponse.setEducation(profile.getEducation());
			profileResponse.setOccupation(profile.getOccupation());
			profileResponse.setAnnualIncome(profile.getAnnualIncome());
			profileResponse.setAboutMe(profile.getAboutMe());
			profileResponse.setFamilyType(profile.getFamilyType());
			profileResponse.setFamilyValue(profile.getFamilyValue());
			profileResponse.setCity(profile.getCity());
			profileResponse.setState(profile.getState());
			profileResponse.setCountry(profile.getCountry());
			profileResponse.setPincode(profile.getPincode());
			profileResponse.setProfileCreatedBy(profile.getProfileCreatedBy());
			profileResponse.setCreatedAt(profile.getCreatedAt());
			profileResponse.setUpdatedAt(profile.getUpdatedAt());

			response.setProfile(profileResponse);
		}

        if(user.getPreferences() != null) {
            response.setPreference(mapToPreferenceResponse(user.getPreferences()));
        }
        return response;
	}

    private PreferenceResponse mapToPreferenceResponse(UserPreference preference) {
        if (preference == null) {
            return null;
        }

        PreferenceResponse response = new PreferenceResponse();

        response.setMinAge(preference.getMinAge());
        response.setMaxAge(preference.getMaxAge());

        response.setMinHeight(preference.getMinHeight());
        response.setMaxHeight(preference.getMaxHeight());

        response.setMaritalStatuses(preference.getMaritalStatuses());
        response.setReligions(preference.getReligions());
        response.setCastes(preference.getCastes());
        response.setEducations(preference.getEducation());
        response.setOccupations(preference.getOccupation());

        response.setMinIncome(preference.getMinIncome());
        response.setMaxIncome(preference.getMaxIncome());

        response.setCities(preference.getCities());
        response.setStates(preference.getStates());
        response.setCountries(preference.getCountries());

        // ✅ Newly added fields
        response.setGender(preference.getGender());
        response.setSubCastes(preference.getSubCastes());
        response.setMotherTongue(preference.getMotherTongue());
        response.setFamilyTypes(preference.getFamilyTypes());
        response.setDiet(preference.getDiet());

        return response;
    }
}
