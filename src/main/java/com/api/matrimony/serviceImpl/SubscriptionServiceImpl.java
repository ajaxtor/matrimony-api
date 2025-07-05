package com.api.matrimony.serviceImpl;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.api.matrimony.entity.SubscriptionPlan;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserSubscription;
import com.api.matrimony.enums.SubscriptionStatus;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.SubscriptionPlanRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.repository.UserSubscriptionRepository;
import com.api.matrimony.response.SubscriptionPlanResponse;
import com.api.matrimony.response.SubscriptionStats;
import com.api.matrimony.response.UserSubscriptionResponse;
import com.api.matrimony.service.NotificationService;
import com.api.matrimony.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
* Subscription Service Implementation
*/

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

 private final SubscriptionPlanRepository planRepository;
 private final UserSubscriptionRepository subscriptionRepository;
 private final UserRepository userRepository;
 private final NotificationService notificationService;

 @Override
 public List<SubscriptionPlanResponse> getAllActivePlans() {
     log.info("Getting all active subscription plans");
     
     List<SubscriptionPlan> plans = planRepository.findByIsActiveTrueOrderByPriceAsc();
     return plans.stream()
             .map(this::mapToPlanResponse)
             .collect(Collectors.toList());
 }

 @Override
 public SubscriptionPlanResponse getPlanById(Long planId) {
     log.info("Getting subscription plan by ID: {}", planId);
     
     SubscriptionPlan plan = planRepository.findById(planId)
             .orElseThrow(() -> new ApplicationException(ErrorEnum.SUB_NOT_FOUND.toString(),
						ErrorEnum.SUB_NOT_FOUND.getExceptionError(), HttpStatus.OK));
     
     return mapToPlanResponse(plan);
 }

 @Override
 public UserSubscriptionResponse getCurrentSubscription(Long userId) {
     log.info("Getting current subscription for user: {}", userId);
     
     return subscriptionRepository.findActiveSubscriptionByUserId(userId)
             .map(this::mapToSubscriptionResponse)
             .orElse(null);
 }

 @Override
 public UserSubscriptionResponse subscribeToPlan(Long userId, Long planId, String paymentId) {
     log.info("Subscribing user: {} to plan: {}, paymentId: {}", userId, planId, paymentId);
     
     User user = userRepository.findById(userId)
             .orElseThrow(() ->new ApplicationException(ErrorEnum.USER_NOT_FOUND.toString(),
						ErrorEnum.USER_NOT_FOUND.getExceptionError(), HttpStatus.OK));
     
     SubscriptionPlan plan = planRepository.findById(planId)
             .orElseThrow(() -> new ApplicationException(ErrorEnum.SUB_NOT_FOUND.toString(),
						ErrorEnum.SUB_NOT_FOUND.getExceptionError(), HttpStatus.OK));

     if (!plan.getIsActive()) {
         throw new ApplicationException(ErrorEnum.SUB_NOT_ACTIVE.toString(),
					ErrorEnum.SUB_NOT_ACTIVE.getExceptionError(), HttpStatus.OK);
     }

     // Check if user already has an active subscription
     if (hasActiveSubscription(userId)) {
    	 throw new ApplicationException(ErrorEnum.USER_IN_ACTIVE_SUB.toString(),
					ErrorEnum.USER_IN_ACTIVE_SUB.getExceptionError(), HttpStatus.OK);
     }

     // Create new subscription
     UserSubscription subscription = new UserSubscription();
     subscription.setUser(user);
     subscription.setPlan(plan);
     subscription.setStartDate(LocalDate.now());
     subscription.setEndDate(LocalDate.now().plusMonths(plan.getDurationMonths()));
     subscription.setStatus(SubscriptionStatus.ACTIVE);
     subscription.setPaymentId(paymentId);
     subscription.setAmountPaid(plan.getPrice());

     UserSubscription savedSubscription = subscriptionRepository.save(subscription);
     log.info("Subscription created successfully for user: {}", userId);

     return mapToSubscriptionResponse(savedSubscription);
 }

 @Override
 public boolean hasActiveSubscription(Long userId) {
     return subscriptionRepository.findActiveSubscriptionByUserId(userId).isPresent();
 }

 @Override
 public boolean hasFeatureAccess(Long userId, String feature) {
     log.debug("Checking feature access for user: {}, feature: {}", userId, feature);
     
     UserSubscription activeSubscription = subscriptionRepository.findActiveSubscriptionByUserId(userId)
             .orElse(null);

     if (activeSubscription == null) {
         return false; // No active subscription
     }

     // Parse features from JSON and check access
     // This is simplified - in real implementation, you'd parse the JSON features
     String features = activeSubscription.getPlan().getFeatures();
     return features != null && features.contains(feature);
 }

 @Override
 public void cancelSubscription(Long userId) {
     log.info("Cancelling subscription for user: {}", userId);
     
     UserSubscription activeSubscription = subscriptionRepository.findActiveSubscriptionByUserId(userId)
             .orElseThrow(() -> new ApplicationException(ErrorEnum.NO_ACTIVE_SUB.toString(),
 					ErrorEnum.NO_ACTIVE_SUB.getExceptionError(), HttpStatus.OK));

     activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
     subscriptionRepository.save(activeSubscription);

     log.info("Subscription cancelled successfully for user: {}", userId);
 }

 @Override
 @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM daily
 public void processExpiredSubscriptions() {
     log.info("Processing expired subscriptions");
     
     List<UserSubscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions();
     
     for (UserSubscription subscription : expiredSubscriptions) {
         subscription.setStatus(SubscriptionStatus.EXPIRED);
         subscriptionRepository.save(subscription);
         
         // Send expiry notification
         notificationService.notifySubscriptionExpiry(subscription.getUser().getId());
         
         log.info("Marked subscription as expired for user: {}", subscription.getUser().getId());
     }
     
     log.info("Processed {} expired subscriptions", expiredSubscriptions.size());
 }

 @Override
 public List<UserSubscriptionResponse> getSubscriptionHistory(Long userId) {
     log.info("Getting subscription history for user: {}", userId);
     
     List<UserSubscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
     return subscriptions.stream()
             .map(this::mapToSubscriptionResponse)
             .collect(Collectors.toList());
 }

 @Override
 public SubscriptionStats getSubscriptionStats() {
     log.info("Getting subscription statistics");
     
     long totalSubscriptions = subscriptionRepository.count();
     long activeSubscriptions = subscriptionRepository.findAll().stream()
             .mapToLong(s -> s.getStatus() == SubscriptionStatus.ACTIVE ? 1 : 0)
             .sum();
     
     // Calculate revenue (simplified)
     BigDecimal totalRevenue = subscriptionRepository.findAll().stream()
             .filter(s -> s.getAmountPaid() != null)
             .map(UserSubscription::getAmountPaid)
             .reduce(BigDecimal.ZERO, BigDecimal::add);

     return SubscriptionStats.builder()
             .totalSubscriptions(totalSubscriptions)
             .activeSubscriptions(activeSubscriptions)
             .totalRevenue(totalRevenue)
             .conversionRate(totalSubscriptions > 0 ? (activeSubscriptions * 100.0 / totalSubscriptions) : 0.0)
             .build();
 }

 // Helper methods
 private SubscriptionPlanResponse mapToPlanResponse(SubscriptionPlan plan) {
     SubscriptionPlanResponse response = new SubscriptionPlanResponse();
     response.setId(plan.getId());
     response.setName(plan.getName());
     response.setDescription(plan.getDescription());
     response.setPrice(plan.getPrice());
     response.setDurationMonths(plan.getDurationMonths());
     response.setFeatures(plan.getFeatures()); // JSON object
     response.setIsActive(plan.getIsActive());
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

