package com.finance.planner.budget.service;

import com.finance.planner.budget.dto.BudgetAiSuggestionDto;

public interface BudgetAiService {

    BudgetAiSuggestionDto generateSuggestions(Long userId, String yearMonth);
}
