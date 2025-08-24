package com.api.matrimony.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.api.matrimony.response.APIResonse;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SubscriptionController {

	@Autowired
    private SubscriptionService subscriptionService;

    /**
     * Get all active subscription plans
     */
    @GetMapping("/getPlans")
    public ResponseEntity<APIResonse<List<SubscriptionPlanResponse>>> getSubscriptionPlans() {
        log.info("Getting all subscription plans");
        APIResonse< List<SubscriptionPlanResponse>> response = new APIResonse<>();
            List<SubscriptionPlanResponse> plans = subscriptionService.getAllActivePlans();
            response.setData(plans);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get subscription plan by ID
     */
    @GetMapping("/getPlanById/{planId}")
    public ResponseEntity<APIResonse<SubscriptionPlanResponse>> getSubscriptionPlan(
            @PathVariable Long planId) {
        
        log.info("Getting subscription plan: {}", planId);
        APIResonse<SubscriptionPlanResponse> response = new APIResonse<>();
            SubscriptionPlanResponse plan = subscriptionService.getPlanById(planId);
            response.setData(plan);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get current user subscription
     */
    @GetMapping("/mySubscription")
    public ResponseEntity<APIResonse<UserSubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting current subscription for user: {}", currentUser.getId());
        APIResonse<UserSubscriptionResponse> response = new APIResonse<>();
            UserSubscriptionResponse subscription = subscriptionService.getCurrentSubscription(currentUser.getId());
            response.setData(subscription);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Subscribe to a plan
     */
    @PostMapping("/subscribe")
    public ResponseEntity<APIResonse<UserSubscriptionResponse>> subscribeToPlan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubscribeRequest request) {
        
        log.info("Subscribing user: {} to plan: {}", currentUser.getId(), request.getPlanId());
        APIResonse<UserSubscriptionResponse> response = new APIResonse<>();
            UserSubscriptionResponse subscription = subscriptionService.subscribeToPlan(
                    currentUser.getId(), request.getPlanId(), request.getPaymentId());
            response.setData(subscription);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cancel subscription
     */
    @PostMapping("/cancel")
    public ResponseEntity<APIResonse<String>> cancelSubscription(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Cancelling subscription for user: {}", currentUser.getId());
        APIResonse<String> response = new APIResonse<>();
            subscriptionService.cancelSubscription(currentUser.getId());
            response.setData("Cancel subscription");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get subscription history
     */
    @GetMapping("/history")
    public ResponseEntity<APIResonse<List<UserSubscriptionResponse>>> getSubscriptionHistory(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting subscription history for user: {}", currentUser.getId());
        APIResonse<List<UserSubscriptionResponse>> response = new APIResonse<>();
            List<UserSubscriptionResponse> history = subscriptionService.getSubscriptionHistory(currentUser.getId());
            response.setData(history);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Check if user has active subscription
     */
    @GetMapping("/mySubscriptionStatus")
    public ResponseEntity<APIResonse<Map<String,Boolean>>> hasActiveSubscription(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Checking subscription status for user: {}", currentUser.getId());
        APIResonse<Map<String,Boolean>> response = new APIResonse<>();
            boolean hasActive = subscriptionService.hasActiveSubscription(currentUser.getId());
            Map<String,Boolean> resMap = new HashMap<>();
            resMap.put("hasActive", hasActive);
            response.setData(resMap);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Check feature access
     */
    @GetMapping("/features/{feature}")
    public ResponseEntity<APIResonse<Boolean>> checkFeatureAccess(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String feature) {
        
        log.info("Checking feature access for user: {}, feature: {}", currentUser.getId(), feature);
        APIResonse<Boolean> response = new APIResonse<>();
            boolean hasAccess = subscriptionService.hasFeatureAccess(currentUser.getId(), feature);
            response.setData(hasAccess);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

