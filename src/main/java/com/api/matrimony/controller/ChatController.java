package com.api.matrimony.controller;


import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import com.api.matrimony.response.APIResonse;
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
    public ResponseEntity<APIResonse<List<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting conversations for user: {}", currentUser.getId());
        
        APIResonse<List<ConversationResponse>> response = new APIResonse<>();
            List<ConversationResponse> conversations = chatService.getConversationsForUser(
                    currentUser.getId());
            response.setData(conversations);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get messages for a specific conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<APIResonse<PagedResponse<MessageResponse>>> getMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting messages for user: {}, conversationId: {}, page: {}, size: {}", 
                currentUser.getId(), conversationId, page, size);
        
        APIResonse<PagedResponse<MessageResponse>> response = new APIResonse<>();
            Pageable pageable = PageRequest.of(page, size);
            PagedResponse<MessageResponse> messages = chatService.getMessagesForConversation(
                    currentUser.getId(), conversationId, pageable);
            
            response.setData(messages);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Send a message
     */
    @PostMapping("/send")
    public ResponseEntity<APIResonse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody MessageRequest request) {
        
        log.info("Sending message from user: {} to user: {}", 
                currentUser.getId(), request.getReceiverId());
        
        APIResonse<MessageResponse> response = new APIResonse<>();
            MessageResponse message = chatService.sendMessage(currentUser.getId(), request);
            response.setData(message);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Mark messages as read in a conversation
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<APIResonse<String>> markMessagesAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {
        
        log.info("Marking messages as read for user: {}, conversationId: {}", 
                currentUser.getId(), conversationId);
        
        APIResonse<String> response = new APIResonse<>();
            chatService.markMessagesAsRead(currentUser.getId(), conversationId);
            response.setData("Mark messages as read in a conversation");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get conversation between current user and another user
     */
    @GetMapping("/conversations/with/{userId}")
    public ResponseEntity<APIResonse<ConversationResponse>> getConversationWithUser(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        
        log.info("Getting conversation between user: {} and user: {}", 
                currentUser.getId(), userId);
        
        APIResonse<ConversationResponse> response = new APIResonse<>();
            ConversationResponse conversation = chatService.getOrCreateConversation(
                    currentUser.getId(), userId);
            response.setData(conversation);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get unread message count for user
     */
    @GetMapping("/unreadCount")
    public ResponseEntity<APIResonse<Long>> getUnreadMessageCount(
            @AuthenticationPrincipal User currentUser) {
        
        log.info("Getting unread message count for user: {}", currentUser.getId());
        
        APIResonse<Long> response = new APIResonse<>();
            Long unreadCount = chatService.getUnreadMessageCount(currentUser.getId());
            response.setData(unreadCount);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Delete a message (soft delete)
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<APIResonse<String>> deleteMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long messageId) {
        
        log.info("Deleting message: {} by user: {}", messageId, currentUser.getId());
        
        APIResonse<String> response = new APIResonse<>();
            chatService.deleteMessage(currentUser.getId(), messageId);
            response.setData("Delete a message (soft delete)");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Block user from conversation
     */
    @PostMapping("/conversations/{conversationId}/block")
    public ResponseEntity<APIResonse<String>> blockUserInConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {
        
        log.info("Blocking conversation: {} by user: {}", conversationId, currentUser.getId());
        
        APIResonse<String> response = new APIResonse<>();
            chatService.blockConversation(currentUser.getId(), conversationId);
            response.setData(" Block user from conversation ");
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get conversation details
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<APIResonse<ConversationResponse>> getConversationDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {
        
        log.info("Getting conversation details for user: {}, conversationId: {}", 
                currentUser.getId(), conversationId);
        
        APIResonse<ConversationResponse> response = new APIResonse<>();
            ConversationResponse conversation = chatService.getConversationDetails(
                    currentUser.getId(), conversationId);
            response.setData(conversation);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Search messages in conversations
     */
    @GetMapping("/searchMessages")
    public ResponseEntity<APIResonse<List<MessageResponse>>> searchMessages(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String query,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching messages for user: {}, query: {}, conversationId: {}", 
                currentUser.getId(), query, conversationId);
        
        APIResonse<List<MessageResponse>> response = new APIResonse<>();
            Pageable pageable = PageRequest.of(page, size);
            List<MessageResponse> messages = chatService.searchMessages(
                    currentUser.getId(), query, conversationId, pageable);
            response.setData(messages);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
