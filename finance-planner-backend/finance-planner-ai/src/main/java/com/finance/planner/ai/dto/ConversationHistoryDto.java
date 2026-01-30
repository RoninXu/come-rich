package com.finance.planner.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationHistoryDto {

    private String sessionId;
    private List<ChatMessageDto> messages;
    private int totalMessages;
}
