package com.finance.planner.budget.service;

import com.finance.planner.budget.dto.*;

import java.util.List;

public interface BudgetService {

    BudgetDto setBudget(Long userId, SetBudgetRequest request);

    List<BudgetDto> getBudgets(Long userId, String yearMonth);

    void deleteBudget(Long userId, Long categoryId, String yearMonth);

    BudgetTotalDto setBudgetTotal(Long userId, SetBudgetTotalRequest request);

    BudgetTotalDto getBudgetTotal(Long userId, String yearMonth);

    BudgetSummaryDto getBudgetSummary(Long userId, String yearMonth);

    List<BudgetSummaryDto> getBudgetTrend(Long userId, int months);

    List<BudgetDto> copyBudgetFromPreviousMonth(Long userId, String targetMonth);
}
