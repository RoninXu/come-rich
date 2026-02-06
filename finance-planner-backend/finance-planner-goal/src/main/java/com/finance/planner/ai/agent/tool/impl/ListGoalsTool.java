package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.ListGoalsParams;
import com.finance.planner.goal.service.GoalService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "list_goals", description = "查看理财目标列表，可按状态筛选（1=进行中，2=已完成，3=已放弃）", riskLevel = RiskLevel.LOW)
public class ListGoalsTool extends AbstractTool<ListGoalsParams> {

    private final GoalService goalService;

    public ListGoalsTool(GoalService goalService) {
        super(ListGoalsParams.class);
        this.goalService = goalService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, ListGoalsParams params) {
        return ToolResult.success(goalService.listGoals(userId, params.getStatus()));
    }
}
