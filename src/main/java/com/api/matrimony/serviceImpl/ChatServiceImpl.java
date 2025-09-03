package com.api.matrimony.serviceImpl;



import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.api.matrimony.entity.Conversation;
import com.api.matrimony.entity.Match;
import com.api.matrimony.entity.Message;
import com.api.matrimony.entity.User;
import com.api.matrimony.entity.UserProfile;
import com.api.matrimony.enums.MatchStatus;
import com.api.matrimony.enums.MessageType;
import com.api.matrimony.exception.ApplicationException;
import com.api.matrimony.exception.ErrorEnum;
import com.api.matrimony.repository.ConversationRepository;
import com.api.matrimony.repository.MatchRepository;
import com.api.matrimony.repository.MessageRepository;
import com.api.matrimony.repository.UserRepository;
import com.api.matrimony.request.MessageRequest;
import com.api.matrimony.response.ConversationResponse;
import com.api.matrimony.response.MessageResponse;
import com.api.matrimony.response.ProfileResponse;
import com.api.matrimony.service.ChatService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

	@Service
	@RequiredArgsConstructor
	@Slf4j
	public class ChatServiceImpl  implements ChatService {

	  private final ConversationRepository conversationRepository;
	  private final MessageRepository messageRepository;
	  private final SimpMessagingTemplate ws;
	  private final UserRepository userRepository;
	  private final MatchRepository matchRepository;

	  // 1) Get all conversations for user
	  @Override
	  public List<ConversationResponse> getConversations(Long userId) {
		 List<Conversation> convList  = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
		 if(convList != null && !convList.isEmpty()) {
			 List<ConversationResponse> responce = convList.stream().map(x -> mapToConversationResponse(x,userId)).toList();
			 return responce;
		 }else {
			 throw  new ApplicationException(ErrorEnum.CNOV_NOT_FOUND.toString(),
						ErrorEnum.CNOV_NOT_FOUND.getExceptionError(), HttpStatus.OK);
		 }
	  }

	  // 2) Get messages for conversation (paged). Client may reverse order for display.
	  @Override
	  public Page<MessageResponse> getMessages(Long conversationId, int page, int size) {
	    PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt").and(Sort.by("id").descending()));
	    return messageRepository.findByConversationId(conversationId, pr).map(this::toResponse);
	  }

	  // 3) Send message: persist, broadcast over WS
	  
	  @Override
	  @Transactional
	  public MessageResponse saveMessage(Long senderId, MessageRequest request) {
	        log.info("Sending message from user: {} to user: {}", senderId, request.getReceiverId());
	        
	        User sender = userRepository.findById(senderId)
	                .orElseThrow(() -> new ApplicationException(ErrorEnum.SENDER_NOT_FUND.toString(),
	    					ErrorEnum.SENDER_NOT_FUND.getExceptionError(), HttpStatus.OK));
	        User receiver = userRepository.findById(request.getReceiverId())
	                .orElseThrow(() -> new ApplicationException(ErrorEnum.RECIVER_NOT_FUND.toString(),
	    					ErrorEnum.RECIVER_NOT_FUND.getExceptionError(), HttpStatus.OK));

	        // Check if users are blocked
//	        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(request.getReceiverId(), senderId)) {
//	            throw new ApplicationException(ErrorEnum.YOU_BLOCK_BY_USER.toString(),
//						ErrorEnum.YOU_BLOCK_BY_USER.getExceptionError(), HttpStatus.OK);
//	        }
//
//	        if (blockedUserRepository.existsByBlockerIdAndBlockedUserId(senderId, request.getReceiverId())) {
//	            throw new ApplicationException(ErrorEnum.YOU_BLOCK_USER.toString(),
//						ErrorEnum.YOU_BLOCK_USER.getExceptionError(), HttpStatus.OK);
//	        }

	        // Check if users have mutual match
	        Optional<Match> mutualMatch = matchRepository.findMatchBetweenUsers(senderId, request.getReceiverId());
	        if (mutualMatch.isEmpty() || mutualMatch.get().getStatus() != MatchStatus.MUTUAL) {
	            throw new ApplicationException(ErrorEnum.ONLY_MUTUAL_MATCH_CAN_MSG.toString(),
						ErrorEnum.ONLY_MUTUAL_MATCH_CAN_MSG.getExceptionError(), HttpStatus.OK);
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
	       // notificationService.notifyNewMessage(request.getReceiverId(), senderId);

	        MessageResponse msg = mapToMessageResponse(savedMessage);
	        
	     // Broadcast to WS topic for conversation
		    ws.convertAndSend("/topic/conversations/" + conversation.getId(), msg);
	        
	        return msg ;
	    }

	  // 5) Get conversation between two users
	  public ConversationResponse getBetween(Long currentUserId, Long otherUserId) {
		  Optional<Conversation> convBtw2user = conversationRepository.findByUsers(currentUserId, otherUserId);
		  if(!convBtw2user.isEmpty()) {
			  ConversationResponse response =  mapToConversationResponse(convBtw2user.get(),currentUserId);
			  return response;
		  }else {
			  throw  new ApplicationException(ErrorEnum.CNOV_NOT_FOUND.toString(),
						ErrorEnum.CNOV_NOT_FOUND.getExceptionError(), HttpStatus.OK);
		  }
	  }

	  // 6) Unread message count
	  public long unreadCount(Long userId) {
	    return messageRepository.countByReceiverIdAndIsReadFalse(userId);
	  }

	  // 7) Block conversation
	  @Transactional
	  public void blockConversation(Long conversationId) {
	    Conversation c = conversationRepository.findById(conversationId)
	        .orElseThrow(() -> new NoSuchElementException("Conversation not found"));
	    c.setIsActive(false);
	    conversationRepository.save(c);
	  }

	  // 8) Typing indicator (WS)
	  public void typing(Long conversationId, Long userId, boolean typing) {
	    Map<String, Object> payload = new HashMap<>();
	    payload.put("conversationId", conversationId);
	    payload.put("userId", userId);
	    payload.put("typing", typing);
	    ws.convertAndSend("/topic/conversations/" + conversationId + "/typing", payload);
	  }

	  private MessageResponse toResponse(Message m) {
	    MessageResponse r = new MessageResponse();
	    r.setId(m.getId());
	    r.setConversationId(m.getConversation().getId());
	    r.setSenderId(m.getSender().getId());
	    r.setReceiverId(m.getReceiver().getId());
	    r.setMessage(m.getMessage());
	    r.setMessageType(m.getMessageType().name());
	    r.setSentAt(m.getSentAt());
	    return r;
	  }
	  
	  private User getUserById(Long userId) {
			User user = userRepository.findById(userId)
					.orElseThrow(() ->new ApplicationException(ErrorEnum.USER_NOT_FOUND.toString(),
								ErrorEnum.USER_NOT_FOUND.getExceptionError(), HttpStatus.OK));
			return user;
		}

//	@Override
//	public MessageResponse saveMessage(SendMessageRequest request) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<MessageResponse> getMessages(String conversationId) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<MessageResponse> getUnreadMessages(Long userId) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public int markMessagesAsRead(Long conversationId, Long userId) {
		int updatedRow = messageRepository.markAsRead(conversationId, userId);
		return updatedRow;
	}
	
	 // Helper methods
    private Conversation getOrCreateConversationEntity(Long userId1, Long userId2) {
        Optional<Conversation> existingConversation = conversationRepository.findByUsers(userId1, userId2);
        
        if (existingConversation.isPresent()) {
            return existingConversation.get();
        }

        // Create new conversation
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.USER_1_NOT_FOUND.toString(),
						ErrorEnum.USER_1_NOT_FOUND.getExceptionError(), HttpStatus.OK));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.USER_2_NOT_FOUND.toString(),
						ErrorEnum.USER_2_NOT_FOUND.getExceptionError(), HttpStatus.OK));

        // Find mutual match
        Match match = matchRepository.findMatchBetweenUsers(userId1, userId2)
                .orElseThrow(() -> new ApplicationException(ErrorEnum.NO_MATCH_FUND_BTWN_USER.toString(),
						ErrorEnum.NO_MATCH_FUND_BTWN_USER.getExceptionError(), HttpStatus.OK));

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
            response.setSenderName(message.getSender().getProfile().getFullName());
        }

        return response;
    }

    private ProfileResponse mapToProfileResponse(UserProfile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setUserId(profile.getUser().getId());
        response.setFullName(profile.getFullName());
        // Add other fields as needed for chat context
        return response;
    }
	
	  
	}


