package com.finance.planner.analysis.service.impl;

import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.entity.Transaction;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.repository.TransactionRepository;
import com.finance.planner.analysis.dto.CategoryStatDto;
import com.finance.planner.analysis.dto.DailyStatDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import com.finance.planner.analysis.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public MonthlySummaryDto getMonthlySummary(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);

        if (transactions.isEmpty()) {
            return MonthlySummaryDto.empty(year, month);
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == 1) { // income
                totalIncome = totalIncome.add(t.getAmount());
            } else { // expense
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        MonthlySummaryDto summary = MonthlySummaryDto.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .transactionCount(transactions.size())
                .build();

        summary.calculateSavingsRate();
        return summary;
    }

    @Override
    public List<CategoryStatDto> getCategoryStats(Long userId, int year, int month, Short type) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions;
        if (type != null) {
            transactions = transactionRepository
                    .findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                            userId, type, startDate, endDate);
        } else {
            transactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                            userId, startDate, endDate);
        }

        if (transactions.isEmpty()) {
            return Collections.emptyList();
        }

        // Group by category
        Map<Long, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        // Calculate total for percentage
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get category details
        Set<Long> categoryIds = byCategory.keySet();
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // Build stats
        List<CategoryStatDto> stats = new ArrayList<>();
        for (Map.Entry<Long, List<Transaction>> entry : byCategory.entrySet()) {
            Long categoryId = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();
            Category category = categoryMap.get(categoryId);

            BigDecimal amount = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            stats.add(CategoryStatDto.builder()
                    .categoryId(categoryId)
                    .categoryName(category != null ? category.getName() : "Unknown")
                    .categoryIcon(category != null ? category.getIcon() : null)
                    .categoryColor(category != null ? category.getColor() : null)
                    .amount(amount)
                    .percentage(percentage)
                    .transactionCount(categoryTransactions.size())
                    .build());
        }

        // Sort by amount descending
        stats.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return stats;
    }

    @Override
    public List<DailyStatDto> getDailyStats(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                        userId, startDate, endDate);

        // Group by date
        Map<LocalDate, List<Transaction>> byDate = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionDate));

        // Build daily stats for each day of the month
        List<DailyStatDto> dailyStats = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<Transaction> dayTransactions = byDate.getOrDefault(date, Collections.emptyList());

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;

            for (Transaction t : dayTransactions) {
                if (t.getType() == 1) {
                    income = income.add(t.getAmount());
                } else {
                    expense = expense.add(t.getAmount());
                }
            }

            dailyStats.add(DailyStatDto.builder()
                    .date(date)
                    .income(income)
                    .expense(expense)
                    .balance(income.subtract(expense))
                    .build());
        }

        return dailyStats;
    }
}
