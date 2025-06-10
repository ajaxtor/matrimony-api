package com.api.matrimony.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.api.matrimony.request.CreateSubscriptionPlanRequest;
import com.api.matrimony.request.UpdateSubscriptionPlanRequest;
import com.api.matrimony.response.AdminDashboardStats;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.UserReportResponse;
import com.api.matrimony.response.UserResponse;
import com.api.matrimony.response.UserSubscriptionResponse;

/**
 * Admin Service Interface
 */

public interface AdminService {
    PagedResponse<UserResponse> getAllUsers(String status, Pageable pageable);
    PagedResponse<UserReportResponse> getAllReports(String status, Pageable pageable);
    void updateReportStatus(Long reportId, String status, String adminNotes);
    void deactivateUser(Long userId, String reason);
    void activateUser(Long userId);
    AdminDashboardStats getDashboardStats();
    List<UserResponse> getPendingVerifications();
    void approveUserVerification(Long userId);
    void rejectUserVerification(Long userId, String reason);
    PagedResponse<UserSubscriptionResponse> getAllSubscriptions(Pageable pageable);
    void createSubscriptionPlan(CreateSubscriptionPlanRequest request);
    void updateSubscriptionPlan(Long planId, UpdateSubscriptionPlanRequest request);
}


