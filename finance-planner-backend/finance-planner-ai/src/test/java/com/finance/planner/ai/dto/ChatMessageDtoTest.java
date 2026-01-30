package com.finance.planner.ai.dto;

import com.finance.planner.ai.entity.AiConversation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatMessageDto Unit Tests")
class ChatMessageDtoTest {

    @Test
    @DisplayName("fromEntity - should map all fields correctly")
    void fromEntity_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        AiConversation entity = new AiConversation();
        entity.setId(1L);
        entity.setSessionId("session-123");
        entity.setRole("user");
        entity.setContent("Hello AI");
        entity.setTokens(15);
        entity.setCreatedAt(now);

        ChatMessageDto dto = ChatMessageDto.fromEntity(entity);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getSessionId()).isEqualTo("session-123");
        assertThat(dto.getRole()).isEqualTo("user");
        assertThat(dto.getContent()).isEqualTo("Hello AI");
        assertThat(dto.getTokens()).isEqualTo(15);
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("fromEntity - should handle null tokens")
    void fromEntity_handlesNullTokens() {
        AiConversation entity = new AiConversation();
        entity.setId(2L);
        entity.setSessionId("session-456");
        entity.setRole("assistant");
        entity.setContent("Hi there!");
        entity.setTokens(null);
        entity.setCreatedAt(LocalDateTime.now());

        ChatMessageDto dto = ChatMessageDto.fromEntity(entity);

        assertThat(dto.getTokens()).isNull();
        assertThat(dto.getContent()).isEqualTo("Hi there!");
    }
}
