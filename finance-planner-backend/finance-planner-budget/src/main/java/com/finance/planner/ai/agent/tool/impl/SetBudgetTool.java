package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.*;
import com.finance.planner.ai.agent.tool.params.SetBudgetParams;
import com.finance.planner.budget.dto.SetBudgetRequest;
import com.finance.planner.budget.service.BudgetService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@AgentTool(name = "set_budget", description = "设置或更新预算", riskLevel = RiskLevel.MEDIUM)
public class SetBudgetTool extends AbstractTool<SetBudgetParams> implements RiskAwareTool<SetBudgetParams> {

    private final BudgetService budgetService;

    public SetBudgetTool(BudgetService budgetService) {
        super(SetBudgetParams.class);
        this.budgetService = budgetService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, SetBudgetParams params) {
        SetBudgetRequest request = SetBudgetRequest.builder()
                .categoryId(params.getCategoryId())
                .yearMonth(params.getYearMonth())
                .amount(params.getAmount())
                .build();
        return ToolResult.success(budgetService.setBudget(userId, request));
    }

    @Override
    public RiskLevel evaluateRisk(SetBudgetParams params, BigDecimal threshold) {
        if (params.getAmount() != null && threshold != null && params.getAmount().compareTo(threshold) > 0) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.MEDIUM;
    }
}
