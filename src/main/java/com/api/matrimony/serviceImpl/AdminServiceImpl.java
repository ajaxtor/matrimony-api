package com.api.matrimony.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.SubscriptionPlan;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserReport;
import com.api.matrimony.entity.UserSubscription;
import com.api.matrimony.enums.ReportStatus;
import com.api.matrimony.enums.SubscriptionStatus;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.MatchRepository;
import com.api.matrimony.repository.MessageRepository;
import com.api.matrimony.repository.SubscriptionPlanRepository;
import com.api.matrimony.repository.UserReportRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.repository.UserSubscriptionRepository;
import com.api.matrimony.request.CreateSubscriptionPlanRequest;
import com.api.matrimony.request.UpdateSubscriptionPlanRequest;
import com.api.matrimony.response.AdminDashboardStats;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.UserReportResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.response.UserSubscriptionResponse;
import com.api.matrimony.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

	private final UserRepository userRepository;
	private final UserReportRepository reportRepository;
	private final UserSubscriptionRepository subscriptionRepository;
	private final SubscriptionPlanRepository planRepository;
	private final MatchRepository matchRepository;
	private final MessageRepository messageRepository;

	@Override
	public PagedResponse<UserResponse> getAllUsers(String status, Pageable pageable) {
		log.info("Getting all users with status: {}", status);

		Page<User> userPage;
		if ("ACTIVE".equalsIgnoreCase(status)) {
			userPage = userRepository.findAll(pageable).map(user -> user.getIsActive() ? user : null);
		} else if ("INACTIVE".equalsIgnoreCase(status)) {
			userPage = userRepository.findAll(pageable).map(user -> !user.getIsActive() ? user : null);
		} else {
			userPage = userRepository.findAll(pageable);
		}

		List<UserResponse> userResponses = userPage.getContent().stream().filter(user -> user != null)
				.map(this::mapToUserResponse).collect(Collectors.toList());

		return PagedResponse.<UserResponse>builder().content(userResponses).page(pageable.getPageNumber())
				.size(pageable.getPageSize()).totalElements(userPage.getTotalElements())
				.totalPages(userPage.getTotalPages()).first(userPage.isFirst()).last(userPage.isLast())
				.empty(userPage.isEmpty()).build();
	}

	@Override
	public PagedResponse<UserReportResponse> getAllReports(String status, Pageable pageable) {
		log.info("Getting all reports with status: {}", status);

		Page<UserReport> reportPage;
		if (status != null && !status.isEmpty()) {
			ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
			reportPage = reportRepository.findByStatusOrderByCreatedAtDesc(reportStatus, pageable);
		} else {
			reportPage = reportRepository.findAll(pageable);
		}

		List<UserReportResponse> reportResponses = reportPage.getContent().stream().map(this::mapToReportResponse)
				.collect(Collectors.toList());

		return PagedResponse.<UserReportResponse>builder().content(reportResponses).page(pageable.getPageNumber())
				.size(pageable.getPageSize()).totalElements(reportPage.getTotalElements())
				.totalPages(reportPage.getTotalPages()).first(reportPage.isFirst()).last(reportPage.isLast())
				.empty(reportPage.isEmpty()).build();
	}

	@Override
	public void updateReportStatus(Long reportId, String status, String adminNotes) {
		log.info("Updating report status: reportId={}, status={}", reportId, status);

		Optional<UserReport> report = reportRepository.findById(reportId);

		if (!report.isEmpty()) {
			UserReport reportData = report.get();
			reportData.setStatus(ReportStatus.valueOf(status.toUpperCase()));
			reportData.setAdminNotes(adminNotes);
			reportData.setUpdatedAt(LocalDateTime.now());
			reportRepository.save(reportData);
		} else {
			throw new ApplicationException(ErrorEnum.BLANK_OR_EMPTY_LIST.toString(),
					ErrorEnum.BLANK_OR_EMPTY_LIST.getExceptionError(), HttpStatus.OK);
		}

		log.info("Report status updated successfully: {}", reportId);
	}

	@Override
	public void deactivateUser(Long userId, String reason) {
		log.info("Deactivating user: {}, reason: {}", userId, reason);

		Optional<User> user = userRepository.findById(userId);
		if (!user.isEmpty()) {
			User userData = user.get();
			userData.setIsActive(false);
			userRepository.save(userData);
		} else {
			throw new ApplicationException(ErrorEnum.BLANK_OR_EMPTY_LIST.toString(),
					ErrorEnum.BLANK_OR_EMPTY_LIST.getExceptionError(), HttpStatus.OK);
		}

		log.info("User deactivated successfully: {}", userId);
	}

	@Override
	public void activateUser(Long userId) {
		log.info("Activating user: {}", userId);
		Optional<User> user = userRepository.findById(userId);
		if (!user.isEmpty()) {
			User userData = user.get();
			userData.setIsActive(true);
			userRepository.save(userData);
		} else {
			throw new ApplicationException(ErrorEnum.BLANK_OR_EMPTY_LIST.toString(),
					ErrorEnum.BLANK_OR_EMPTY_LIST.getExceptionError(), HttpStatus.OK);
		}
		log.info("User activated successfully: {}", userId);
	}

	@Override
	public AdminDashboardStats getDashboardStats() {
		log.info("Getting admin dashboard statistics");

		// User statistics
		long totalUsers = userRepository.count();
		long activeUsers = userRepository.findByIsActiveTrue().size();
		long newUsersToday = userRepository.countNewUsersFromDate(LocalDateTime.now().minusDays(1));

		// Match statistics
		long totalMatches = matchRepository.count();
		long mutualMatches = matchRepository.findAll().stream()
				.mapToLong(m -> "MUTUAL".equals(m.getStatus().name()) ? 1 : 0).sum();

		// Report statistics
		long totalReports = reportRepository.count();
		long pendingReports = reportRepository.findAll().stream()
				.mapToLong(r -> r.getStatus() == ReportStatus.PENDING ? 1 : 0).sum();

		// Subscription statistics
		long totalSubscriptions = subscriptionRepository.count();
		long activeSubscriptions = subscriptionRepository.findAll().stream()
				.mapToLong(s -> s.getStatus() == SubscriptionStatus.ACTIVE ? 1 : 0).sum();

		return AdminDashboardStats.builder().totalUsers(totalUsers).activeUsers(activeUsers)
				.newUsersToday(newUsersToday).totalMatches(totalMatches).mutualMatches(mutualMatches)
				.totalReports(totalReports).pendingReports(pendingReports).totalSubscriptions(totalSubscriptions)
				.activeSubscriptions(activeSubscriptions).build();
	}

	@Override
	public List<UserResponse> getPendingVerifications() {
		log.info("Getting pending user verifications");

		List<User> pendingUsers = userRepository.findByIsVerifiedFalse();
		return pendingUsers.stream().map(this::mapToUserResponse).collect(Collectors.toList());
	}

	@Override
	public void approveUserVerification(Long userId) {
		log.info("Approving user verification: {}", userId);
		Optional<User> user = userRepository.findById(userId);
		if (!user.isEmpty()) {
			User userData = user.get();
			userData.setIsVerified(true);
			userData.setIsActive(true);
			userRepository.save(userData);
		} else {
			throw new ApplicationException(ErrorEnum.BLANK_OR_EMPTY_LIST.toString(),
					ErrorEnum.BLANK_OR_EMPTY_LIST.getExceptionError(), HttpStatus.OK);
		}

		log.info("User verification approved: {}", userId);
	}

	@Override
	public void rejectUserVerification(Long userId, String reason) {
		log.info("Rejecting user verification: {}, reason: {}", userId, reason);

		Optional<User> user = userRepository.findById(userId);
		if (!user.isEmpty()) {
			User userData = user.get();
			userData.setIsVerified(false);
			userData.setIsActive(false);
			userRepository.save(userData);
		} else {
			throw new ApplicationException(ErrorEnum.INVALID_USER.toString(),
					ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK);
		}

		log.info("User verification rejected: {}", userId);
	}

	@Override
	public PagedResponse<UserSubscriptionResponse> getAllSubscriptions(Pageable pageable) {
		log.info("Getting all subscriptions");

		Page<UserSubscription> subscriptionPage = subscriptionRepository.findAll(pageable);

		List<UserSubscriptionResponse> subscriptionResponses = subscriptionPage.getContent().stream()
				.map(this::mapToSubscriptionResponse).collect(Collectors.toList());

		return PagedResponse.<UserSubscriptionResponse>builder().content(subscriptionResponses)
				.page(pageable.getPageNumber()).size(pageable.getPageSize())
				.totalElements(subscriptionPage.getTotalElements()).totalPages(subscriptionPage.getTotalPages())
				.first(subscriptionPage.isFirst()).last(subscriptionPage.isLast()).empty(subscriptionPage.isEmpty())
				.build();
	}

	@Override
	public void createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
		log.info("Creating subscription plan: {}", request.getName());

		SubscriptionPlan plan = new SubscriptionPlan();
		plan.setName(request.getName());
		plan.setDescription(request.getDescription());
		plan.setPrice(request.getPrice());
		plan.setDurationMonths(request.getDurationMonths());
		plan.setFeatures(request.getFeatures()); // JSON string
		plan.setIsActive(true);

		planRepository.save(plan);
		log.info("Subscription plan created successfully: {}", plan.getId());
	}

	@Override
	public void updateSubscriptionPlan(Long planId, UpdateSubscriptionPlanRequest request) {
		log.info("Updating subscription plan: {}", planId);

		SubscriptionPlan plan = planRepository.findById(planId)
				.orElseThrow(() -> new ApplicationException(ErrorEnum.INVALID_USER.toString(),
						ErrorEnum.INVALID_USER.getExceptionError(), HttpStatus.OK));

		if (request.getName() != null) {
			plan.setName(request.getName());
		}
		if (request.getDescription() != null) {
			plan.setDescription(request.getDescription());
		}
		if (request.getPrice() != null) {
			plan.setPrice(request.getPrice());
		}
		if (request.getDurationMonths() != null) {
			plan.setDurationMonths(request.getDurationMonths());
		}
		if (request.getFeatures() != null) {
			plan.setFeatures(request.getFeatures());
		}
		if (request.getIsActive() != null) {
			plan.setIsActive(request.getIsActive());
		}

		planRepository.save(plan);
		log.info("Subscription plan updated successfully: {}", planId);
	}

	// Helper methods
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
		return response;
	}

	private UserReportResponse mapToReportResponse(UserReport report) {
		UserReportResponse response = new UserReportResponse();
		response.setId(report.getId());
		response.setReporterId(report.getReporter().getId());
		response.setReportedUserId(report.getReportedUser().getId());
		response.setReason(report.getReason().name());
		response.setDescription(report.getDescription());
		response.setStatus(report.getStatus().name());
		response.setAdminNotes(report.getAdminNotes());
		response.setCreatedAt(report.getCreatedAt());
		response.setUpdatedAt(report.getUpdatedAt());
		return response;
	}

	private UserSubscriptionResponse mapToSubscriptionResponse(UserSubscription subscription) {
		UserSubscriptionResponse response = new UserSubscriptionResponse();
		response.setId(subscription.getId());
		response.setUserId(subscription.getUser().getId());
		response.setPlanId(subscription.getPlan().getId());
		response.setPlanName(subscription.getPlan().getName());
		response.setStartDate(subscription.getStartDate());
		response.setEndDate(subscription.getEndDate());
		response.setStatus(subscription.getStatus().name());
		response.setAmountPaid(subscription.getAmountPaid());
		response.setPaymentId(subscription.getPaymentId());
		response.setCreatedAt(subscription.getCreatedAt());
		return response;
	}
}
