package com.finance.planner.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryDto {

    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal savingsRate; // percentage (0-100)
    private int transactionCount;

    /**
     * Calculate savings rate
     */
    public void calculateSavingsRate() {
        if (totalIncome != null && totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savings = totalIncome.subtract(totalExpense != null ? totalExpense : BigDecimal.ZERO);
            this.savingsRate = savings.multiply(BigDecimal.valueOf(100))
                    .divide(totalIncome, 2, RoundingMode.HALF_UP);
        } else {
            this.savingsRate = BigDecimal.ZERO;
        }
    }

    /**
     * Create empty summary for a month with no data
     */
    public static MonthlySummaryDto empty(int year, int month) {
        return MonthlySummaryDto.builder()
                .year(year)
                .month(month)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .savingsRate(BigDecimal.ZERO)
                .transactionCount(0)
                .build();
    }
}
