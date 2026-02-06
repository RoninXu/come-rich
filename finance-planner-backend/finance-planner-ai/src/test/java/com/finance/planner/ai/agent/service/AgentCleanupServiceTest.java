package com.finance.planner.ai.agent.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentCleanupService Unit Tests")
class AgentCleanupServiceTest {

    @Mock
    private ConfirmationStore confirmationStore;

    @InjectMocks
    private AgentCleanupService cleanupService;

    @Test
    @DisplayName("cleanupSession - should cancel all pending confirmations")
    void cleanupSession_cancelsConfirmations() {
        cleanupService.cleanupSession(1L, "session-1", "test error");

        verify(confirmationStore).cancelAllForUser(1L);
    }

    @Test
    @DisplayName("cleanupSession - should not throw when confirmationStore fails")
    void cleanupSession_handlesErrors() {
        doThrow(new RuntimeException("store error")).when(confirmationStore).cancelAllForUser(1L);

        // Should not throw
        cleanupService.cleanupSession(1L, "session-1", "test");
    }
}
