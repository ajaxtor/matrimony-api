package com.api.matrimony.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Conversation Response DTO
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {
    
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private Long matchId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProfileResponse otherUserProfile;
    private MessageResponse lastMessage;
    private Integer unreadCount;
}

