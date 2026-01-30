package com.finance.planner.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.service.*;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiChatServiceImpl Unit Tests")
class AiChatServiceImplTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private LlmClient llmClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "test-session";

    @Test
    @DisplayName("streamChat - should throw when rate limit exceeded")
    void streamChat_rateLimitExceeded() {
        when(rateLimitService.isAllowed(USER_ID)).thenReturn(false);

        SseEmitter emitter = new SseEmitter();

        assertThatThrownBy(() -> aiChatService.streamChat(USER_ID, "Hello", SESSION_ID, emitter))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("今日对话次数已用完");
    }

    @Test
    @DisplayName("streamChat - should save user message and start stream")
    void streamChat_savesUserMessage() {
        when(rateLimitService.isAllowed(USER_ID)).thenReturn(true);
        when(conversationService.createOrGetSessionId(SESSION_ID)).thenReturn(SESSION_ID);
        when(promptBuilder.buildMessages(eq(USER_ID), eq(SESSION_ID), eq("Hello")))
                .thenReturn(List.of(Map.of("role", "user", "content", "Hello")));
        when(llmClient.streamChat(any())).thenReturn(Flux.empty());

        SseEmitter emitter = new SseEmitter(120_000L);
        aiChatService.streamChat(USER_ID, "Hello", SESSION_ID, emitter);

        verify(conversationService).saveMessage(USER_ID, SESSION_ID, "user", "Hello", null);
        verify(llmClient).streamChat(any());
    }

    @Test
    @DisplayName("streamChat - should create new session when sessionId is null")
    void streamChat_createsNewSession() {
        when(rateLimitService.isAllowed(USER_ID)).thenReturn(true);
        when(conversationService.createOrGetSessionId(null)).thenReturn("new-session");
        when(promptBuilder.buildMessages(eq(USER_ID), eq("new-session"), eq("Hello")))
                .thenReturn(List.of(Map.of("role", "user", "content", "Hello")));
        when(llmClient.streamChat(any())).thenReturn(Flux.empty());

        SseEmitter emitter = new SseEmitter(120_000L);
        aiChatService.streamChat(USER_ID, "Hello", null, emitter);

        verify(conversationService).createOrGetSessionId(null);
        verify(conversationService).saveMessage(USER_ID, "new-session", "user", "Hello", null);
    }

    @Test
    @DisplayName("getHistory - should delegate to conversation service")
    void getHistory_delegates() {
        ConversationHistoryDto expected = ConversationHistoryDto.builder()
                .sessionId(SESSION_ID)
                .messages(Collections.emptyList())
                .totalMessages(0)
                .build();
        when(conversationService.getConversationHistory(USER_ID, SESSION_ID)).thenReturn(expected);

        ConversationHistoryDto result = aiChatService.getHistory(USER_ID, SESSION_ID);

        assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(result.getTotalMessages()).isEqualTo(0);
    }

    @Test
    @DisplayName("getRemainingChats - should delegate to rate limit service")
    void getRemainingChats_delegates() {
        when(rateLimitService.getRemainingChats(USER_ID)).thenReturn(7);

        int remaining = aiChatService.getRemainingChats(USER_ID);

        assertThat(remaining).isEqualTo(7);
    }
}
