package com.finance.planner.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAiSuggestionDto {

    private String summary;
    private List<String> suggestions;
    private String riskWarning;
}
