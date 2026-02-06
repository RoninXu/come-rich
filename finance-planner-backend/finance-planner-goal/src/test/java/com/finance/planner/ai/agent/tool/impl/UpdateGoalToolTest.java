package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.UpdateGoalParams;
import com.finance.planner.goal.dto.GoalDto;
import com.finance.planner.goal.dto.UpdateGoalRequest;
import com.finance.planner.goal.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateGoalToolTest {

    @Mock
    private GoalService goalService;

    private UpdateGoalTool tool;

    @BeforeEach
    void setUp() {
        tool = new UpdateGoalTool(goalService);
    }

    @Test
    @DisplayName("Should update goal with partial fields")
    void updateGoal_partialUpdate() {
        UpdateGoalParams params = new UpdateGoalParams();
        params.setGoalId(1L);
        params.setTitle("新标题");

        GoalDto dto = GoalDto.builder().id(1L).title("新标题").build();
        when(goalService.updateGoal(eq(1L), eq(1L), any(UpdateGoalRequest.class))).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<UpdateGoalRequest> captor = ArgumentCaptor.forClass(UpdateGoalRequest.class);
        verify(goalService).updateGoal(eq(1L), eq(1L), captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("新标题");
        assertThat(captor.getValue().getDeadline()).isNull();
    }

    @Test
    @DisplayName("Should update goal with new deadline")
    void updateGoal_withDeadline() {
        UpdateGoalParams params = new UpdateGoalParams();
        params.setGoalId(1L);
        params.setDeadline("2026-01-01");
        params.setTargetAmount(new BigDecimal("200000"));

        when(goalService.updateGoal(eq(1L), eq(1L), any(UpdateGoalRequest.class)))
                .thenReturn(GoalDto.builder().id(1L).build());

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Should fail with invalid deadline format")
    void updateGoal_invalidDeadline() {
        UpdateGoalParams params = new UpdateGoalParams();
        params.setGoalId(1L);
        params.setDeadline("bad-date");

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("deadline 格式错误");
    }
}
