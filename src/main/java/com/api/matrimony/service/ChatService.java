package com.api.matrimony.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.api.matrimony.request.MessageRequest;
import com.api.matrimony.response.ConversationResponse;
import com.api.matrimony.response.MessageResponse;
import com.api.matrimony.response.PagedResponse;

/**
 * Chat Service Interface
 */

public interface ChatService {
    List<ConversationResponse> getConversationsForUser(Long userId);
    PagedResponse<MessageResponse> getMessagesForConversation(Long userId, Long conversationId, Pageable pageable);
    MessageResponse sendMessage(Long senderId, MessageRequest request);
    void markMessagesAsRead(Long userId, Long conversationId);
    ConversationResponse getOrCreateConversation(Long userId1, Long userId2);
    Long getUnreadMessageCount(Long userId);
    void deleteMessage(Long userId, Long messageId);
    void blockConversation(Long userId, Long conversationId);
    ConversationResponse getConversationDetails(Long userId, Long conversationId);
    List<MessageResponse> searchMessages(Long userId, String query, Long conversationId, Pageable pageable);
}
