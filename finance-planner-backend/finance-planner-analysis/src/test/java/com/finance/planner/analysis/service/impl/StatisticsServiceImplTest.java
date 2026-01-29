package com.finance.planner.analysis.service.impl;

import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import com.finance.planner.analysis.dto.CategoryStatDto;
import com.finance.planner.analysis.dto.DailyStatDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatisticsServiceImpl Unit Tests")
class StatisticsServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private static final Long USER_ID = 1L;

    // ========== Helper methods ==========

    private Transaction createTransaction(Long id, BigDecimal amount, Short type,
                                           Long categoryId, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUserId(USER_ID);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategoryId(categoryId);
        transaction.setDescription("Test");
        transaction.setTransactionDate(date);
        transaction.setIsDeleted(false);
        return transaction;
    }

    private Category createCategory(Long id, String name, Short type) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setType(type);
        category.setIcon("icon-" + name.toLowerCase());
        category.setColor("#FF0000");
        return category;
    }

    // ========== getMonthlySummary ==========

    @Test
    @DisplayName("getMonthlySummary - calculates totals with mixed transactions")
    void getMonthlySummary_withTransactions() {
        // Given
        int year = 2025, month = 1;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        Transaction income1 = createTransaction(1L, new BigDecimal("5000.00"), (short) 1, 10L, LocalDate.of(2025, 1, 5));
        Transaction income2 = createTransaction(2L, new BigDecimal("3000.00"), (short) 1, 10L, LocalDate.of(2025, 1, 15));
        Transaction expense1 = createTransaction(3L, new BigDecimal("1000.00"), (short) 2, 20L, LocalDate.of(2025, 1, 10));
        Transaction expense2 = createTransaction(4L, new BigDecimal("2000.00"), (short) 2, 20L, LocalDate.of(2025, 1, 20));

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(income1, income2, expense1, expense2));

        // When
        MonthlySummaryDto result = statisticsService.getMonthlySummary(USER_ID, year, month);

        // Then
        assertThat(result.getYear()).isEqualTo(year);
        assertThat(result.getMonth()).isEqualTo(month);
        assertThat(result.getTotalIncome()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(result.getTotalExpense()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getTransactionCount()).isEqualTo(4);
        assertThat(result.getSavingsRate()).isNotNull();
    }

    @Test
    @DisplayName("getMonthlySummary - returns empty summary when no transactions")
    void getMonthlySummary_empty() {
        // Given
        int year = 2025, month = 2;
        LocalDate start = LocalDate.of(2025, 2, 1);
        LocalDate end = LocalDate.of(2025, 2, 28);

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(Collections.emptyList());

        // When
        MonthlySummaryDto result = statisticsService.getMonthlySummary(USER_ID, year, month);

        // Then
        assertThat(result.getYear()).isEqualTo(year);
        assertThat(result.getMonth()).isEqualTo(month);
        assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTransactionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("getMonthlySummary - income only results in positive balance")
    void getMonthlySummary_incomeOnly() {
        // Given
        int year = 2025, month = 3;
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        Transaction income = createTransaction(1L, new BigDecimal("10000.00"), (short) 1, 10L, LocalDate.of(2025, 3, 1));

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(income));

        // When
        MonthlySummaryDto result = statisticsService.getMonthlySummary(USER_ID, year, month);

        // Then
        assertThat(result.getTotalIncome()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(result.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(result.getTransactionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getMonthlySummary - expense only results in negative balance")
    void getMonthlySummary_expenseOnly() {
        // Given
        int year = 2025, month = 4;
        LocalDate start = LocalDate.of(2025, 4, 1);
        LocalDate end = LocalDate.of(2025, 4, 30);

        Transaction expense = createTransaction(1L, new BigDecimal("5000.00"), (short) 2, 20L, LocalDate.of(2025, 4, 10));

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(expense));

        // When
        MonthlySummaryDto result = statisticsService.getMonthlySummary(USER_ID, year, month);

        // Then
        assertThat(result.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalExpense()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("-5000.00"));
    }

    @Test
    @DisplayName("getMonthlySummary - calculates correct balance as income minus expense")
    void getMonthlySummary_calculatesBalance() {
        // Given
        int year = 2025, month = 5;
        LocalDate start = LocalDate.of(2025, 5, 1);
        LocalDate end = LocalDate.of(2025, 5, 31);

        Transaction income = createTransaction(1L, new BigDecimal("8000.00"), (short) 1, 10L, LocalDate.of(2025, 5, 1));
        Transaction expense = createTransaction(2L, new BigDecimal("6000.00"), (short) 2, 20L, LocalDate.of(2025, 5, 15));

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(income, expense));

        // When
        MonthlySummaryDto result = statisticsService.getMonthlySummary(USER_ID, year, month);

        // Then
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("2000.00"));
        // Savings rate should be (2000/8000)*100 = 25.00
        assertThat(result.getSavingsRate()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    // ========== getCategoryStats ==========

    @Test
    @DisplayName("getCategoryStats - with type filter groups by category")
    void getCategoryStats_withType() {
        // Given
        int year = 2025, month = 1;
        Short expenseType = 2;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        Transaction t1 = createTransaction(1L, new BigDecimal("500.00"), expenseType, 20L, LocalDate.of(2025, 1, 5));
        Transaction t2 = createTransaction(2L, new BigDecimal("300.00"), expenseType, 20L, LocalDate.of(2025, 1, 10));
        Transaction t3 = createTransaction(3L, new BigDecimal("200.00"), expenseType, 30L, LocalDate.of(2025, 1, 15));

        Category foodCategory = createCategory(20L, "Food", expenseType);
        Category transportCategory = createCategory(30L, "Transport", expenseType);

        when(transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, expenseType, start, end))
                .thenReturn(List.of(t1, t2, t3));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(foodCategory, transportCategory));

        // When
        List<CategoryStatDto> result = statisticsService.getCategoryStats(USER_ID, year, month, expenseType);

        // Then
        assertThat(result).hasSize(2);
        // Sorted by amount descending: Food (800) > Transport (200)
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(result.get(0).getTransactionCount()).isEqualTo(2);
        assertThat(result.get(1).getCategoryName()).isEqualTo("Transport");
        assertThat(result.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(result.get(1).getTransactionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getCategoryStats - without type filter fetches all transactions")
    void getCategoryStats_withoutType() {
        // Given
        int year = 2025, month = 1;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        Transaction income = createTransaction(1L, new BigDecimal("5000.00"), (short) 1, 10L, LocalDate.of(2025, 1, 5));
        Transaction expense = createTransaction(2L, new BigDecimal("2000.00"), (short) 2, 20L, LocalDate.of(2025, 1, 10));

        Category salaryCategory = createCategory(10L, "Salary", (short) 1);
        Category foodCategory = createCategory(20L, "Food", (short) 2);

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(income, expense));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(salaryCategory, foodCategory));

        // When
        List<CategoryStatDto> result = statisticsService.getCategoryStats(USER_ID, year, month, null);

        // Then
        assertThat(result).hasSize(2);
        // Sorted by amount descending: Salary (5000) > Food (2000)
        assertThat(result.get(0).getCategoryName()).isEqualTo("Salary");
        assertThat(result.get(1).getCategoryName()).isEqualTo("Food");
        verify(transactionRepository).findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end);
    }

    @Test
    @DisplayName("getCategoryStats - returns empty list when no transactions")
    void getCategoryStats_empty() {
        // Given
        int year = 2025, month = 6;
        Short expenseType = 2;
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end = LocalDate.of(2025, 6, 30);

        when(transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, expenseType, start, end))
                .thenReturn(Collections.emptyList());

        // When
        List<CategoryStatDto> result = statisticsService.getCategoryStats(USER_ID, year, month, expenseType);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("getCategoryStats - calculates correct percentages")
    void getCategoryStats_calculatesPercentages() {
        // Given
        int year = 2025, month = 1;
        Short expenseType = 2;
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        // 750 + 250 = 1000 total => Food=75%, Transport=25%
        Transaction t1 = createTransaction(1L, new BigDecimal("750.00"), expenseType, 20L, LocalDate.of(2025, 1, 5));
        Transaction t2 = createTransaction(2L, new BigDecimal("250.00"), expenseType, 30L, LocalDate.of(2025, 1, 10));

        Category foodCategory = createCategory(20L, "Food", expenseType);
        Category transportCategory = createCategory(30L, "Transport", expenseType);

        when(transactionRepository.findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, expenseType, start, end))
                .thenReturn(List.of(t1, t2));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(foodCategory, transportCategory));

        // When
        List<CategoryStatDto> result = statisticsService.getCategoryStats(USER_ID, year, month, expenseType);

        // Then
        assertThat(result).hasSize(2);
        // Food: 750/1000 * 100 = 75.00%
        assertThat(result.get(0).getPercentage()).isEqualByComparingTo(new BigDecimal("75.00"));
        // Transport: 250/1000 * 100 = 25.00%
        assertThat(result.get(1).getPercentage()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    // ========== getDailyStats ==========

    @Test
    @DisplayName("getDailyStats - populates all days of the month")
    void getDailyStats_populatesAllDays() {
        // Given
        int year = 2025, month = 1; // January has 31 days
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        Transaction t1 = createTransaction(1L, new BigDecimal("1000.00"), (short) 1, 10L, LocalDate.of(2025, 1, 5));
        Transaction t2 = createTransaction(2L, new BigDecimal("500.00"), (short) 2, 20L, LocalDate.of(2025, 1, 5));

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(t1, t2));

        // When
        List<DailyStatDto> result = statisticsService.getDailyStats(USER_ID, year, month);

        // Then
        assertThat(result).hasSize(31); // January has 31 days

        // Day 5 should have income and expense
        DailyStatDto day5 = result.get(4); // index 4 = day 5
        assertThat(day5.getDate()).isEqualTo(LocalDate.of(2025, 1, 5));
        assertThat(day5.getIncome()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(day5.getExpense()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(day5.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));

        // Day 1 (no transactions) should have zeros
        DailyStatDto day1 = result.get(0);
        assertThat(day1.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(day1.getIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(day1.getExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(day1.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getDailyStats - returns all zero stats for empty month")
    void getDailyStats_empty() {
        // Given
        int year = 2025, month = 2; // February 2025 has 28 days
        LocalDate start = LocalDate.of(2025, 2, 1);
        LocalDate end = LocalDate.of(2025, 2, 28);

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(Collections.emptyList());

        // When
        List<DailyStatDto> result = statisticsService.getDailyStats(USER_ID, year, month);

        // Then
        assertThat(result).hasSize(28); // February 2025 has 28 days
        assertThat(result).allMatch(d ->
                d.getIncome().compareTo(BigDecimal.ZERO) == 0 &&
                d.getExpense().compareTo(BigDecimal.ZERO) == 0 &&
                d.getBalance().compareTo(BigDecimal.ZERO) == 0
        );
    }

    @Test
    @DisplayName("getDailyStats - handles mixed income and expense on multiple days")
    void getDailyStats_mixedTransactions() {
        // Given
        int year = 2025, month = 3;
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        Transaction incomeDay1 = createTransaction(1L, new BigDecimal("3000.00"), (short) 1, 10L, LocalDate.of(2025, 3, 1));
        Transaction expenseDay1 = createTransaction(2L, new BigDecimal("200.00"), (short) 2, 20L, LocalDate.of(2025, 3, 1));
        Transaction expenseDay10 = createTransaction(3L, new BigDecimal("800.00"), (short) 2, 20L, LocalDate.of(2025, 3, 10));
        Transaction incomeDay15 = createTransaction(4L, new BigDecimal("5000.00"), (short) 1, 10L, LocalDate.of(2025, 3, 15));

        when(transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                USER_ID, start, end))
                .thenReturn(List.of(incomeDay1, expenseDay1, expenseDay10, incomeDay15));

        // When
        List<DailyStatDto> result = statisticsService.getDailyStats(USER_ID, year, month);

        // Then
        assertThat(result).hasSize(31);

        // Day 1: income 3000, expense 200, balance 2800
        DailyStatDto day1 = result.get(0);
        assertThat(day1.getIncome()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(day1.getExpense()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(day1.getBalance()).isEqualByComparingTo(new BigDecimal("2800.00"));

        // Day 10: expense only
        DailyStatDto day10 = result.get(9);
        assertThat(day10.getIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(day10.getExpense()).isEqualByComparingTo(new BigDecimal("800.00"));
        assertThat(day10.getBalance()).isEqualByComparingTo(new BigDecimal("-800.00"));

        // Day 15: income only
        DailyStatDto day15 = result.get(14);
        assertThat(day15.getIncome()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(day15.getExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(day15.getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }
}
