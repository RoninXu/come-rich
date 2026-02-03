package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.BudgetsParams;
import com.finance.planner.budget.service.BudgetService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_budgets", description = "获取指定月份的预算列表", riskLevel = RiskLevel.LOW)
public class GetBudgetsTool extends AbstractTool<BudgetsParams> {

    private final BudgetService budgetService;

    public GetBudgetsTool(BudgetService budgetService) {
        super(BudgetsParams.class);
        this.budgetService = budgetService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, BudgetsParams params) {
        return ToolResult.success(budgetService.getBudgets(userId, params.getYearMonth()));
    }
}
