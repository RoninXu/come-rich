package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTransactionParams {

    @NotNull
    private Long transactionId;

    @DecimalMin("0.01")
    private BigDecimal amount;

    @Min(1)
    @Max(2)
    private Short type;

    private Long categoryId;

    @Size(max = 255)
    private String description;

    private String transactionDate;

    @Size(max = 50)
    private String paymentMethod;

    @Size(max = 100)
    private String merchant;
}
