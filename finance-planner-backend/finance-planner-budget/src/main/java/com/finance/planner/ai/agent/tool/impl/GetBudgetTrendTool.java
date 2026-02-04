package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.BudgetTrendParams;
import com.finance.planner.budget.service.BudgetService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_budget_trend", description = "获取预算趋势数据", riskLevel = RiskLevel.LOW)
public class GetBudgetTrendTool extends AbstractTool<BudgetTrendParams> {

    private final BudgetService budgetService;

    public GetBudgetTrendTool(BudgetService budgetService) {
        super(BudgetTrendParams.class);
        this.budgetService = budgetService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, BudgetTrendParams params) {
        int months = params.getMonths() == null ? 6 : params.getMonths();
        return ToolResult.success(budgetService.getBudgetTrend(userId, months));
    }
}
