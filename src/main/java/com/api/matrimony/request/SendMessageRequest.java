package com.api.matrimony.request;


import com.api.matrimony.enums.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
  @NotNull
  private Long conversationId;
  @NotNull
  private Long receiverId;
  @NotBlank
  private String message;
  private MessageType messageType = MessageType.TEXT;
}
