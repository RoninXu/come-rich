package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.DeleteGoalParams;
import com.finance.planner.goal.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeleteGoalToolTest {

    @Mock
    private GoalService goalService;

    private DeleteGoalTool tool;

    @BeforeEach
    void setUp() {
        tool = new DeleteGoalTool(goalService);
    }

    @Test
    @DisplayName("Should delete goal successfully")
    void deleteGoal_success() {
        DeleteGoalParams params = new DeleteGoalParams();
        params.setGoalId(1L);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("目标已成功删除");
        verify(goalService).deleteGoal(1L, 1L);
    }

    @Test
    @DisplayName("Should have HIGH risk level")
    void riskLevel() {
        assertThat(tool.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
    }
}
