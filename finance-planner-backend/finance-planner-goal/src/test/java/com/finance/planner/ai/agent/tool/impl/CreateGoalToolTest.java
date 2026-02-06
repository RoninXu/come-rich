package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.CreateGoalParams;
import com.finance.planner.goal.dto.CreateGoalRequest;
import com.finance.planner.goal.dto.GoalDto;
import com.finance.planner.goal.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateGoalToolTest {

    @Mock
    private GoalService goalService;

    private CreateGoalTool tool;

    @BeforeEach
    void setUp() {
        tool = new CreateGoalTool(goalService);
    }

    @Test
    @DisplayName("Should create goal with valid date")
    void createGoal_success() {
        CreateGoalParams params = new CreateGoalParams();
        params.setTitle("存10万买车");
        params.setTargetAmount(new BigDecimal("100000"));
        params.setDeadline("2025-12-31");
        params.setPriority((short) 1);

        GoalDto dto = GoalDto.builder().id(1L).title("存10万买车").build();
        when(goalService.createGoal(eq(1L), any(CreateGoalRequest.class))).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);

        ArgumentCaptor<CreateGoalRequest> captor = ArgumentCaptor.forClass(CreateGoalRequest.class);
        verify(goalService).createGoal(eq(1L), captor.capture());
        assertThat(captor.getValue().getDeadline()).isEqualTo(LocalDate.of(2025, 12, 31));
        assertThat(captor.getValue().getPriority()).isEqualTo((short) 1);
    }

    @Test
    @DisplayName("Should use default priority when not provided")
    void createGoal_defaultPriority() {
        CreateGoalParams params = new CreateGoalParams();
        params.setTitle("存款目标");
        params.setTargetAmount(new BigDecimal("50000"));
        params.setDeadline("2025-06-30");

        when(goalService.createGoal(eq(1L), any(CreateGoalRequest.class))).thenReturn(GoalDto.builder().build());

        tool.execute(1L, params);

        ArgumentCaptor<CreateGoalRequest> captor = ArgumentCaptor.forClass(CreateGoalRequest.class);
        verify(goalService).createGoal(eq(1L), captor.capture());
        assertThat(captor.getValue().getPriority()).isEqualTo((short) 2);
    }

    @Test
    @DisplayName("Should fail with invalid date format")
    void createGoal_invalidDate() {
        CreateGoalParams params = new CreateGoalParams();
        params.setTitle("测试目标");
        params.setTargetAmount(new BigDecimal("10000"));
        params.setDeadline("invalid-date");

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("deadline 格式错误");
    }
}
