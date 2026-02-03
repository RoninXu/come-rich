package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.DeleteTransactionParams;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "delete_transaction", description = "删除一条交易记录", riskLevel = RiskLevel.HIGH)
public class DeleteTransactionTool extends AbstractTool<DeleteTransactionParams> {

    private final TransactionService transactionService;

    public DeleteTransactionTool(TransactionService transactionService) {
        super(DeleteTransactionParams.class);
        this.transactionService = transactionService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, DeleteTransactionParams params) {
        transactionService.deleteTransaction(userId, params.getTransactionId());
        return ToolResult.success("已删除交易记录");
    }
}
