package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.ListGoalsParams;
import com.finance.planner.goal.dto.GoalDto;
import com.finance.planner.goal.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListGoalsToolTest {

    @Mock
    private GoalService goalService;

    private ListGoalsTool tool;

    @BeforeEach
    void setUp() {
        tool = new ListGoalsTool(goalService);
    }

    @Test
    @DisplayName("Should return all goals when no status filter")
    void listGoals_noFilter() {
        ListGoalsParams params = new ListGoalsParams();
        List<GoalDto> goals = List.of(GoalDto.builder().id(1L).title("存10万").build());
        when(goalService.listGoals(1L, null)).thenReturn(goals);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(goals);
        verify(goalService).listGoals(1L, null);
    }

    @Test
    @DisplayName("Should filter goals by status")
    void listGoals_withStatusFilter() {
        ListGoalsParams params = new ListGoalsParams();
        params.setStatus((short) 1);
        when(goalService.listGoals(1L, (short) 1)).thenReturn(Collections.emptyList());

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        verify(goalService).listGoals(1L, (short) 1);
    }

    @Test
    @DisplayName("Should return empty list when no goals exist")
    void listGoals_empty() {
        ListGoalsParams params = new ListGoalsParams();
        when(goalService.listGoals(1L, null)).thenReturn(Collections.emptyList());

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(Collections.emptyList());
    }

    @Test
    @DisplayName("Should have LOW risk level")
    void riskLevel() {
        assertThat(tool.getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }
}
