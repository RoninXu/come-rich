package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.AddGoalProgressParams;
import com.finance.planner.goal.dto.AddProgressRequest;
import com.finance.planner.goal.dto.GoalProgressDto;
import com.finance.planner.goal.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddGoalProgressToolTest {

    @Mock
    private GoalService goalService;

    private AddGoalProgressTool tool;

    @BeforeEach
    void setUp() {
        tool = new AddGoalProgressTool(goalService);
    }

    @Test
    @DisplayName("Should add progress successfully")
    void addProgress_success() {
        AddGoalProgressParams params = new AddGoalProgressParams();
        params.setGoalId(1L);
        params.setAmount(new BigDecimal("5000"));
        params.setRecordDate("2025-01-15");
        params.setNote("月度存款");

        GoalProgressDto dto = GoalProgressDto.builder().id(1L).goalId(1L).amount(new BigDecimal("5000")).build();
        when(goalService.addProgress(eq(1L), eq(1L), any(AddProgressRequest.class))).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should fail with invalid record date")
    void addProgress_invalidDate() {
        AddGoalProgressParams params = new AddGoalProgressParams();
        params.setGoalId(1L);
        params.setAmount(new BigDecimal("5000"));
        params.setRecordDate("bad-date");

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("recordDate 格式错误");
    }

    @Test
    @DisplayName("Should evaluate risk as HIGH when amount exceeds threshold")
    void evaluateRisk_highAmount() {
        AddGoalProgressParams params = new AddGoalProgressParams();
        params.setAmount(new BigDecimal("50000"));

        RiskLevel risk = tool.evaluateRisk(params, new BigDecimal("10000"));

        assertThat(risk).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    @DisplayName("Should evaluate risk as MEDIUM when amount within threshold")
    void evaluateRisk_normalAmount() {
        AddGoalProgressParams params = new AddGoalProgressParams();
        params.setAmount(new BigDecimal("5000"));

        RiskLevel risk = tool.evaluateRisk(params, new BigDecimal("10000"));

        assertThat(risk).isEqualTo(RiskLevel.MEDIUM);
    }
}
