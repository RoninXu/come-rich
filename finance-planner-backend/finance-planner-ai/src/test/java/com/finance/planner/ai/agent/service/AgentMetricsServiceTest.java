package com.finance.planner.ai.agent.service;

import com.finance.planner.ai.agent.entity.AgentToolMetrics;
import com.finance.planner.ai.agent.repository.AgentToolMetricsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentMetricsService Unit Tests")
class AgentMetricsServiceTest {

    @Mock
    private AgentToolMetricsRepository metricsRepository;

    @InjectMocks
    private AgentMetricsService metricsService;

    @Test
    @DisplayName("recordToolExecution - should save metrics correctly")
    void recordToolExecution_savesMetrics() {
        metricsService.recordToolExecution(1L, "session-1", "list_transactions",
                true, 150, null, false);

        ArgumentCaptor<AgentToolMetrics> captor = ArgumentCaptor.forClass(AgentToolMetrics.class);
        verify(metricsRepository).save(captor.capture());

        AgentToolMetrics saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getSessionId()).isEqualTo("session-1");
        assertThat(saved.getToolName()).isEqualTo("list_transactions");
        assertThat(saved.getSuccess()).isTrue();
        assertThat(saved.getLatencyMs()).isEqualTo(150);
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getCached()).isFalse();
    }

    @Test
    @DisplayName("recordToolExecution - should mark cached=true for cached results")
    void recordToolExecution_cachedResult() {
        metricsService.recordToolExecution(1L, "session-1", "get_budgets",
                true, 5, null, true);

        ArgumentCaptor<AgentToolMetrics> captor = ArgumentCaptor.forClass(AgentToolMetrics.class);
        verify(metricsRepository).save(captor.capture());
        assertThat(captor.getValue().getCached()).isTrue();
    }

    @Test
    @DisplayName("recordToolExecution - should not throw on save failure")
    void recordToolExecution_handlesSaveError() {
        doThrow(new RuntimeException("DB error")).when(metricsRepository).save(any());

        // Should not throw
        metricsService.recordToolExecution(1L, "session-1", "list_transactions",
                true, 100, null, false);
    }
}
