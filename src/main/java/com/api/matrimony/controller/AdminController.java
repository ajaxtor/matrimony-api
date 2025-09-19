package com.api.matrimony.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.request.CreateSubscriptionPlanRequest;
import com.api.matrimony.request.LoginRequest;
import com.api.matrimony.request.UpdateReportRequest;
import com.api.matrimony.request.UpdateSubscriptionPlanRequest;
import com.api.matrimony.response.APIResonse;
import com.api.matrimony.response.AdminDashboardStats;
import com.api.matrimony.response.AdminResponse;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.UserReportResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.response.UserSubscriptionResponse;
import com.api.matrimony.service.AdminService;
import com.api.matrimony.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin Controller for administrative operations
 */

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	@Autowired
	private AdminService adminService;
	@Autowired
	private AuthService authService;

	/**
	 * Get dashboard statistics
	 */
	@GetMapping("/dashboard/stats")
	public ResponseEntity<APIResonse<AdminDashboardStats>> getDashboardStats() {
		APIResonse<AdminDashboardStats> response = new APIResonse<>();
		log.info("Getting admin dashboard statistics");
		AdminDashboardStats stats = adminService.getDashboardStats();
		response.setData(stats);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * Get all users
	 */
	@GetMapping("/users")
	public ResponseEntity<APIResonse<PagedResponse<UserResponse>>> getAllUsers(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String status) {
		APIResonse<PagedResponse<UserResponse>> response = new APIResonse<>();
		log.info("Getting all users - page: {}, size: {}, status: {}", page, size, status);
		Pageable pageable = PageRequest.of(page, size);
		PagedResponse<UserResponse> users = adminService.getAllUsers(status, pageable);
		response.setData(users);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * Activate user
	 */
	@PutMapping("/users/{userId}/activate")
	public ResponseEntity<APIResonse<String>> activateUser(@PathVariable Long userId) {
		log.info("Activating user: {}", userId);
		APIResonse<String> response = new APIResonse<>();
		adminService.activateUser(userId);
		response.setData("Activated user");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Deactivate user
	 */
	@PutMapping("/users/{userId}/deactivate")
	public ResponseEntity<APIResonse<String>> deactivateUser(@PathVariable Long userId,
			@RequestParam(required = false) String reason) {
		APIResonse<String> response = new APIResonse<>();
		log.info("Deactivating user: {}, reason: {}", userId, reason);
		adminService.deactivateUser(userId, reason);
		response.setData("Deactivated user");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Get all reports
	 */
	@GetMapping("/reports")
	public ResponseEntity<APIResonse<PagedResponse<UserReportResponse>>> getAllReports(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(required = false) String status) {
		APIResonse<PagedResponse<UserReportResponse>> response = new APIResonse<>();
		log.info("Getting all reports - page: {}, size: {}, status: {}", page, size, status);
		Pageable pageable = PageRequest.of(page, size);
		PagedResponse<UserReportResponse> reports = adminService.getAllReports(status, pageable);
		response.setData(reports);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	/**
	 * Update report status
	 */
	@PutMapping("/reports/{reportId}")
	public ResponseEntity<APIResonse<String>> updateReportStatus(@PathVariable Long reportId,
			@Valid @RequestBody UpdateReportRequest request) {
		log.info("Updating report status - reportId: {}, status: {}", reportId, request.getStatus());
		APIResonse<String> response = new APIResonse<>();
		adminService.updateReportStatus(reportId, request.getStatus(), request.getAdminNotes());
		response.setData("Update report status");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Get pending verifications
	 */
	@GetMapping("/verifications/pending")
	public ResponseEntity<APIResonse<List<UserResponse>>> getPendingVerifications() {
		log.info("Getting pending user verifications");
		APIResonse<List<UserResponse>> response = new APIResonse<>();
		List<UserResponse> pendingUsers = adminService.getPendingVerifications();
		response.setData(pendingUsers);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Approve user verification
	 */
	@PostMapping("/verifications/{userId}/approve")
	public ResponseEntity<APIResonse<String>> approveUserVerification(@PathVariable Long userId) {
		log.info("Approving user verification: {}", userId);
		APIResonse<String> response = new APIResonse<>();
		adminService.approveUserVerification(userId);
		response.setData("Approve user verification");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Reject user verification
	 */
	@PostMapping("/verifications/{userId}/reject")
	public ResponseEntity<APIResonse<String>> rejectUserVerification(@PathVariable Long userId,
			@RequestParam String reason) {

		log.info("Rejecting user verification: {}, reason: {}", userId, reason);
		APIResonse<String> response = new APIResonse<>();
		adminService.rejectUserVerification(userId, reason);
		response.setData("Reject user verification");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Get all subscriptions
	 */
	@GetMapping("/subscriptions")
	public ResponseEntity<APIResonse<PagedResponse<UserSubscriptionResponse>>> getAllSubscriptions(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		APIResonse<PagedResponse<UserSubscriptionResponse>> response = new APIResonse<>();
		log.info("Getting all subscriptions - page: {}, size: {}", page, size);
		Pageable pageable = PageRequest.of(page, size);
		PagedResponse<UserSubscriptionResponse> subscriptions = adminService.getAllSubscriptions(pageable);
		response.setData(subscriptions);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Create subscription plan
	 */
	@PostMapping("/subscription-plans")
	public ResponseEntity<APIResonse<String>> createSubscriptionPlan(
			@Valid @RequestBody CreateSubscriptionPlanRequest request) {

		log.info("Creating subscription plan: {}", request.getName());
		APIResonse<String> response = new APIResonse<>();
		adminService.createSubscriptionPlan(request);
		response.setData("Create subscription plan");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Update subscription plan
	 */
	@PutMapping("/subscription-plans/{planId}")
	public ResponseEntity<APIResonse<String>> updateSubscriptionPlan(@PathVariable Long planId,
			@Valid @RequestBody UpdateSubscriptionPlanRequest request) {

		log.info("Updating subscription plan: {}", planId);
		APIResonse<String> response = new APIResonse<>();
		response.setData("Create subscription plan");
		adminService.updateSubscriptionPlan(planId, request);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
