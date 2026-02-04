package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.BudgetAiSuggestionsParams;
import com.finance.planner.budget.service.BudgetAiService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_budget_ai_suggestions", description = "获取预算AI优化建议", riskLevel = RiskLevel.LOW)
public class GetBudgetAiSuggestionsTool extends AbstractTool<BudgetAiSuggestionsParams> {

    private final BudgetAiService budgetAiService;

    public GetBudgetAiSuggestionsTool(BudgetAiService budgetAiService) {
        super(BudgetAiSuggestionsParams.class);
        this.budgetAiService = budgetAiService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, BudgetAiSuggestionsParams params) {
        return ToolResult.success(budgetAiService.generateSuggestions(userId, params.getYearMonth()));
    }
}
