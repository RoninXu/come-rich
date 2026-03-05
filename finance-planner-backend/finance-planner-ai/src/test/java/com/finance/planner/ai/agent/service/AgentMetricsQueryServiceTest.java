package com.finance.planner.ai.agent.service;

import com.finance.planner.ai.agent.dto.metrics.AgentErrorMetricDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsOverviewDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsTimelinePointDto;
import com.finance.planner.ai.agent.dto.metrics.AgentToolMetricDto;
import com.finance.planner.ai.agent.repository.AgentToolMetricsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentMetricsQueryService Unit Tests")
class AgentMetricsQueryServiceTest {

    @Mock
    private AgentToolMetricsRepository metricsRepository;

    @InjectMocks
    private AgentMetricsQueryService queryService;

    @Test
    @DisplayName("getOverview - should aggregate overview metrics")
    void getOverview_shouldAggregateMetrics() {
        when(metricsRepository.getOverviewStats(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new Object[]{10L, 8L, 123.456, 3L, 4L});

        AgentMetricsOverviewDto result = queryService.getOverview(
                1L,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        );

        assertThat(result.getTotalCalls()).isEqualTo(10L);
        assertThat(result.getSuccessfulCalls()).isEqualTo(8L);
        assertThat(result.getFailedCalls()).isEqualTo(2L);
        assertThat(result.getTotalSessions()).isEqualTo(4L);
        assertThat(result.getSuccessRate()).isEqualTo(80.0);
        assertThat(result.getAverageLatencyMs()).isEqualTo(123.46);
        assertThat(result.getCacheHitRate()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("getToolMetrics - should map ranking rows")
    void getToolMetrics_shouldMapRows() {
        when(metricsRepository.getToolStats(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(
                        new Object[]{"list_transactions", 6L, 5L, 101.2, 2L},
                        new Object[]{"get_budgets", 4L, 3L, 88.0, 1L}
                ));

        List<AgentToolMetricDto> result = queryService.getToolMetrics(
                1L,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now(),
                10
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getToolName()).isEqualTo("list_transactions");
        assertThat(result.get(0).getSuccessRate()).isEqualTo(83.33);
        assertThat(result.get(0).getCacheHitRate()).isEqualTo(33.33);
        assertThat(result.get(1).getFailedCalls()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getTimeline - should map date timeline rows")
    void getTimeline_shouldMapRows() {
        when(metricsRepository.getTimelineStats(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        new Object[]{Date.valueOf("2026-03-01"), 5L, 4L, 120.0},
                        new Object[]{Date.valueOf("2026-03-02"), 3L, 2L, 95.0}
                ));

        List<AgentMetricsTimelinePointDto> result = queryService.getTimeline(
                1L,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate().toString()).isEqualTo("2026-03-01");
        assertThat(result.get(0).getSuccessRate()).isEqualTo(80.0);
        assertThat(result.get(1).getFailedCalls()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getErrorStats - should map and normalize errors")
    void getErrorStats_shouldMapRows() {
        when(metricsRepository.getErrorStats(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(
                        new Object[]{"timeout", 4L},
                        new Object[]{null, 2L}
                ));

        List<AgentErrorMetricDto> result = queryService.getErrorStats(
                1L,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now(),
                10
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getErrorMessage()).isEqualTo("timeout");
        assertThat(result.get(1).getErrorMessage()).isEqualTo("未知错误");
        assertThat(result.get(1).getCount()).isEqualTo(2L);
    }
}

