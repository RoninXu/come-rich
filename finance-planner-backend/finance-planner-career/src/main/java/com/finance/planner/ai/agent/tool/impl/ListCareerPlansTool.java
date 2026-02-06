package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.ListCareerPlansParams;
import com.finance.planner.career.service.CareerPlanService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "list_career_plans", description = "查看用户的副业计划列表", riskLevel = RiskLevel.LOW)
public class ListCareerPlansTool extends AbstractTool<ListCareerPlansParams> {

    private final CareerPlanService careerPlanService;

    public ListCareerPlansTool(CareerPlanService careerPlanService) {
        super(ListCareerPlansParams.class);
        this.careerPlanService = careerPlanService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, ListCareerPlansParams params) {
        return ToolResult.success(careerPlanService.listPlans(userId));
    }
}
