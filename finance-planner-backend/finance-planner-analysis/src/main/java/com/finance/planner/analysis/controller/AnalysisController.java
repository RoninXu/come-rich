package com.finance.planner.analysis.controller;

import com.finance.planner.accounting.dto.TransactionDto;
import com.finance.planner.accounting.service.TransactionService;
import com.finance.planner.analysis.dto.*;
import com.finance.planner.analysis.service.HealthScoreService;
import com.finance.planner.analysis.service.StatisticsService;
import com.finance.planner.auth.entity.User;
import com.finance.planner.auth.repository.UserRepository;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Financial analysis and statistics endpoints")
public class AnalysisController {

    private final StatisticsService statisticsService;
    private final HealthScoreService healthScoreService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly summary", description = "Get income, expense, and balance for a specific month")
    public ApiResponse<MonthlySummaryDto> getMonthlySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Year (e.g., 2026)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12)")
            @RequestParam(required = false) Integer month) {

        Long userId = getUserId(userDetails);

        // Default to current month if not specified
        YearMonth targetMonth = YearMonth.now();
        if (year != null && month != null) {
            targetMonth = YearMonth.of(year, month);
        }

        MonthlySummaryDto summary = statisticsService.getMonthlySummary(
                userId, targetMonth.getYear(), targetMonth.getMonthValue());
        return ApiResponse.success(summary);
    }

    @GetMapping("/category")
    @Operation(summary = "Get category statistics", description = "Get expense/income breakdown by category")
    public ApiResponse<List<CategoryStatDto>> getCategoryStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Year (e.g., 2026)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12)")
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Transaction type: 1=income, 2=expense")
            @RequestParam(required = false) Short type) {

        Long userId = getUserId(userDetails);

        // Default to current month if not specified
        YearMonth targetMonth = YearMonth.now();
        if (year != null && month != null) {
            targetMonth = YearMonth.of(year, month);
        }

        List<CategoryStatDto> stats = statisticsService.getCategoryStats(
                userId, targetMonth.getYear(), targetMonth.getMonthValue(), type);
        return ApiResponse.success(stats);
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily statistics", description = "Get daily income and expense for a month")
    public ApiResponse<List<DailyStatDto>> getDailyStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Year (e.g., 2026)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12)")
            @RequestParam(required = false) Integer month) {

        Long userId = getUserId(userDetails);

        // Default to current month if not specified
        YearMonth targetMonth = YearMonth.now();
        if (year != null && month != null) {
            targetMonth = YearMonth.of(year, month);
        }

        List<DailyStatDto> stats = statisticsService.getDailyStats(
                userId, targetMonth.getYear(), targetMonth.getMonthValue());
        return ApiResponse.success(stats);
    }

    @GetMapping("/health-score")
    @Operation(summary = "Get financial health score", description = "Calculate health score based on recent transactions")
    public ApiResponse<HealthScoreDto> getHealthScore(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        HealthScoreDto score = healthScoreService.calculateHealthScore(userId);
        return ApiResponse.success(score);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard data", description = "Get summary data for the dashboard")
    public ApiResponse<DashboardDto> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);

        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);

        // Get current month summary
        MonthlySummaryDto currentSummary = statisticsService.getMonthlySummary(
                userId, currentMonth.getYear(), currentMonth.getMonthValue());

        // Get last month summary for comparison
        MonthlySummaryDto lastSummary = statisticsService.getMonthlySummary(
                userId, lastMonth.getYear(), lastMonth.getMonthValue());

        // Get health score
        HealthScoreDto healthScore = healthScoreService.calculateHealthScore(userId);

        // Get recent transactions
        List<TransactionDto> recentTransactions = transactionService.getRecentTransactions(userId);

        // Calculate month-over-month changes
        BigDecimal incomeChange = calculatePercentageChange(lastSummary.getTotalIncome(), currentSummary.getTotalIncome());
        BigDecimal expenseChange = calculatePercentageChange(lastSummary.getTotalExpense(), currentSummary.getTotalExpense());

        DashboardDto dashboard = DashboardDto.builder()
                .currentMonth(currentSummary)
                .healthScore(healthScore.getTotalScore())
                .healthGrade(healthScore.getGrade())
                .recentTransactions(recentTransactions)
                .incomeChange(incomeChange)
                .expenseChange(expenseChange)
                .build();

        return ApiResponse.success(dashboard);
    }

    /**
     * Calculate percentage change between two values
     */
    private BigDecimal calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                return BigDecimal.valueOf(100); // 100% increase from zero
            }
            return BigDecimal.ZERO;
        }

        if (current == null) {
            current = BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    /**
     * Extract user ID from authenticated user
     */
    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }
}
