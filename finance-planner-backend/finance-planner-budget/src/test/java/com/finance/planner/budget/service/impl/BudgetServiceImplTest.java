package com.finance.planner.budget.service.impl;

import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import com.finance.planner.budget.dto.*;
import com.finance.planner.budget.entity.Budget;
import com.finance.planner.budget.entity.BudgetTotal;
import com.finance.planner.budget.repository.BudgetRepository;
import com.finance.planner.budget.repository.BudgetTotalRepository;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetServiceImpl Unit Tests")
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private BudgetTotalRepository budgetTotalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 10L;
    private static final String YEAR_MONTH = "2026-01";

    // ========== Helper methods ==========

    private Budget createBudget(Long id, Long userId, Long categoryId, String yearMonth,
                                BigDecimal amount, String note) {
        Budget budget = new Budget();
        budget.setId(id);
        budget.setUserId(userId);
        budget.setCategoryId(categoryId);
        budget.setYearMonth(yearMonth);
        budget.setAmount(amount);
        budget.setNote(note);
        return budget;
    }

    private BudgetTotal createBudgetTotal(Long id, Long userId, String yearMonth, BigDecimal totalAmount) {
        BudgetTotal budgetTotal = new BudgetTotal();
        budgetTotal.setId(id);
        budgetTotal.setUserId(userId);
        budgetTotal.setYearMonth(yearMonth);
        budgetTotal.setTotalAmount(totalAmount);
        return budgetTotal;
    }

    private Category createCategory(Long id, String name, Long parentId, Short type,
                                     String icon, String color) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setParentId(parentId);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(color);
        return category;
    }

    private Transaction createExpenseTransaction(Long id, Long userId, Long categoryId,
                                                  BigDecimal amount, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUserId(userId);
        transaction.setCategoryId(categoryId);
        transaction.setAmount(amount);
        transaction.setType((short) 2); // expense
        transaction.setTransactionDate(date);
        transaction.setIsDeleted(false);
        return transaction;
    }

    // ========== setBudget ==========

    @Test
    @DisplayName("setBudget - creates new budget when none exists")
    void setBudget_createNew() {
        SetBudgetRequest request = SetBudgetRequest.builder()
                .categoryId(CATEGORY_ID)
                .yearMonth(YEAR_MONTH)
                .amount(new BigDecimal("5000.00"))
                .note("Food budget")
                .build();

        Category category = createCategory(CATEGORY_ID, "Food", null, (short) 2, "food-icon", "#FF5733");

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(USER_ID, CATEGORY_ID, YEAR_MONTH))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        BudgetDto result = budgetService.setBudget(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(result.getYearMonth()).isEqualTo(YEAR_MONTH);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getNote()).isEqualTo("Food budget");
        assertThat(result.getCategoryName()).isEqualTo("Food");
        assertThat(result.getCategoryIcon()).isEqualTo("food-icon");
        assertThat(result.getCategoryColor()).isEqualTo("#FF5733");
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    @DisplayName("setBudget - updates existing budget")
    void setBudget_updateExisting() {
        SetBudgetRequest request = SetBudgetRequest.builder()
                .categoryId(CATEGORY_ID)
                .yearMonth(YEAR_MONTH)
                .amount(new BigDecimal("8000.00"))
                .note("Updated food budget")
                .build();

        Category category = createCategory(CATEGORY_ID, "Food", null, (short) 2, "food-icon", "#FF5733");
        Budget existingBudget = createBudget(50L, USER_ID, CATEGORY_ID, YEAR_MONTH,
                new BigDecimal("5000.00"), "Food budget");

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(USER_ID, CATEGORY_ID, YEAR_MONTH))
                .thenReturn(Optional.of(existingBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BudgetDto result = budgetService.setBudget(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(result.getNote()).isEqualTo("Updated food budget");
        verify(budgetRepository).save(existingBudget);
    }

    // ========== getBudgets ==========

    @Test
    @DisplayName("getBudgets - returns budget list for month")
    void getBudgets_returnsList() {
        Budget b1 = createBudget(1L, USER_ID, 10L, YEAR_MONTH, new BigDecimal("5000.00"), "Food");
        Budget b2 = createBudget(2L, USER_ID, 20L, YEAR_MONTH, new BigDecimal("3000.00"), "Transport");

        Category c1 = createCategory(10L, "Food", null, (short) 2, "food-icon", "#FF5733");
        Category c2 = createCategory(20L, "Transport", null, (short) 2, "transport-icon", "#33FF57");

        when(budgetRepository.findByUserIdAndYearMonth(USER_ID, YEAR_MONTH))
                .thenReturn(List.of(b1, b2));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(c1, c2));

        List<BudgetDto> result = budgetService.getBudgets(USER_ID, YEAR_MONTH);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.get(1).getCategoryName()).isEqualTo("Transport");
        assertThat(result.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        verify(budgetRepository).findByUserIdAndYearMonth(USER_ID, YEAR_MONTH);
    }

    // ========== deleteBudget ==========

    @Test
    @DisplayName("deleteBudget - deletes a budget successfully")
    void deleteBudget_success() {
        Budget budget = createBudget(1L, USER_ID, CATEGORY_ID, YEAR_MONTH,
                new BigDecimal("5000.00"), "Food");

        when(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(USER_ID, CATEGORY_ID, YEAR_MONTH))
                .thenReturn(Optional.of(budget));

        budgetService.deleteBudget(USER_ID, CATEGORY_ID, YEAR_MONTH);

        verify(budgetRepository).deleteByUserIdAndCategoryIdAndYearMonth(USER_ID, CATEGORY_ID, YEAR_MONTH);
    }

    // ========== getBudgetSummary ==========

    @Test
    @DisplayName("getBudgetSummary - returns summary with correct utilization calculation")
    void getBudgetSummary_withBudgets() {
        Budget budget = createBudget(1L, USER_ID, CATEGORY_ID, YEAR_MONTH,
                new BigDecimal("5000.00"), "Food");

        Category category = createCategory(CATEGORY_ID, "Food", null, (short) 2, "food-icon", "#FF5733");

        // Expense transaction: spent 2000 out of 5000 budget
        Transaction t1 = createExpenseTransaction(1L, USER_ID, CATEGORY_ID,
                new BigDecimal("1200.00"), LocalDate.of(2026, 1, 10));
        Transaction t2 = createExpenseTransaction(2L, USER_ID, CATEGORY_ID,
                new BigDecimal("800.00"), LocalDate.of(2026, 1, 15));

        when(budgetRepository.findByUserIdAndYearMonth(USER_ID, YEAR_MONTH))
                .thenReturn(List.of(budget));
        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        eq(USER_ID), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
                .thenReturn(List.of(t1, t2));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));

        BudgetSummaryDto result = budgetService.getBudgetSummary(USER_ID, YEAR_MONTH);

        assertThat(result).isNotNull();
        assertThat(result.getYearMonth()).isEqualTo(YEAR_MONTH);
        assertThat(result.getTotalBudget()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getTotalSpent()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.getTotalRemaining()).isEqualByComparingTo(new BigDecimal("3000.00"));
        // Utilization: 2000/5000 * 100 = 40.0%
        assertThat(result.getOverallUtilization()).isEqualByComparingTo(new BigDecimal("40.0"));
        assertThat(result.getCategories()).hasSize(1);

        BudgetComparisonDto comparison = result.getCategories().get(0);
        assertThat(comparison.getCategoryId()).isEqualTo(CATEGORY_ID);
        assertThat(comparison.getCategoryName()).isEqualTo("Food");
        assertThat(comparison.getBudgetAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(comparison.getActualAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(comparison.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(comparison.getUtilizationPercentage()).isEqualByComparingTo(new BigDecimal("40.0"));
        assertThat(comparison.isOverBudget()).isFalse();
    }

    @Test
    @DisplayName("getBudgetSummary - detects over-budget categories")
    void getBudgetSummary_overBudget() {
        Budget budget = createBudget(1L, USER_ID, CATEGORY_ID, YEAR_MONTH,
                new BigDecimal("2000.00"), "Food");

        Category category = createCategory(CATEGORY_ID, "Food", null, (short) 2, "food-icon", "#FF5733");

        // Expense exceeds budget: spent 3000 out of 2000 budget
        Transaction t1 = createExpenseTransaction(1L, USER_ID, CATEGORY_ID,
                new BigDecimal("1800.00"), LocalDate.of(2026, 1, 5));
        Transaction t2 = createExpenseTransaction(2L, USER_ID, CATEGORY_ID,
                new BigDecimal("1200.00"), LocalDate.of(2026, 1, 20));

        when(budgetRepository.findByUserIdAndYearMonth(USER_ID, YEAR_MONTH))
                .thenReturn(List.of(budget));
        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        eq(USER_ID), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
                .thenReturn(List.of(t1, t2));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));

        BudgetSummaryDto result = budgetService.getBudgetSummary(USER_ID, YEAR_MONTH);

        assertThat(result).isNotNull();
        assertThat(result.getTotalBudget()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.getTotalSpent()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(result.getTotalRemaining()).isEqualByComparingTo(new BigDecimal("-1000.00"));
        // Utilization: 3000/2000 * 100 = 150.0%
        assertThat(result.getOverallUtilization()).isEqualByComparingTo(new BigDecimal("150.0"));

        BudgetComparisonDto comparison = result.getCategories().get(0);
        assertThat(comparison.getUtilizationPercentage()).isEqualByComparingTo(new BigDecimal("150.0"));
        assertThat(comparison.isOverBudget()).isTrue();
    }

    @Test
    @DisplayName("getBudgetSummary - returns empty summary when no budgets exist")
    void getBudgetSummary_noBudgets() {
        when(budgetRepository.findByUserIdAndYearMonth(USER_ID, YEAR_MONTH))
                .thenReturn(List.of());
        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        eq(USER_ID), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 1, 31))))
                .thenReturn(List.of());
        when(categoryRepository.findAllById(any())).thenReturn(List.of());
        when(budgetTotalRepository.findByUserIdAndYearMonth(USER_ID, YEAR_MONTH))
                .thenReturn(Optional.empty());

        BudgetSummaryDto result = budgetService.getBudgetSummary(USER_ID, YEAR_MONTH);

        assertThat(result).isNotNull();
        assertThat(result.getYearMonth()).isEqualTo(YEAR_MONTH);
        assertThat(result.getTotalBudget()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalRemaining()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOverallUtilization()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCategories()).isEmpty();
    }

    // ========== copyBudgetFromPreviousMonth ==========

    @Test
    @DisplayName("copyBudgetFromPreviousMonth - copies budgets from previous month")
    void copyBudgetFromPreviousMonth_success() {
        String targetMonth = "2026-02";
        String previousMonth = "2026-01";

        Budget prevBudget1 = createBudget(1L, USER_ID, 10L, previousMonth,
                new BigDecimal("5000.00"), "Food");
        Budget prevBudget2 = createBudget(2L, USER_ID, 20L, previousMonth,
                new BigDecimal("3000.00"), "Transport");

        Category c1 = createCategory(10L, "Food", null, (short) 2, "food-icon", "#FF5733");
        Category c2 = createCategory(20L, "Transport", null, (short) 2, "transport-icon", "#33FF57");

        when(budgetRepository.findByUserIdAndYearMonth(USER_ID, previousMonth))
                .thenReturn(List.of(prevBudget1, prevBudget2));
        when(categoryRepository.findAllById(any())).thenReturn(List.of(c1, c2));

        // No existing budgets for target month
        when(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(USER_ID, 10L, targetMonth))
                .thenReturn(Optional.empty());
        when(budgetRepository.findByUserIdAndCategoryIdAndYearMonth(USER_ID, 20L, targetMonth))
                .thenReturn(Optional.empty());

        when(budgetRepository.save(any(Budget.class))).thenAnswer(invocation -> {
            Budget b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(100L + b.getCategoryId());
            }
            return b;
        });

        // No previous total budget
        when(budgetTotalRepository.findByUserIdAndYearMonth(USER_ID, previousMonth))
                .thenReturn(Optional.empty());

        List<BudgetDto> result = budgetService.copyBudgetFromPreviousMonth(USER_ID, targetMonth);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getYearMonth()).isEqualTo(targetMonth);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.get(1).getYearMonth()).isEqualTo(targetMonth);
        assertThat(result.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("3000.00"));
        verify(budgetRepository, times(2)).save(any(Budget.class));
    }

    @Test
    @DisplayName("copyBudgetFromPreviousMonth - throws when no previous budgets exist")
    void copyBudgetFromPreviousMonth_noPrevious() {
        String targetMonth = "2026-02";
        String previousMonth = "2026-01";

        when(budgetRepository.findByUserIdAndYearMonth(USER_ID, previousMonth))
                .thenReturn(List.of());

        assertThatThrownBy(() -> budgetService.copyBudgetFromPreviousMonth(USER_ID, targetMonth))
                .isInstanceOf(BusinessException.class);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // ========== setBudgetTotal ==========

    @Test
    @DisplayName("setBudgetTotal - sets total budget successfully")
    void setBudgetTotal_success() {
        SetBudgetTotalRequest request = SetBudgetTotalRequest.builder()
                .yearMonth(YEAR_MONTH)
                .totalAmount(new BigDecimal("20000.00"))
                .build();

        when(budgetTotalRepository.findByUserIdAndYearMonth(USER_ID, YEAR_MONTH))
                .thenReturn(Optional.empty());
        when(budgetTotalRepository.save(any(BudgetTotal.class))).thenAnswer(invocation -> {
            BudgetTotal bt = invocation.getArgument(0);
            bt.setId(200L);
            return bt;
        });

        BudgetTotalDto result = budgetService.setBudgetTotal(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(200L);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getYearMonth()).isEqualTo(YEAR_MONTH);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20000.00"));
        verify(budgetTotalRepository).save(any(BudgetTotal.class));
    }
}
