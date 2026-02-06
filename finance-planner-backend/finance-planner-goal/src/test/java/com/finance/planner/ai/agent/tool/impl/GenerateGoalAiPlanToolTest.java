package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GenerateGoalAiPlanParams;
import com.finance.planner.goal.dto.GoalAiPlanDto;
import com.finance.planner.goal.service.GoalAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateGoalAiPlanToolTest {

    @Mock
    private GoalAiService goalAiService;

    private GenerateGoalAiPlanTool tool;

    @BeforeEach
    void setUp() {
        tool = new GenerateGoalAiPlanTool(goalAiService);
    }

    @Test
    @DisplayName("Should generate AI plan successfully")
    void generateAiPlan_success() {
        GenerateGoalAiPlanParams params = new GenerateGoalAiPlanParams();
        params.setGoalId(1L);

        GoalAiPlanDto planDto = GoalAiPlanDto.builder()
                .summary("每月存5000元，12个月达成目标")
                .steps(List.of("第一步：减少不必要开支", "第二步：增加收入来源"))
                .tips(List.of("自动扣款设置"))
                .riskWarning("此计划仅供参考")
                .build();
        when(goalAiService.generateAiPlan(1L, 1L)).thenReturn(planDto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(planDto);
    }

    @Test
    @DisplayName("Should have LOW risk level")
    void riskLevel() {
        assertThat(tool.getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }
}
