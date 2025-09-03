package com.api.matrimony.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingStatus {
    private Long conversationId;
    private Long userId;
    private boolean typing;
}
