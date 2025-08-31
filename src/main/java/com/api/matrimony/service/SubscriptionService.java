package com.api.matrimony.service;

import java.util.List;

import com.api.matrimony.request.SubscribeRequest;
import com.api.matrimony.response.RazorpayOrderResponse;
import com.api.matrimony.response.SubscriptionPlanResponse;
import com.api.matrimony.response.SubscriptionStats;
import com.api.matrimony.response.UserSubscriptionResponse;

/**
 * Subscription Service Interface
 */

public interface SubscriptionService {
    List<SubscriptionPlanResponse> getAllActivePlans();
    SubscriptionPlanResponse getPlanById(Long planId);
    UserSubscriptionResponse getCurrentSubscription(Long userId);
    UserSubscriptionResponse subscribeToPlan(Long userId, SubscribeRequest request);
    boolean hasActiveSubscription(Long userId);
    boolean hasFeatureAccess(Long userId, String feature);
    void cancelSubscription(Long userId);
    void processExpiredSubscriptions();
    List<UserSubscriptionResponse> getSubscriptionHistory(Long userId);
    SubscriptionStats getSubscriptionStats();
    RazorpayOrderResponse createOrderByPlanId(Long id, Long planId);
}
