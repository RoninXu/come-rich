package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BudgetAiSuggestionsParams {

    @NotBlank
    private String yearMonth;
}
