package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GenerateGoalAiPlanParams;
import com.finance.planner.goal.service.GoalAiService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "generate_goal_ai_plan", description = "为理财目标生成AI储蓄计划", riskLevel = RiskLevel.LOW)
public class GenerateGoalAiPlanTool extends AbstractTool<GenerateGoalAiPlanParams> {

    private final GoalAiService goalAiService;

    public GenerateGoalAiPlanTool(GoalAiService goalAiService) {
        super(GenerateGoalAiPlanParams.class);
        this.goalAiService = goalAiService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GenerateGoalAiPlanParams params) {
        return ToolResult.success(goalAiService.generateAiPlan(userId, params.getGoalId()));
    }
}
