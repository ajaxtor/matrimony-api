package com.api.matrimony.service;

import com.api.matrimony.entity.OtpVerification;

/**
 * Notification Service Interface
 */

public interface NotificationService {
    void sendPushNotification(Long userId, String title, String message, String type);
    void sendEmailNotification(OtpVerification otpVerification);
    void sendSmsNotification(String phone, String message);
    void notifyNewMatch(Long userId, Long matchedUserId);
    void notifyMutualMatch(Long userId1, Long userId2);
    void notifyNewMessage(Long receiverId, Long senderId);
    void notifySubscriptionExpiry(Long userId);
    void sendWelcomeNotification(Long userId);
    void sendMatchRecommendations(Long userId);
}

