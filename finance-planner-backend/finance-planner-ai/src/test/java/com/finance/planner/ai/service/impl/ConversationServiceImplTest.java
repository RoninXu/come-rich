package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.entity.AiConversation;
import com.finance.planner.ai.repository.AiConversationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationServiceImpl Unit Tests")
class ConversationServiceImplTest {

    @Mock
    private AiConversationRepository aiConversationRepository;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "test-session-123";

    @Test
    @DisplayName("createOrGetSessionId - should return existing session ID when provided")
    void createOrGetSessionId_existing() {
        String result = conversationService.createOrGetSessionId(SESSION_ID);
        assertThat(result).isEqualTo(SESSION_ID);
    }

    @Test
    @DisplayName("createOrGetSessionId - should generate new session ID when null")
    void createOrGetSessionId_generateNew_null() {
        String result = conversationService.createOrGetSessionId(null);
        assertThat(result).isNotNull().isNotEmpty().hasSize(32);
    }

    @Test
    @DisplayName("createOrGetSessionId - should generate new session ID when blank")
    void createOrGetSessionId_generateNew_blank() {
        String result = conversationService.createOrGetSessionId("  ");
        assertThat(result).isNotNull().isNotEmpty().hasSize(32);
    }

    @Test
    @DisplayName("saveMessage - should persist conversation entity")
    void saveMessage_persists() {
        when(aiConversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        conversationService.saveMessage(USER_ID, SESSION_ID, "user", "Hello", 10);

        ArgumentCaptor<AiConversation> captor = ArgumentCaptor.forClass(AiConversation.class);
        verify(aiConversationRepository).save(captor.capture());

        AiConversation saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(saved.getRole()).isEqualTo("user");
        assertThat(saved.getContent()).isEqualTo("Hello");
        assertThat(saved.getTokens()).isEqualTo(10);
    }

    @Test
    @DisplayName("getRecentMessages - should return messages in chronological order")
    void getRecentMessages_chronologicalOrder() {
        AiConversation msg2 = createConversation(2L, "assistant", "Hi", LocalDateTime.now());
        AiConversation msg1 = createConversation(1L, "user", "Hello", LocalDateTime.now().minusMinutes(1));
        // Repository returns DESC order
        when(aiConversationRepository.findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(USER_ID, SESSION_ID))
                .thenReturn(Arrays.asList(msg2, msg1));

        List<AiConversation> result = conversationService.getRecentMessages(USER_ID, SESSION_ID, 5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L); // Older first
        assertThat(result.get(1).getId()).isEqualTo(2L); // Newer second
    }

    @Test
    @DisplayName("getRecentMessages - should return empty list when no messages")
    void getRecentMessages_empty() {
        when(aiConversationRepository.findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(USER_ID, SESSION_ID))
                .thenReturn(Collections.emptyList());

        List<AiConversation> result = conversationService.getRecentMessages(USER_ID, SESSION_ID, 5);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRecentMessages - should limit to requested rounds")
    void getRecentMessages_limitedRounds() {
        // Create 6 messages (3 rounds)
        List<AiConversation> sixMessages = Arrays.asList(
                createConversation(6L, "assistant", "resp3", LocalDateTime.now()),
                createConversation(5L, "user", "msg3", LocalDateTime.now().minusMinutes(1)),
                createConversation(4L, "assistant", "resp2", LocalDateTime.now().minusMinutes(2)),
                createConversation(3L, "user", "msg2", LocalDateTime.now().minusMinutes(3)),
                createConversation(2L, "assistant", "resp1", LocalDateTime.now().minusMinutes(4)),
                createConversation(1L, "user", "msg1", LocalDateTime.now().minusMinutes(5))
        );
        when(aiConversationRepository.findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(USER_ID, SESSION_ID))
                .thenReturn(sixMessages);

        // Request only 2 rounds = 4 messages
        List<AiConversation> result = conversationService.getRecentMessages(USER_ID, SESSION_ID, 2);

        assertThat(result).hasSize(4);
    }

    @Test
    @DisplayName("getConversationHistory - should return complete history")
    void getConversationHistory_complete() {
        List<AiConversation> conversations = Arrays.asList(
                createConversation(1L, "user", "Hello", LocalDateTime.now().minusMinutes(2)),
                createConversation(2L, "assistant", "Hi there", LocalDateTime.now().minusMinutes(1))
        );
        when(aiConversationRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(USER_ID, SESSION_ID))
                .thenReturn(conversations);

        ConversationHistoryDto history = conversationService.getConversationHistory(USER_ID, SESSION_ID);

        assertThat(history.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(history.getMessages()).hasSize(2);
        assertThat(history.getTotalMessages()).isEqualTo(2);
        assertThat(history.getMessages().get(0).getRole()).isEqualTo("user");
        assertThat(history.getMessages().get(1).getRole()).isEqualTo("assistant");
    }

    private AiConversation createConversation(Long id, String role, String content, LocalDateTime createdAt) {
        AiConversation conv = new AiConversation();
        conv.setId(id);
        conv.setUserId(USER_ID);
        conv.setSessionId(SESSION_ID);
        conv.setRole(role);
        conv.setContent(content);
        conv.setCreatedAt(createdAt);
        return conv;
    }
}
