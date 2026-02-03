package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SetBudgetParams {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String yearMonth;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
