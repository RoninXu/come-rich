package com.finance.planner.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetComparisonDto {

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private BigDecimal remainingAmount;
    private BigDecimal utilizationPercentage;
    private boolean overBudget;
}
