package com.api.matrimony.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.api.matrimony.request.UpdateReportRequest;
import com.api.matrimony.request.UpdateSubscriptionPlanRequest;
import com.api.matrimony.response.AdminDashboardStats;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.UserReportResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.response.UserSubscriptionResponse;
import com.api.matrimony.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin Controller for administrative operations
 */

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	@Autowired
    private  AdminService adminService;

    /**
     * Get dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStats>> getDashboardStats() {
        log.info("Getting admin dashboard statistics");
        
        try {
            AdminDashboardStats stats = adminService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting dashboard statistics", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        log.info("Getting all users - page: {}, size: {}, status: {}", page, size, status);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponse<UserResponse> users = adminService.getAllUsers(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting all users", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Activate user
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long userId) {
        log.info("Activating user: {}", userId);
        
        try {
            adminService.activateUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Success", "User activated successfully"));
        } catch (Exception e) {
            log.error("Error activating user: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Deactivate user
     */
    @PutMapping("/users/{userId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        
        log.info("Deactivating user: {}, reason: {}", userId, reason);
        
        try {
            adminService.deactivateUser(userId, reason);
            return ResponseEntity.ok(ApiResponse.success("Success", "User deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating user: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all reports
     */
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<PagedResponse<UserReportResponse>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        log.info("Getting all reports - page: {}, size: {}, status: {}", page, size, status);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponse<UserReportResponse> reports = adminService.getAllReports(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(reports, "Reports retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting all reports", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update report status
     */
    @PutMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<String>> updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportRequest request) {
        
        log.info("Updating report status - reportId: {}, status: {}", reportId, request.getStatus());
        
        try {
            adminService.updateReportStatus(reportId, request.getStatus(), request.getAdminNotes());
            return ResponseEntity.ok(ApiResponse.success("Success", "Report status updated successfully"));
        } catch (Exception e) {
            log.error("Error updating report status - reportId: {}", reportId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get pending verifications
     */
    @GetMapping("/verifications/pending")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingVerifications() {
        log.info("Getting pending user verifications");
        
        try {
            List<UserResponse> pendingUsers = adminService.getPendingVerifications();
            return ResponseEntity.ok(ApiResponse.success(pendingUsers, "Pending verifications retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting pending verifications", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Approve user verification
     */
    @PostMapping("/verifications/{userId}/approve")
    public ResponseEntity<ApiResponse<String>> approveUserVerification(@PathVariable Long userId) {
        log.info("Approving user verification: {}", userId);
        
        try {
            adminService.approveUserVerification(userId);
            return ResponseEntity.ok(ApiResponse.success("Success", "User verification approved"));
        } catch (Exception e) {
            log.error("Error approving user verification: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Reject user verification
     */
    @PostMapping("/verifications/{userId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectUserVerification(
            @PathVariable Long userId,
            @RequestParam String reason) {
        
        log.info("Rejecting user verification: {}, reason: {}", userId, reason);
        
        try {
            adminService.rejectUserVerification(userId, reason);
            return ResponseEntity.ok(ApiResponse.success("Success", "User verification rejected"));
        } catch (Exception e) {
            log.error("Error rejecting user verification: {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all subscriptions
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<PagedResponse<UserSubscriptionResponse>>> getAllSubscriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting all subscriptions - page: {}, size: {}", page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponse<UserSubscriptionResponse> subscriptions = adminService.getAllSubscriptions(pageable);
            return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting all subscriptions", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Create subscription plan
     */
    @PostMapping("/subscription-plans")
    public ResponseEntity<ApiResponse<String>> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request) {
        
        log.info("Creating subscription plan: {}", request.getName());
        
        try {
            adminService.createSubscriptionPlan(request);
            return ResponseEntity.ok(ApiResponse.success("Success", "Subscription plan created successfully"));
        } catch (Exception e) {
            log.error("Error creating subscription plan: {}", request.getName(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update subscription plan
     */
    @PutMapping("/subscription-plans/{planId}")
    public ResponseEntity<ApiResponse<String>> updateSubscriptionPlan(
            @PathVariable Long planId,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        
        log.info("Updating subscription plan: {}", planId);
        
        try {
            adminService.updateSubscriptionPlan(planId, request);
            return ResponseEntity.ok(ApiResponse.success("Success", "Subscription plan updated successfully"));
        } catch (Exception e) {
            log.error("Error updating subscription plan: {}", planId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
