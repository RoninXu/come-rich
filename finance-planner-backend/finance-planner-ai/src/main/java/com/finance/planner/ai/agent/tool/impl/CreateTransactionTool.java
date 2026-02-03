package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.accounting.dto.CreateTransactionRequest;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.agent.tool.*;
import com.finance.planner.ai.agent.tool.params.CreateTransactionParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@AgentTool(name = "create_transaction", description = "创建一条交易记录", riskLevel = RiskLevel.MEDIUM)
public class CreateTransactionTool extends AbstractTool<CreateTransactionParams> implements RiskAwareTool<CreateTransactionParams> {

    private final TransactionService transactionService;

    public CreateTransactionTool(TransactionService transactionService) {
        super(CreateTransactionParams.class);
        this.transactionService = transactionService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, CreateTransactionParams params) {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(params.getAmount())
                .type(params.getType())
                .categoryId(params.getCategoryId())
                .description(params.getDescription())
                .transactionDate(LocalDate.parse(params.getTransactionDate()))
                .paymentMethod(params.getPaymentMethod())
                .merchant(params.getMerchant())
                .build();
        return ToolResult.success(transactionService.createTransaction(userId, request));
    }

    @Override
    public RiskLevel evaluateRisk(CreateTransactionParams params, BigDecimal threshold) {
        if (params.getAmount() != null && threshold != null && params.getAmount().compareTo(threshold) > 0) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.MEDIUM;
    }
}
