package com.api.matrimony.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.api.matrimony.request.MessageRequest;
import com.api.matrimony.response.ConversationResponse;
import com.api.matrimony.response.MessageResponse;

/**
 * Chat Service Interface
 */

public interface ChatService {

	MessageResponse saveMessage(Long userId,MessageRequest request);  
	
	int markMessagesAsRead(Long conversationId, Long userId);  

	List<ConversationResponse> getConversations(Long id);   

	Page<MessageResponse> getMessages(Long conversationId, int page, int size);  

	ConversationResponse getBetween(Long id, Long otherUserId);   

	long unreadCount(Long id);

	void typing(Long conversationId, Long id, boolean typing);   

}
