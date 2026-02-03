package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.ListTransactionsParams;
import com.finance.planner.common.response.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@AgentTool(name = "list_transactions", description = "查询交易记录列表（支持类型和日期过滤）", riskLevel = RiskLevel.LOW)
public class ListTransactionsTool extends AbstractTool<ListTransactionsParams> {

    private final TransactionService transactionService;

    public ListTransactionsTool(TransactionService transactionService) {
        super(ListTransactionsParams.class);
        this.transactionService = transactionService;
    }

    @Override
    public ToolResult executeInternal(Long userId, ListTransactionsParams params) {
        LocalDate startDate = (params.getStartDate() != null && !params.getStartDate().isBlank())
                ? LocalDate.parse(params.getStartDate())
                : null;
        LocalDate endDate = (params.getEndDate() != null && !params.getEndDate().isBlank())
                ? LocalDate.parse(params.getEndDate())
                : null;
        int page = params.getPage() == null ? 1 : params.getPage();
        int pageSize = params.getPageSize() == null ? 20 : params.getPageSize();
        PageResponse<?> result = transactionService.listTransactions(
                userId,
                params.getType(),
                startDate,
                endDate,
                PageRequest.of(Math.max(0, page - 1), pageSize)
        );
        return ToolResult.success(result);
    }
}
