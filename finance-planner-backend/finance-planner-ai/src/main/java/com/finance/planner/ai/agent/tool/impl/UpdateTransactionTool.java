package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.accounting.dto.UpdateTransactionRequest;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.ai.agent.tool.*;
import com.finance.planner.ai.agent.tool.params.UpdateTransactionParams;
import com.finance.planner.ai.agent.util.DateParser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@AgentTool(name = "update_transaction", description = "更新一条交易记录", riskLevel = RiskLevel.MEDIUM)
public class UpdateTransactionTool extends AbstractTool<UpdateTransactionParams> implements RiskAwareTool<UpdateTransactionParams> {

    private final TransactionService transactionService;

    public UpdateTransactionTool(TransactionService transactionService) {
        super(UpdateTransactionParams.class);
        this.transactionService = transactionService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, UpdateTransactionParams params) {
        LocalDate transactionDate = null;
        if (params.getTransactionDate() != null && !params.getTransactionDate().isBlank()) {
            try {
                transactionDate = DateParser.parseUserDate(params.getTransactionDate());
            } catch (DateTimeParseException e) {
                return ToolResult.failure("transactionDate 格式错误，请使用 yyyy-MM-dd 或相对日期（今天/昨天/明天/前天/后天）");
            }
        }

        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .amount(params.getAmount())
                .type(params.getType())
                .categoryId(params.getCategoryId())
                .description(params.getDescription())
                .transactionDate(transactionDate)
                .paymentMethod(params.getPaymentMethod())
                .merchant(params.getMerchant())
                .build();
        return ToolResult.success(transactionService.updateTransaction(userId, params.getTransactionId(), request));
    }

    @Override
    public RiskLevel evaluateRisk(UpdateTransactionParams params, BigDecimal threshold) {
        if (params.getAmount() != null && threshold != null && params.getAmount().compareTo(threshold) > 0) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.MEDIUM;
    }
}
