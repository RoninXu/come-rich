package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.BudgetSummaryParams;
import com.finance.planner.budget.service.BudgetService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_budget_summary", description = "获取预算使用汇总", riskLevel = RiskLevel.LOW)
public class GetBudgetSummaryTool extends AbstractTool<BudgetSummaryParams> {

    private final BudgetService budgetService;

    public GetBudgetSummaryTool(BudgetService budgetService) {
        super(BudgetSummaryParams.class);
        this.budgetService = budgetService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, BudgetSummaryParams params) {
        return ToolResult.success(budgetService.getBudgetSummary(userId, params.getYearMonth()));
    }
}
