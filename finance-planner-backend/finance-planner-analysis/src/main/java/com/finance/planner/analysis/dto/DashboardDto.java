package com.finance.planner.analysis.dto;

import com.finance.planner.accounting.dto.TransactionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    /**
     * Current month summary
     */
    private MonthlySummaryDto currentMonth;

    /**
     * Health score summary (just the total score and grade)
     */
    private int healthScore;
    private String healthGrade;

    /**
     * Recent transactions (last 10)
     */
    private List<TransactionDto> recentTransactions;

    /**
     * Month-over-month comparison
     */
    private BigDecimal incomeChange;   // percentage change from last month
    private BigDecimal expenseChange;  // percentage change from last month
}
