package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteBudgetParams {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String yearMonth;
}
