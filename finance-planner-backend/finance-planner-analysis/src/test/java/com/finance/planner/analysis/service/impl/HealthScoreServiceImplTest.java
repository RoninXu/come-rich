package com.finance.planner.analysis.service.impl;

import com.finance.planner.analysis.dto.HealthScoreDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import com.finance.planner.analysis.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthScoreServiceImpl Unit Tests")
class HealthScoreServiceImplTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private HealthScoreServiceImpl healthScoreService;

    private static final Long USER_ID = 1L;

    // ========== Helper methods ==========

    private MonthlySummaryDto createSummary(int year, int month, BigDecimal income,
                                             BigDecimal expense, int transactionCount) {
        BigDecimal balance = income.subtract(expense);
        MonthlySummaryDto summary = MonthlySummaryDto.builder()
                .year(year)
                .month(month)
                .totalIncome(income)
                .totalExpense(expense)
                .balance(balance)
                .transactionCount(transactionCount)
                .build();
        summary.calculateSavingsRate();
        return summary;
    }

    private MonthlySummaryDto emptySummary(int year, int month) {
        return MonthlySummaryDto.empty(year, month);
    }

    /**
     * Stub all 3 months of statisticsService.getMonthlySummary
     * for the current, previous, and two-months-ago months.
     */
    private void stubThreeMonths(MonthlySummaryDto current, MonthlySummaryDto lastMonth,
                                  MonthlySummaryDto twoMonthsAgo) {
        YearMonth now = YearMonth.now();
        when(statisticsService.getMonthlySummary(USER_ID, now.getYear(), now.getMonthValue()))
                .thenReturn(current);
        when(statisticsService.getMonthlySummary(USER_ID,
                now.minusMonths(1).getYear(), now.minusMonths(1).getMonthValue()))
                .thenReturn(lastMonth);
        when(statisticsService.getMonthlySummary(USER_ID,
                now.minusMonths(2).getYear(), now.minusMonths(2).getMonthValue()))
                .thenReturn(twoMonthsAgo);
    }

    // ========== calculateHealthScore - full score ==========

    @Test
    @DisplayName("calculateHealthScore - achieves high score with ideal financial data")
    void calculateHealthScore_fullScore() {
        // Given: Good savings rate (>=20%), low expense ratio (<=60%), many transactions,
        // growth vs last month, good habit over 3 months (>=30 total)
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 15);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("9000"), new BigDecimal("5500"), 12);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("8000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then
        assertThat(result.getTotalScore()).isGreaterThanOrEqualTo(80);
        assertThat(result.getGrade()).isIn("A", "B");
        assertThat(result.getSavingAbility()).isGreaterThan(0);
        assertThat(result.getBalanceRatio()).isGreaterThan(0);
        assertThat(result.getConsumptionStructure()).isGreaterThan(0);
        assertThat(result.getAssetGrowth()).isGreaterThan(0);
        assertThat(result.getRecordingHabit()).isGreaterThan(0);
        assertThat(result.getSuggestions()).isNotNull();
    }

    // ========== Saving Score ==========

    @Test
    @DisplayName("savingScore - full score when savings rate >= 20%")
    void savingScore_goodRate() {
        // Given: income 10000, expense 5000 => savings rate 50% >= 20%
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: savings rate = 50% >= 20%, so full 30 points
        assertThat(result.getSavingAbility()).isEqualTo(30);
        assertThat(result.getSavingDetail().getStatus()).isEqualTo("good");
    }

    @Test
    @DisplayName("savingScore - proportional score when savings rate between 0 and 20%")
    void savingScore_averageRate() {
        // Given: income 10000, expense 9000 => savings rate 10% (halfway to 20%)
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("9000"), 10);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: savings rate = 10%, score = 10 * 30 / 20 = 15
        assertThat(result.getSavingAbility()).isEqualTo(15);
        assertThat(result.getSavingDetail().getStatus()).isEqualTo("average");
    }

    @Test
    @DisplayName("savingScore - zero score when no income (savings rate 0)")
    void savingScore_zeroRate() {
        // Given: no income, so savings rate = 0
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = emptySummary(now.getYear(), now.getMonthValue());
        MonthlySummaryDto lastMonth = emptySummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue());
        MonthlySummaryDto twoMonthsAgo = emptySummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue());

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then
        assertThat(result.getSavingAbility()).isEqualTo(0);
        assertThat(result.getSavingDetail().getStatus()).isEqualTo("poor");
    }

    // ========== Balance Score ==========

    @Test
    @DisplayName("balanceScore - full score when expense/income ratio <= 0.6")
    void balanceScore_goodRatio() {
        // Given: income 10000, expense 5000 => ratio 0.5 <= 0.6
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: ratio 0.5 <= 0.6, so full 25 points
        assertThat(result.getBalanceRatio()).isEqualTo(25);
        assertThat(result.getBalanceDetail().getStatus()).isEqualTo("good");
    }

    @Test
    @DisplayName("balanceScore - 80% score when expense/income ratio between 0.6 and 0.8")
    void balanceScore_averageRatio() {
        // Given: income 10000, expense 7000 => ratio 0.7 (between 0.6 and 0.8)
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("7000"), 10);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: ratio 0.7, 0.6 < ratio <= 0.8, so 25 * 0.8 = 20
        assertThat(result.getBalanceRatio()).isEqualTo(20);
        assertThat(result.getBalanceDetail().getStatus()).isEqualTo("average");
    }

    @Test
    @DisplayName("balanceScore - zero score when expense exceeds income")
    void balanceScore_poorRatio() {
        // Given: income 5000, expense 6000 => ratio 1.2 > 1.0
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("5000"), new BigDecimal("6000"), 10);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: ratio > 1.0, so 0 points
        assertThat(result.getBalanceRatio()).isEqualTo(0);
        assertThat(result.getBalanceDetail().getStatus()).isEqualTo("poor");
    }

    @Test
    @DisplayName("balanceScore - zero score when no income")
    void balanceScore_noIncome() {
        // Given: no income
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = emptySummary(now.getYear(), now.getMonthValue());
        MonthlySummaryDto lastMonth = emptySummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue());
        MonthlySummaryDto twoMonthsAgo = emptySummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue());

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: no income, so 0 points
        assertThat(result.getBalanceRatio()).isEqualTo(0);
        assertThat(result.getBalanceDetail().getStatus()).isEqualTo("poor");
    }

    // ========== Consumption Score ==========

    @Test
    @DisplayName("consumptionScore - capped at 20 for many transactions")
    void consumptionScore_manyTransactions() {
        // Given: 25 transactions, capped at max 20
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 25);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: min(20, 25) = 20
        assertThat(result.getConsumptionStructure()).isEqualTo(20);
        assertThat(result.getConsumptionDetail().getStatus()).isEqualTo("good");
    }

    @Test
    @DisplayName("consumptionScore - proportional score for few transactions")
    void consumptionScore_fewTransactions() {
        // Given: 5 transactions
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 5);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: min(20, 5) = 5
        assertThat(result.getConsumptionStructure()).isEqualTo(5);
        assertThat(result.getConsumptionDetail().getStatus()).isEqualTo("average");
    }

    @Test
    @DisplayName("consumptionScore - zero score for no transactions")
    void consumptionScore_noTransactions() {
        // Given: 0 transactions in current month
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = emptySummary(now.getYear(), now.getMonthValue());
        MonthlySummaryDto lastMonth = emptySummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue());
        MonthlySummaryDto twoMonthsAgo = emptySummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue());

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then
        assertThat(result.getConsumptionStructure()).isEqualTo(0);
        assertThat(result.getConsumptionDetail().getStatus()).isEqualTo("poor");
    }

    // ========== Growth Score ==========

    @Test
    @DisplayName("growthScore - full score when current savings > previous savings")
    void growthScore_improvement() {
        // Given: current balance > last month balance
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10); // balance 5000
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("8000"), new BigDecimal("5000"), 10); // balance 3000
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("7000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: current (5000) > previous (3000), so full 15 points
        assertThat(result.getAssetGrowth()).isEqualTo(15);
        assertThat(result.getGrowthDetail().getStatus()).isEqualTo("good");
    }

    @Test
    @DisplayName("growthScore - partial score when savings declined but still positive")
    void growthScore_decline() {
        // Given: current balance < last month balance but still positive
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("8000"), 10); // balance 2000
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10); // balance 5000
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: current (2000) < previous (5000) but > 0, so 15 * 0.6 = 9
        assertThat(result.getAssetGrowth()).isEqualTo(9);
        assertThat(result.getGrowthDetail().getStatus()).isEqualTo("average");
    }

    // ========== Habit Score ==========

    @Test
    @DisplayName("habitScore - full score when total transactions >= 30 over 3 months")
    void habitScore_goodHabit() {
        // Given: 15 + 10 + 10 = 35 >= 30
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 15);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 10);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: 35 >= 30, so full 10 points
        assertThat(result.getRecordingHabit()).isEqualTo(10);
        assertThat(result.getHabitDetail().getStatus()).isEqualTo("good");
    }

    @Test
    @DisplayName("habitScore - partial score when total transactions < 15")
    void habitScore_poorHabit() {
        // Given: 3 + 2 + 1 = 6 (>0 but <15)
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = createSummary(now.getYear(), now.getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 3);
        MonthlySummaryDto lastMonth = createSummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 2);
        MonthlySummaryDto twoMonthsAgo = createSummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue(),
                new BigDecimal("10000"), new BigDecimal("5000"), 1);

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then: 6 > 0 but < 15, so 10 * 0.3 = 3
        assertThat(result.getRecordingHabit()).isEqualTo(3);
        assertThat(result.getHabitDetail().getStatus()).isEqualTo("average");
    }

    @Test
    @DisplayName("habitScore - zero score when no records over 3 months")
    void habitScore_noRecords() {
        // Given: 0 + 0 + 0 = 0
        YearMonth now = YearMonth.now();
        MonthlySummaryDto current = emptySummary(now.getYear(), now.getMonthValue());
        MonthlySummaryDto lastMonth = emptySummary(now.minusMonths(1).getYear(),
                now.minusMonths(1).getMonthValue());
        MonthlySummaryDto twoMonthsAgo = emptySummary(now.minusMonths(2).getYear(),
                now.minusMonths(2).getMonthValue());

        stubThreeMonths(current, lastMonth, twoMonthsAgo);

        // When
        HealthScoreDto result = healthScoreService.calculateHealthScore(USER_ID);

        // Then
        assertThat(result.getRecordingHabit()).isEqualTo(0);
        assertThat(result.getHabitDetail().getStatus()).isEqualTo("poor");
    }
}
