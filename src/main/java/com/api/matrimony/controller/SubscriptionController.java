package com.api.matrimony.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.entity.User;
import com.api.matrimony.request.SubscribeRequest;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.SubscriptionPlanResponse;
import com.api.matrimony.response.UserSubscriptionResponse;
import com.api.matrimony.service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Subscription Controller for subscription management
 */
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SubscriptionController {

	@Autowired
    private SubscriptionService subscriptionService;

    /**
     * Get all active subscription plans
     */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getSubscriptionPlans() {
        log.info("Getting all subscription plans");
        
        try {
            List<SubscriptionPlanResponse> plans = subscriptionService.getAllActivePlans();
            return ResponseEntity.ok(ApiResponse.success(plans, "Subscription plans retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting subscription plans", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get subscription plan by ID
     */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getSubscriptionPlan(
            @PathVariable Long planId) {
        
        log.info("Getting subscription plan: {}", planId);
        
        try {
            SubscriptionPlanResponse plan = subscriptionService.getPlanById(planId);
            return ResponseEntity.ok(ApiResponse.success(plan, "Subscription plan retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting subscription plan: {}", planId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get current user subscription
     */
    @GetMapping("/my-subscription")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting current subscription for user: {}", currentUser.getId());
        
        try {
            UserSubscriptionResponse subscription = subscriptionService.getCurrentSubscription(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(subscription, "Current subscription retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting current subscription for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Subscribe to a plan
     */
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> subscribeToPlan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubscribeRequest request) {
        
        log.info("Subscribing user: {} to plan: {}", currentUser.getId(), request.getPlanId());
        
        try {
            UserSubscriptionResponse subscription = subscriptionService.subscribeToPlan(
                    currentUser.getId(), request.getPlanId(), request.getPaymentId());
            return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription created successfully"));
        } catch (Exception e) {
            log.error("Error subscribing user: {} to plan: {}", currentUser.getId(), request.getPlanId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cancel subscription
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancelSubscription(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Cancelling subscription for user: {}", currentUser.getId());
        
        try {
            subscriptionService.cancelSubscription(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Success", "Subscription cancelled successfully"));
        } catch (Exception e) {
            log.error("Error cancelling subscription for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get subscription history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getSubscriptionHistory(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting subscription history for user: {}", currentUser.getId());
        
        try {
            List<UserSubscriptionResponse> history = subscriptionService.getSubscriptionHistory(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(history, "Subscription history retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting subscription history for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if user has active subscription
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveSubscription(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Checking subscription status for user: {}", currentUser.getId());
        
        try {
            boolean hasActive = subscriptionService.hasActiveSubscription(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(hasActive, "Subscription status checked"));
        } catch (Exception e) {
            log.error("Error checking subscription status for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check feature access
     */
    @GetMapping("/features/{feature}")
    public ResponseEntity<ApiResponse<Boolean>> checkFeatureAccess(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String feature) {
        
        log.info("Checking feature access for user: {}, feature: {}", currentUser.getId(), feature);
        
        try {
            boolean hasAccess = subscriptionService.hasFeatureAccess(currentUser.getId(), feature);
            return ResponseEntity.ok(ApiResponse.success(hasAccess, "Feature access checked"));
        } catch (Exception e) {
            log.error("Error checking feature access for user: {}, feature: {}", currentUser.getId(), feature, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

