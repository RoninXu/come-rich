package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BudgetSummaryParams {

    @NotBlank
    private String yearMonth;
}
