package com.api.matrimony.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.matrimony.entity.User;
import com.api.matrimony.request.MessageRequest;
import com.api.matrimony.request.TypingStatus;
import com.api.matrimony.response.APIResonse;
import com.api.matrimony.response.ConversationResponse;
import com.api.matrimony.response.MessageResponse;
import com.api.matrimony.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

	private final ChatService chatService;
	private final SimpMessagingTemplate messagingTemplate;

	// 1) Get all conversations for the current user
	
	@GetMapping("/conversations")
	public ResponseEntity<APIResonse<List<ConversationResponse>>> myConversations(@AuthenticationPrincipal User currentUser) {
		APIResonse<List<ConversationResponse>> response = new APIResonse<>();
		List<ConversationResponse> chatsConversations = chatService.getConversations(currentUser.getId());
		response.setData(chatsConversations);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// 2) Get messages for a specific conversation (paged)
	
	@GetMapping("/messages")
	public ResponseEntity<APIResonse<Page<MessageResponse>>> messages(@RequestParam Long conversationId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
		APIResonse<Page<MessageResponse>> response = new APIResonse<>();
		Page<MessageResponse> messagesByConversationId = chatService.getMessages(conversationId, page, size);
		response.setData(messagesByConversationId);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// 3) Send a message (also available via WebSocket)

	@PostMapping("/send")
	public ResponseEntity<APIResonse<MessageResponse>> send(@AuthenticationPrincipal User currentUser,
			@RequestBody MessageRequest request) {
		log.info("Sending message from user: {} to user: {}", currentUser.getId(), request.getReceiverId());
		APIResonse<MessageResponse> response = new APIResonse<>();
		// 1. Save to DB
		MessageResponse savedMessage = chatService.saveMessage(currentUser.getId(),request);
		// 2. Push to WebSocket subscribers
		String destination = "/topic/chat/" + savedMessage.getConversationId();
		messagingTemplate.convertAndSend(destination, savedMessage);
		response.setData(savedMessage);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	// 4) Mark messages as read in a conversation
	
	@PostMapping("/read")
	public ResponseEntity<APIResonse<Map<String, Object>>> markRead(@AuthenticationPrincipal User currentUser,
			@RequestParam Long conversationId) {
		APIResonse<Map<String, Object>> response = new APIResonse<>();
		int updated = chatService.markMessagesAsRead(conversationId, currentUser.getId());
		
	    // 🔹 Push event to WebSocket subscribers of this conversation
	    String destination = "/topic/chat/" + conversationId + "/read";
	    messagingTemplate.convertAndSend(destination, Map.of("updated", updated));
		
		response.setData(Map.of("updated", updated));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// 5) Get conversation between current user and another user
	
	@GetMapping("/conversation-with/{otherUserId}")
	public ResponseEntity<APIResonse<ConversationResponse>> conversationWith(@AuthenticationPrincipal User currentUser,
			@PathVariable Long otherUserId) {
		APIResonse<ConversationResponse> response = new APIResonse<>();
		ConversationResponse conversation = chatService.getBetween(currentUser.getId(), otherUserId);
		response.setData(conversation);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	// 6) Get unread message count for user
	
	@GetMapping("/unread-count")
	public ResponseEntity<APIResonse<Map<String, Object>>> unread(@AuthenticationPrincipal User currentUser) {
		APIResonse<Map<String, Object>> response = new APIResonse<>();
		Map<String, Object> dataMap = Map.of("unread", chatService.unreadCount(currentUser.getId()));
		response.setData(dataMap);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

//	// 7) Block user from conversation
//	@PostMapping("/conversations/{id}/block")
//	public void block(@PathVariable Long id) {
//		chatService.blockConversation(id);
//	}

    /**
     * Handle typing indicator via WebSocket (no DB persistence).
     * Client sends to: /app/chat/typing
     * Subscribers listen on: /topic/chat/{conversationId}/typing
     */
    @MessageMapping("/chat/typing")
    public void typing(TypingStatus typingStatus, @AuthenticationPrincipal User currentUser) {
        // Ensure userId comes from session, not just client
        typingStatus.setUserId(currentUser.getId());

        String destination = "/topic/chat/" + typingStatus.getConversationId() + "/typing";
        messagingTemplate.convertAndSend(destination, typingStatus);
    }

//    public ResponseEntity<APIResonse<List<SubscriptionPlanResponse>>> getSubscriptionPlans() {
//        log.info("Getting all subscription plans");
//        APIResonse< List<SubscriptionPlanResponse>> response = new APIResonse<>();
//            List<SubscriptionPlanResponse> plans = subscriptionService.getAllActivePlans();
//            response.setData(plans);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//    }
	
	
}
