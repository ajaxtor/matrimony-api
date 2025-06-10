package com.api.matrimony.controller;


import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.entity.User;
import com.api.matrimony.request.MessageRequest;
import com.api.matrimony.response.ApiResponse;
import com.api.matrimony.response.ConversationResponse;
import com.api.matrimony.response.MessageResponse;
import com.api.matrimony.response.PagedResponse;
import com.api.matrimony.service.ChatService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Chat Controller for handling messaging and conversations
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting conversations for user: {}", currentUser.getId());
        
        try {
            List<ConversationResponse> conversations = chatService.getConversationsForUser(
                    currentUser.getId());
            
            return ResponseEntity.ok(ApiResponse.success(conversations, 
                    "Conversations retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting conversations for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get messages for a specific conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting messages for user: {}, conversationId: {}, page: {}, size: {}", 
                currentUser.getId(), conversationId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            PagedResponse<MessageResponse> messages = chatService.getMessagesForConversation(
                    currentUser.getId(), conversationId, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, 
                    "Messages retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting messages for user: {}, conversationId: {}", 
                    currentUser.getId(), conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Send a message
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody MessageRequest request) {
        
        log.info("Sending message from user: {} to user: {}", 
                currentUser.getId(), request.getReceiverId());
        
        try {
            MessageResponse message = chatService.sendMessage(currentUser.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(message, "Message sent successfully"));
        } catch (Exception e) {
            log.error("Error sending message from user: {} to user: {}", 
                    currentUser.getId(), request.getReceiverId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Mark messages as read in a conversation
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<String>> markMessagesAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {
        
        log.info("Marking messages as read for user: {}, conversationId: {}", 
                currentUser.getId(), conversationId);
        
        try {
            chatService.markMessagesAsRead(currentUser.getId(), conversationId);
            return ResponseEntity.ok(ApiResponse.success("Success", "Messages marked as read"));
        } catch (Exception e) {
            log.error("Error marking messages as read for user: {}, conversationId: {}", 
                    currentUser.getId(), conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get conversation between current user and another user
     */
    @GetMapping("/conversations/with/{userId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationWithUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("Getting conversation between user: {} and user: {}", 
                currentUser.getId(), userId);
        
        try {
            ConversationResponse conversation = chatService.getOrCreateConversation(
                    currentUser.getId(), userId);
            
            return ResponseEntity.ok(ApiResponse.success(conversation, 
                    "Conversation retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting conversation between user: {} and user: {}", 
                    currentUser.getId(), userId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get unread message count for user
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadMessageCount(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting unread message count for user: {}", currentUser.getId());
        
        try {
            Long unreadCount = chatService.getUnreadMessageCount(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(unreadCount, 
                    "Unread count retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting unread count for user: {}", currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete a message (soft delete)
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long messageId) {
        
        log.info("Deleting message: {} by user: {}", messageId, currentUser.getId());
        
        try {
            chatService.deleteMessage(currentUser.getId(), messageId);
            return ResponseEntity.ok(ApiResponse.success("Success", "Message deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting message: {} by user: {}", messageId, currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Block user from conversation
     */
    @PostMapping("/conversations/{conversationId}/block")
    public ResponseEntity<ApiResponse<String>> blockUserInConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {
        
        log.info("Blocking conversation: {} by user: {}", conversationId, currentUser.getId());
        
        try {
            chatService.blockConversation(currentUser.getId(), conversationId);
            return ResponseEntity.ok(ApiResponse.success("Success", "User blocked successfully"));
        } catch (Exception e) {
            log.error("Error blocking conversation: {} by user: {}", 
                    conversationId, currentUser.getId(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get conversation details
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {
        
        log.info("Getting conversation details for user: {}, conversationId: {}", 
                currentUser.getId(), conversationId);
        
        try {
            ConversationResponse conversation = chatService.getConversationDetails(
                    currentUser.getId(), conversationId);
            
            return ResponseEntity.ok(ApiResponse.success(conversation, 
                    "Conversation details retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting conversation details for user: {}, conversationId: {}", 
                    currentUser.getId(), conversationId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Search messages in conversations
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> searchMessages(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String query,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching messages for user: {}, query: {}, conversationId: {}", 
                currentUser.getId(), query, conversationId);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<MessageResponse> messages = chatService.searchMessages(
                    currentUser.getId(), query, conversationId, pageable);
            
            return ResponseEntity.ok(ApiResponse.success(messages, 
                    "Message search completed successfully"));
        } catch (Exception e) {
            log.error("Error searching messages for user: {}, query: {}", 
                    currentUser.getId(), query, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
