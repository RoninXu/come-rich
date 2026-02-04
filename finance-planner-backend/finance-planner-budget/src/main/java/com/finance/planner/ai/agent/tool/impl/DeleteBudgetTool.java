package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.DeleteBudgetParams;
import com.finance.planner.budget.service.BudgetService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "delete_budget", description = "删除预算设置", riskLevel = RiskLevel.HIGH)
public class DeleteBudgetTool extends AbstractTool<DeleteBudgetParams> {

    private final BudgetService budgetService;

    public DeleteBudgetTool(BudgetService budgetService) {
        super(DeleteBudgetParams.class);
        this.budgetService = budgetService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, DeleteBudgetParams params) {
        budgetService.deleteBudget(userId, params.getCategoryId(), params.getYearMonth());
        return ToolResult.success("已删除预算设置");
    }
}
