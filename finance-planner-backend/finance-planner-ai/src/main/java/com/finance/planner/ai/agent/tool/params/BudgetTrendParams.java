package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BudgetTrendParams {

    @Min(1)
    @Max(24)
    private Integer months = 6;
}
