package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.DeleteGoalParams;
import com.finance.planner.goal.service.GoalService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "delete_goal", description = "删除理财目标", riskLevel = RiskLevel.HIGH)
public class DeleteGoalTool extends AbstractTool<DeleteGoalParams> {

    private final GoalService goalService;

    public DeleteGoalTool(GoalService goalService) {
        super(DeleteGoalParams.class);
        this.goalService = goalService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, DeleteGoalParams params) {
        goalService.deleteGoal(userId, params.getGoalId());
        return ToolResult.success("目标已成功删除");
    }
}
