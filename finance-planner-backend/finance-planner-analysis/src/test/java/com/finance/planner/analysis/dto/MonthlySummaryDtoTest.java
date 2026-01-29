package com.finance.planner.analysis.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MonthlySummaryDto Unit Tests")
class MonthlySummaryDtoTest {

    // ========== calculateSavingsRate ==========

    @Test
    @DisplayName("calculateSavingsRate - positive savings rate when income > expense")
    void calculateSavingsRate_positive() {
        // Given
        MonthlySummaryDto summary = MonthlySummaryDto.builder()
                .year(2025)
                .month(1)
                .totalIncome(new BigDecimal("10000.00"))
                .totalExpense(new BigDecimal("7000.00"))
                .balance(new BigDecimal("3000.00"))
                .transactionCount(10)
                .build();

        // When
        summary.calculateSavingsRate();

        // Then: (10000 - 7000) / 10000 * 100 = 30.00
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("calculateSavingsRate - returns zero when no income")
    void calculateSavingsRate_noIncome() {
        // Given
        MonthlySummaryDto summary = MonthlySummaryDto.builder()
                .year(2025)
                .month(2)
                .totalIncome(BigDecimal.ZERO)
                .totalExpense(new BigDecimal("500.00"))
                .balance(new BigDecimal("-500.00"))
                .transactionCount(5)
                .build();

        // When
        summary.calculateSavingsRate();

        // Then
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ========== empty ==========

    @Test
    @DisplayName("empty - returns summary with all zero values")
    void empty_returnsZeroValues() {
        // When
        MonthlySummaryDto result = MonthlySummaryDto.empty(2025, 6);

        // Then
        assertThat(result.getYear()).isEqualTo(2025);
        assertThat(result.getMonth()).isEqualTo(6);
        assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getSavingsRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTransactionCount()).isEqualTo(0);
    }

    // ========== builder ==========

    @Test
    @DisplayName("builder - sets all fields correctly")
    void builder_setsAllFields() {
        // When
        MonthlySummaryDto summary = MonthlySummaryDto.builder()
                .year(2025)
                .month(3)
                .totalIncome(new BigDecimal("15000.00"))
                .totalExpense(new BigDecimal("8000.00"))
                .balance(new BigDecimal("7000.00"))
                .savingsRate(new BigDecimal("46.67"))
                .transactionCount(25)
                .build();

        // Then
        assertThat(summary.getYear()).isEqualTo(2025);
        assertThat(summary.getMonth()).isEqualTo(3);
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(summary.getBalance()).isEqualByComparingTo(new BigDecimal("7000.00"));
        assertThat(summary.getSavingsRate()).isEqualByComparingTo(new BigDecimal("46.67"));
        assertThat(summary.getTransactionCount()).isEqualTo(25);
    }
}
