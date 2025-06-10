package com.api.matrimony.serviceImpl;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.api.matrimony.entity.BlockedUser;
import com.api.matrimony.entity.Conversation;
import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.Message;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.enums.MessageType;
import com.api.matrimony.exception.CustomException;
import com.api.matrimony.exception.ResourceNotFoundException;
import com.api.matrimony.repository.BlockedUserRepository;
import com.api.matrimony.repository.ConversationRepository;
import com.api.matrimony.repository.MatchRepository;
import com.api.matrimony.repository.MessageRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.MessageRequest;
import com.api.matrimony.response.ConversationResponse;
import com.api.matrimony.response.MessageResponse;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.ChatService;
import com.api.matrimony.service.NotificationService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Chat Service Implementation
 */

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final NotificationService notificationService;

    @Override
    public List<ConversationResponse> getConversationsForUser(Long userId) {
        log.info("Getting conversations for user: {}", userId);
        
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        
        return conversations.stream()
                .map(conversation -> mapToConversationResponse(conversation, userId))
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<MessageResponse> getMessagesForConversation(Long userId, Long conversationId, Pageable pageable) {
        log.info("Getting messages for user: {}, conversationId: {}", userId, conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Check if user is part of the conversation
        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new CustomException("You are not part of this conversation");
        }

        Page<Message> messagePage = messageRepository.findByConversationIdOrderBySentAtDesc(conversationId, pageable);
        
        List<MessageResponse> messageResponses = messagePage.getContent().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());

        return PagedResponse.<MessageResponse>builder()
                .content(messageResponses)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .first(messagePage.isFirst())
                .last(messagePage.isLast())
                .empty(messagePage.isEmpty())
                .build();
    }

    @Override
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        log.info("Sending message from user: {} to user: {}", senderId, request.getReceiverId());
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        // Check if users are blocked
        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(request.getReceiverId(), senderId)) {
            throw new CustomException("You are blocked by this user");
        }

        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(senderId, request.getReceiverId())) {
            throw new CustomException("You have blocked this user");
        }

        // Check if users have mutual match
        Optional<Match> mutualMatch = matchRepository.findMatchBetweenUsers(senderId, request.getReceiverId());
        if (mutualMatch.isEmpty() || mutualMatch.get().getStatus() != MatchStatus.MUTUAL) {
            throw new CustomException("You can only message users with mutual matches");
        }

        // Get or create conversation
        Conversation conversation = getOrCreateConversationEntity(senderId, request.getReceiverId());

        // Create message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setMessage(request.getMessage());
        message.setMessageType(MessageType.valueOf(request.getMessageType()));
        message.setIsRead(false);
        message.setSentAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Send notification
        notificationService.notifyNewMessage(request.getReceiverId(), senderId);

        return mapToMessageResponse(savedMessage);
    }

    @Override
    public void markMessagesAsRead(Long userId, Long conversationId) {
        log.info("Marking messages as read for user: {}, conversationId: {}", userId, conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new CustomException("You are not part of this conversation");
        }

        messageRepository.markMessagesAsRead(conversationId, userId);
    }

    @Override
    public ConversationResponse getOrCreateConversation(Long userId1, Long userId2) {
        log.info("Getting or creating conversation between users: {} and {}", userId1, userId2);
        
        Conversation conversation = getOrCreateConversationEntity(userId1, userId2);
        return mapToConversationResponse(conversation, userId1);
    }

    @Override
    public Long getUnreadMessageCount(Long userId) {
        log.info("Getting unread message count for user: {}", userId);
        
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        
        return conversations.stream()
                .mapToLong(conversation -> messageRepository.countUnreadMessages(conversation.getId(), userId))
                .sum();
    }

    @Override
    public void deleteMessage(Long userId, Long messageId) {
        log.info("Deleting message: {} by user: {}", messageId, userId);
        
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new CustomException("You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    @Override
    public void blockConversation(Long userId, Long conversationId) {
        log.info("Blocking conversation: {} by user: {}", conversationId, userId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Long otherUserId = conversation.getUser1().getId().equals(userId) ? 
                          conversation.getUser2().getId() : conversation.getUser1().getId();

        // Block the other user
        if (!blockedUserRepository.existsByBlockerIdAndBlockedUserId(userId, otherUserId)) {
            User blocker = userRepository.findById(userId).orElse(null);
            User blockedUser = userRepository.findById(otherUserId).orElse(null);
            
            if (blocker != null && blockedUser != null) {
                BlockedUser blockedUserEntity = new BlockedUser();
                blockedUserEntity.setBlocker(blocker);
                blockedUserEntity.setBlockedUser(blockedUser);
                blockedUserRepository.save(blockedUserEntity);
            }
        }

        // Deactivate conversation
        conversation.setIsActive(false);
        conversationRepository.save(conversation);
    }

    @Override
    public ConversationResponse getConversationDetails(Long userId, Long conversationId) {
        log.info("Getting conversation details for user: {}, conversationId: {}", userId, conversationId);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new CustomException("You are not part of this conversation");
        }

        return mapToConversationResponse(conversation, userId);
    }

    @Override
    public List<MessageResponse> searchMessages(Long userId, String query, Long conversationId, Pageable pageable) {
        log.info("Searching messages for user: {}, query: {}, conversationId: {}", userId, query, conversationId);
        
        // This is a simplified implementation
        // In a real application, you would use full-text search or Elasticsearch
        
        List<Conversation> conversations;
        if (conversationId != null) {
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            conversations = List.of(conversation);
        } else {
            conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        }

        // This is a basic search implementation
        // In production, you would want to use proper search indexing
        return conversations.stream()
                .flatMap(conv -> messageRepository.findByConversationIdOrderBySentAtDesc(conv.getId(), pageable).getContent().stream())
                .filter(message -> message.getMessage().toLowerCase().contains(query.toLowerCase()))
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Conversation getOrCreateConversationEntity(Long userId1, Long userId2) {
        Optional<Conversation> existingConversation = conversationRepository.findByUsers(userId1, userId2);
        
        if (existingConversation.isPresent()) {
            return existingConversation.get();
        }

        // Create new conversation
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException("User1 not found"));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException("User2 not found"));

        // Find mutual match
        Match match = matchRepository.findMatchBetweenUsers(userId1, userId2)
                .orElseThrow(() -> new CustomException("No mutual match found between users"));

        Conversation conversation = new Conversation();
        conversation.setUser1(user1);
        conversation.setUser2(user2);
        conversation.setMatch(match);
        conversation.setIsActive(true);

        return conversationRepository.save(conversation);
    }

    private ConversationResponse mapToConversationResponse(Conversation conversation, Long currentUserId) {
        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setUser1Id(conversation.getUser1().getId());
        response.setUser2Id(conversation.getUser2().getId());
        response.setMatchId(conversation.getMatch().getId());
        response.setIsActive(conversation.getIsActive());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());

        // Get other user profile
        User otherUser = conversation.getUser1().getId().equals(currentUserId) ? 
                        conversation.getUser2() : conversation.getUser1();
        if (otherUser.getProfile() != null) {
            response.setOtherUserProfile(mapToProfileResponse(otherUser.getProfile()));
        }

        // Get last message
        Optional<Message> lastMessage = messageRepository.findLastMessageByConversationId(conversation.getId());
        if (lastMessage.isPresent()) {
            response.setLastMessage(mapToMessageResponse(lastMessage.get()));
        }

        // Get unread count
        Long unreadCount = messageRepository.countUnreadMessages(conversation.getId(), currentUserId);
        response.setUnreadCount(unreadCount.intValue());

        return response;
    }

    private MessageResponse mapToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversation().getId());
        response.setSenderId(message.getSender().getId());
        response.setReceiverId(message.getReceiver().getId());
        response.setMessage(message.getMessage());
        response.setMessageType(message.getMessageType().name());
        response.setIsRead(message.getIsRead());
        response.setSentAt(message.getSentAt());
        
        if (message.getSender().getProfile() != null) {
            response.setSenderName(message.getSender().getProfile().getFirstName() + " " + 
                                  message.getSender().getProfile().getLastName());
        }

        return response;
    }

    private ProfileResponse mapToProfileResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setFullName(profile.getFirstName() + " " + profile.getLastName());
        // Add other fields as needed for chat context
        return response;
    }
}
