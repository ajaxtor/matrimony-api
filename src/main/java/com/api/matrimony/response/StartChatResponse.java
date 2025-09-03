package com.api.matrimony.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartChatResponse {
    private Long conversationId;
    private Long user1Id;
    private Long user2Id;
    private String matchId;
    private boolean isActive;
}

