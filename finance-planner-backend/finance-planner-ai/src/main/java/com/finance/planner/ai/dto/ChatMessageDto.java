package com.finance.planner.ai.dto;

import com.finance.planner.ai.entity.AiConversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long id;
    private String sessionId;
    private String role;
    private String content;
    private Integer tokens;
    private String messageType;
    private String toolCalls;
    private String toolCallId;
    private LocalDateTime createdAt;

    public static ChatMessageDto fromEntity(AiConversation entity) {
        return ChatMessageDto.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .role(entity.getRole())
                .content(entity.getContent())
                .tokens(entity.getTokens())
                .messageType(entity.getMessageType())
                .toolCalls(entity.getToolCalls())
                .toolCallId(entity.getToolCallId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
