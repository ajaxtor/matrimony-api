package com.api.matrimony.serviceImpl;






import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.api.matrimony.entity.User;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
* Notification Service Implementation
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

 private final JavaMailSender mailSender;
 private final UserRepository userRepository;

 @Value("${spring.mail.username}")
 private String fromEmail;

 @Override
 public void sendPushNotification(Long userId, String title, String message, String type) {
     log.info("Sending push notification to user: {}, title: {}", userId, title);
     // Implementation for FCM push notifications would go here
     // For now, we'll just log it
     log.info("Push notification sent: userId={}, title={}, message={}, type={}", 
             userId, title, message, type);
 }

 @Override
 public void sendEmailNotification(String email, String subject, String message) {
     log.info("Sending email notification to: {}, subject: {}", email, subject);
     
     try {
         SimpleMailMessage mailMessage = new SimpleMailMessage();
         mailMessage.setFrom(fromEmail);
         mailMessage.setTo(email);
         mailMessage.setSubject(subject);
         mailMessage.setText(message);
         
         mailSender.send(mailMessage);
         log.info("Email sent successfully to: {}", email);
     } catch (Exception e) {
         log.error("Failed to send email to: {}", email, e);
     }
 }

 @Override
 public void sendSmsNotification(String phone, String message) {
     log.info("Sending SMS notification to: {}", phone);
     // Implementation for SMS service would go here
     // For now, we'll just log it
     log.info("SMS sent: phone={}, message={}", phone, message);
 }

 @Override
 public void notifyNewMatch(Long userId, Long matchedUserId) {
     log.info("Notifying new match: userId={}, matchedUserId={}", userId, matchedUserId);
     
     User user = userRepository.findById(matchedUserId).orElse(null);
     if (user != null && user.getProfile() != null) {
         String title = "New Match!";
         String message = user.getProfile().getFirstName() + " is interested in your profile!";
         sendPushNotification(userId, title, message, "NEW_MATCH");
     }
 }

 @Override
 public void notifyMutualMatch(Long userId1, Long userId2) {
     log.info("Notifying mutual match: userId1={}, userId2={}", userId1, userId2);
     
     String title = "It's a Match!";
     String message = "You have a mutual match! Start chatting now.";
     
     sendPushNotification(userId1, title, message, "MUTUAL_MATCH");
     sendPushNotification(userId2, title, message, "MUTUAL_MATCH");
 }

 @Override
 public void notifyNewMessage(Long receiverId, Long senderId) {
     log.info("Notifying new message: receiverId={}, senderId={}", receiverId, senderId);
     
     User sender = userRepository.findById(senderId).orElse(null);
     if (sender != null && sender.getProfile() != null) {
         String title = "New Message";
         String message = "You have a new message from " + sender.getProfile().getFirstName();
         sendPushNotification(receiverId, title, message, "NEW_MESSAGE");
     }
 }

 @Override
 public void notifySubscriptionExpiry(Long userId) {
     log.info("Notifying subscription expiry for user: {}", userId);
     
     User user = userRepository.findById(userId).orElse(null);
     if (user != null) {
         String title = "Subscription Expiring";
         String message = "Your subscription is about to expire. Renew now to continue enjoying premium features.";
         
         sendPushNotification(userId, title, message, "SUBSCRIPTION_EXPIRY");
         sendEmailNotification(user.getEmail(), title, message);
     }
 }

 @Override
 public void sendWelcomeNotification(Long userId) {
     log.info("Sending welcome notification to user: {}", userId);
     
     User user = userRepository.findById(userId).orElse(null);
     if (user != null) {
         String title = "Welcome to Matrimony App!";
         String message = "Complete your profile to get better matches.";
         
         sendPushNotification(userId, title, message, "WELCOME");
         sendEmailNotification(user.getEmail(), title, 
                 "Welcome to our matrimony platform! Complete your profile to find your perfect match.");
     }
 }

 @Override
 public void sendMatchRecommendations(Long userId) {
     log.info("Sending match recommendations to user: {}", userId);
     
     String title = "New Matches Available";
     String message = "We found some new matches for you! Check them out now.";
     
     sendPushNotification(userId, title, message, "MATCH_RECOMMENDATIONS");
 }
}

