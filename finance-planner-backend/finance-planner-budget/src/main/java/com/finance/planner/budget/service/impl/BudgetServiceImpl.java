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
import com.finance.planner.budget.service.BudgetService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetTotalRepository budgetTotalRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional
    public BudgetDto setBudget(Long userId, SetBudgetRequest request) {
        validateYearMonth(request.getYearMonth());

        // Verify category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // Upsert: update if exists, create if not
        Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIdAndYearMonth(
                userId, request.getCategoryId(), request.getYearMonth());

        Budget budget;
        if (existing.isPresent()) {
            budget = existing.get();
            budget.setAmount(request.getAmount());
            budget.setNote(request.getNote());
        } else {
            budget = new Budget();
            budget.setUserId(userId);
            budget.setCategoryId(request.getCategoryId());
            budget.setYearMonth(request.getYearMonth());
            budget.setAmount(request.getAmount());
            budget.setNote(request.getNote());
        }

        Budget saved = budgetRepository.save(budget);
        log.info("Set budget {} for user {} category {} month {}",
                saved.getId(), userId, request.getCategoryId(), request.getYearMonth());
        return BudgetDto.fromEntity(saved, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDto> getBudgets(Long userId, String yearMonth) {
        validateYearMonth(yearMonth);

        List<Budget> budgets = budgetRepository.findByUserIdAndYearMonth(userId, yearMonth);

        // Build category map for enrichment
        Set<Long> categoryIds = budgets.stream()
                .map(Budget::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        return budgets.stream()
                .map(b -> BudgetDto.fromEntity(b, categoryMap.get(b.getCategoryId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBudget(Long userId, Long categoryId, String yearMonth) {
        validateYearMonth(yearMonth);

        budgetRepository.findByUserIdAndCategoryIdAndYearMonth(userId, categoryId, yearMonth)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUDGET_NOT_FOUND));

        budgetRepository.deleteByUserIdAndCategoryIdAndYearMonth(userId, categoryId, yearMonth);
        log.info("Deleted budget for user {} category {} month {}", userId, categoryId, yearMonth);
    }

    @Override
    @Transactional
    public BudgetTotalDto setBudgetTotal(Long userId, SetBudgetTotalRequest request) {
        validateYearMonth(request.getYearMonth());

        // Upsert: update if exists, create if not
        Optional<BudgetTotal> existing = budgetTotalRepository.findByUserIdAndYearMonth(
                userId, request.getYearMonth());

        BudgetTotal budgetTotal;
        if (existing.isPresent()) {
            budgetTotal = existing.get();
            budgetTotal.setTotalAmount(request.getTotalAmount());
        } else {
            budgetTotal = new BudgetTotal();
            budgetTotal.setUserId(userId);
            budgetTotal.setYearMonth(request.getYearMonth());
            budgetTotal.setTotalAmount(request.getTotalAmount());
        }

        BudgetTotal saved = budgetTotalRepository.save(budgetTotal);
        log.info("Set budget total {} for user {} month {}", saved.getId(), userId, request.getYearMonth());
        return BudgetTotalDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetTotalDto getBudgetTotal(Long userId, String yearMonth) {
        validateYearMonth(yearMonth);

        return budgetTotalRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .map(BudgetTotalDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetSummaryDto getBudgetSummary(Long userId, String yearMonth) {
        validateYearMonth(yearMonth);

        YearMonth ym = YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // Get all budgets for this month
        List<Budget> budgets = budgetRepository.findByUserIdAndYearMonth(userId, yearMonth);

        // Get all transactions for the month
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);

        // Build category map
        Set<Long> allCategoryIds = new HashSet<>();
        budgets.forEach(b -> allCategoryIds.add(b.getCategoryId()));
        transactions.forEach(t -> allCategoryIds.add(t.getCategoryId()));
        Map<Long, Category> categoryMap = categoryRepository.findAllById(allCategoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // Build parent-to-children mapping for hierarchical summation
        Map<Long, List<Long>> parentToChildIds = new HashMap<>();
        for (Category cat : categoryMap.values()) {
            if (cat.getParentId() != null) {
                parentToChildIds
                        .computeIfAbsent(cat.getParentId(), k -> new ArrayList<>())
                        .add(cat.getId());
            }
        }

        // Group expense transactions (type=2) by categoryId and sum amounts
        Map<Long, BigDecimal> expenseByCategoryId = transactions.stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        // Build comparison for each budget
        List<BudgetComparisonDto> comparisons = new ArrayList<>();
        BigDecimal totalBudgetAmount = BigDecimal.ZERO;
        BigDecimal totalSpentAmount = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            Category category = categoryMap.get(budget.getCategoryId());
            BigDecimal budgetAmount = budget.getAmount();

            // Calculate actual amount: direct category spend
            BigDecimal actualAmount = expenseByCategoryId.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO);

            // If this is a parent category, also include child category transactions
            if (category != null && category.isParent()) {
                List<Long> childIds = parentToChildIds.getOrDefault(budget.getCategoryId(), List.of());
                for (Long childId : childIds) {
                    actualAmount = actualAmount.add(
                            expenseByCategoryId.getOrDefault(childId, BigDecimal.ZERO));
                }
            }

            BigDecimal remaining = budgetAmount.subtract(actualAmount);
            BigDecimal utilization = BigDecimal.ZERO;
            if (budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                utilization = actualAmount
                        .multiply(new BigDecimal("100"))
                        .divide(budgetAmount, 1, RoundingMode.HALF_UP);
            }
            boolean overBudget = utilization.compareTo(new BigDecimal("100")) > 0;

            comparisons.add(BudgetComparisonDto.builder()
                    .categoryId(budget.getCategoryId())
                    .categoryName(category != null ? category.getName() : null)
                    .categoryIcon(category != null ? category.getIcon() : null)
                    .categoryColor(category != null ? category.getColor() : null)
                    .budgetAmount(budgetAmount)
                    .actualAmount(actualAmount)
                    .remainingAmount(remaining)
                    .utilizationPercentage(utilization)
                    .overBudget(overBudget)
                    .build());

            totalBudgetAmount = totalBudgetAmount.add(budgetAmount);
            totalSpentAmount = totalSpentAmount.add(actualAmount);
        }

        // If no category budgets exist, use total budget
        if (budgets.isEmpty()) {
            BudgetTotal budgetTotal = budgetTotalRepository.findByUserIdAndYearMonth(userId, yearMonth)
                    .orElse(null);
            if (budgetTotal != null) {
                totalBudgetAmount = budgetTotal.getTotalAmount();
            }
            // Sum all expense transactions
            totalSpentAmount = transactions.stream()
                    .filter(Transaction::isExpense)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal totalRemaining = totalBudgetAmount.subtract(totalSpentAmount);
        BigDecimal overallUtilization = BigDecimal.ZERO;
        if (totalBudgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            overallUtilization = totalSpentAmount
                    .multiply(new BigDecimal("100"))
                    .divide(totalBudgetAmount, 1, RoundingMode.HALF_UP);
        }

        return BudgetSummaryDto.builder()
                .yearMonth(yearMonth)
                .totalBudget(totalBudgetAmount)
                .totalSpent(totalSpentAmount)
                .totalRemaining(totalRemaining)
                .overallUtilization(overallUtilization)
                .categories(comparisons)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetSummaryDto> getBudgetTrend(Long userId, int months) {
        List<BudgetSummaryDto> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 0; i < months; i++) {
            YearMonth ym = current.minusMonths(i);
            String yearMonth = ym.format(YEAR_MONTH_FORMATTER);

            try {
                BudgetSummaryDto summary = getBudgetSummary(userId, yearMonth);
                trend.add(summary);
            } catch (Exception e) {
                log.warn("Failed to get budget summary for {} month {}: {}", userId, yearMonth, e.getMessage());
                // Add an empty summary for this month
                trend.add(BudgetSummaryDto.builder()
                        .yearMonth(yearMonth)
                        .totalBudget(BigDecimal.ZERO)
                        .totalSpent(BigDecimal.ZERO)
                        .totalRemaining(BigDecimal.ZERO)
                        .overallUtilization(BigDecimal.ZERO)
                        .categories(List.of())
                        .build());
            }
        }

        return trend;
    }

    @Override
    @Transactional
    public List<BudgetDto> copyBudgetFromPreviousMonth(Long userId, String targetMonth) {
        validateYearMonth(targetMonth);

        YearMonth target = YearMonth.parse(targetMonth, YEAR_MONTH_FORMATTER);
        YearMonth previous = target.minusMonths(1);
        String previousMonth = previous.format(YEAR_MONTH_FORMATTER);

        // Find budgets from previous month
        List<Budget> previousBudgets = budgetRepository.findByUserIdAndYearMonth(userId, previousMonth);
        if (previousBudgets.isEmpty()) {
            throw new BusinessException(ErrorCode.BUDGET_NO_PREVIOUS);
        }

        // Build category map for enrichment
        Set<Long> categoryIds = previousBudgets.stream()
                .map(Budget::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        List<BudgetDto> copied = new ArrayList<>();
        for (Budget prev : previousBudgets) {
            // Skip if budget already exists for target month
            Optional<Budget> existing = budgetRepository.findByUserIdAndCategoryIdAndYearMonth(
                    userId, prev.getCategoryId(), targetMonth);
            if (existing.isPresent()) {
                log.debug("Budget already exists for user {} category {} month {}, skipping",
                        userId, prev.getCategoryId(), targetMonth);
                copied.add(BudgetDto.fromEntity(existing.get(), categoryMap.get(prev.getCategoryId())));
                continue;
            }

            Budget newBudget = new Budget();
            newBudget.setUserId(userId);
            newBudget.setCategoryId(prev.getCategoryId());
            newBudget.setYearMonth(targetMonth);
            newBudget.setAmount(prev.getAmount());
            newBudget.setNote(prev.getNote());

            Budget saved = budgetRepository.save(newBudget);
            copied.add(BudgetDto.fromEntity(saved, categoryMap.get(prev.getCategoryId())));
        }

        // Also copy total budget if exists
        budgetTotalRepository.findByUserIdAndYearMonth(userId, previousMonth).ifPresent(prevTotal -> {
            if (budgetTotalRepository.findByUserIdAndYearMonth(userId, targetMonth).isEmpty()) {
                BudgetTotal newTotal = new BudgetTotal();
                newTotal.setUserId(userId);
                newTotal.setYearMonth(targetMonth);
                newTotal.setTotalAmount(prevTotal.getTotalAmount());
                budgetTotalRepository.save(newTotal);
                log.info("Copied total budget from {} to {} for user {}", previousMonth, targetMonth, userId);
            }
        });

        log.info("Copied {} budgets from {} to {} for user {}", copied.size(), previousMonth, targetMonth, userId);
        return copied;
    }

    private void validateYearMonth(String yearMonth) {
        try {
            YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.BUDGET_INVALID_MONTH);
        }
    }
}
