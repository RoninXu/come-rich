package com.finance.planner.ai.controller;

import com.finance.planner.ai.agent.dto.metrics.AgentErrorMetricDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsOverviewDto;
import com.finance.planner.ai.agent.dto.metrics.AgentMetricsTimelinePointDto;
import com.finance.planner.ai.agent.dto.metrics.AgentToolMetricDto;
import com.finance.planner.ai.agent.service.AgentCleanupService;
import com.finance.planner.ai.agent.service.AgentMetricsQueryService;
import com.finance.planner.ai.agent.service.AgentRiskConfigService;
import com.finance.planner.ai.agent.service.AgentService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentController Unit Tests")
class AgentControllerTest {

    @Mock
    private AgentService agentService;
    @Mock
    private AgentRiskConfigService riskConfigService;
    @Mock
    private AgentCleanupService agentCleanupService;
    @Mock
    private AgentMetricsQueryService metricsQueryService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AgentController agentController;

    @Test
    @DisplayName("setRiskThreshold - should update threshold")
    void setRiskThreshold_shouldUpdate() {
        mockUser(1L, "alice");

        ApiResponse<Void> response = agentController.setRiskThreshold(userDetails, BigDecimal.valueOf(8888));

        verify(riskConfigService).setRiskThreshold(1L, BigDecimal.valueOf(8888));
        assertThat(response.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
    }

    @Test
    @DisplayName("getMetricsOverview - should return overview from service")
    void getMetricsOverview_shouldReturnData() {
        mockUser(1L, "alice");
        AgentMetricsOverviewDto dto = AgentMetricsOverviewDto.builder()
                .totalCalls(10L)
                .successfulCalls(8L)
                .failedCalls(2L)
                .build();
        when(metricsQueryService.getOverview(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(dto);

        ApiResponse<AgentMetricsOverviewDto> response = agentController.getMetricsOverview(userDetails, null, null, 7);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getTotalCalls()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getMetricsOverview - should throw when only one date exists")
    void getMetricsOverview_shouldValidateDatePair() {
        mockUser(1L, "alice");

        assertThatThrownBy(() ->
                agentController.getMetricsOverview(userDetails, LocalDate.of(2026, 3, 1), null, 7)
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("startDate 和 endDate 需要同时提供");
    }

    @Test
    @DisplayName("getMetricsOverview - should throw when days out of range")
    void getMetricsOverview_shouldValidateDays() {
        mockUser(1L, "alice");

        assertThatThrownBy(() ->
                agentController.getMetricsOverview(userDetails, null, null, 0)
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining("days 必须在 1 到 365 之间");
    }

    @Test
    @DisplayName("getToolMetrics - should return tool ranking list")
    void getToolMetrics_shouldReturnData() {
        mockUser(1L, "alice");
        List<AgentToolMetricDto> tools = List.of(
                AgentToolMetricDto.builder().toolName("queryTransactions").totalCalls(50L).build()
        );
        when(metricsQueryService.getToolMetrics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(tools);

        ApiResponse<List<AgentToolMetricDto>> response = agentController.getToolMetrics(userDetails, null, null, 7, 10);

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getToolName()).isEqualTo("queryTransactions");
    }

    @Test
    @DisplayName("getMetricsTimeline - should return timeline points")
    void getMetricsTimeline_shouldReturnData() {
        mockUser(1L, "alice");
        List<AgentMetricsTimelinePointDto> points = List.of(
                AgentMetricsTimelinePointDto.builder().date(LocalDate.of(2026, 3, 1)).totalCalls(20L).build()
        );
        when(metricsQueryService.getTimeline(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(points);

        ApiResponse<List<AgentMetricsTimelinePointDto>> response = agentController.getMetricsTimeline(userDetails, null, null, 7);

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getTotalCalls()).isEqualTo(20L);
    }

    @Test
    @DisplayName("getErrorMetrics - should return error stats")
    void getErrorMetrics_shouldReturnData() {
        mockUser(1L, "alice");
        List<AgentErrorMetricDto> errs = List.of(
                AgentErrorMetricDto.builder().errorMessage("Timeout").count(5L).build()
        );
        when(metricsQueryService.getErrorStats(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(errs);

        ApiResponse<List<AgentErrorMetricDto>> response = agentController.getErrorMetrics(userDetails, null, null, 7, 10);

        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getErrorMessage()).isEqualTo("Timeout");
    }

    @Test
    @DisplayName("getToolMetrics - limit should be clamped to max 100")
    void getToolMetrics_shouldClampLimit() {
        mockUser(1L, "alice");
        when(metricsQueryService.getToolMetrics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(100)))
                .thenReturn(List.of());

        agentController.getToolMetrics(userDetails, null, null, 7, 9999);

        verify(metricsQueryService).getToolMetrics(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(100));
    }

    private void mockUser(Long id, String username) {
        when(userDetails.getUsername()).thenReturn(username);
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
    }
}

