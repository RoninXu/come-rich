package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BudgetsParams {

    @NotBlank
    private String yearMonth;
}
