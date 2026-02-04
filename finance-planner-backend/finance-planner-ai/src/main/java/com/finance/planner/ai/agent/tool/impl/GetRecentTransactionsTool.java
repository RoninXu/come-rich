package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetRecentTransactionsParams;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_recent_transactions", description = "获取最近交易记录", riskLevel = RiskLevel.LOW)
public class GetRecentTransactionsTool extends AbstractTool<GetRecentTransactionsParams> {

    private final TransactionService transactionService;

    public GetRecentTransactionsTool(TransactionService transactionService) {
        super(GetRecentTransactionsParams.class);
        this.transactionService = transactionService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetRecentTransactionsParams params) {
        return ToolResult.success(transactionService.getRecentTransactions(userId));
    }
}
