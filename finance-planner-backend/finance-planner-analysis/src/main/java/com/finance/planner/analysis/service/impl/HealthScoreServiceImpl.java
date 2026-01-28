package com.finance.planner.analysis.service.impl;

import com.finance.planner.analysis.dto.HealthScoreDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import com.finance.planner.analysis.service.HealthScoreService;
import com.finance.planner.analysis.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthScoreServiceImpl implements HealthScoreService {

    private static final int MAX_SAVING_SCORE = 30;
    private static final int MAX_BALANCE_SCORE = 25;
    private static final int MAX_CONSUMPTION_SCORE = 20;
    private static final int MAX_GROWTH_SCORE = 15;
    private static final int MAX_HABIT_SCORE = 10;

    private static final BigDecimal TARGET_SAVINGS_RATE = BigDecimal.valueOf(20);

    private final StatisticsService statisticsService;

    @Override
    public HealthScoreDto calculateHealthScore(Long userId) {
        YearMonth now = YearMonth.now();

        // Get last 3 months of data
        MonthlySummaryDto currentMonth = statisticsService.getMonthlySummary(userId, now.getYear(), now.getMonthValue());
        MonthlySummaryDto lastMonth = statisticsService.getMonthlySummary(userId,
                now.minusMonths(1).getYear(), now.minusMonths(1).getMonthValue());
        MonthlySummaryDto twoMonthsAgo = statisticsService.getMonthlySummary(userId,
                now.minusMonths(2).getYear(), now.minusMonths(2).getMonthValue());

        // Calculate individual scores
        HealthScoreDto.ScoreDetail savingDetail = calculateSavingScore(currentMonth);
        HealthScoreDto.ScoreDetail balanceDetail = calculateBalanceScore(currentMonth);
        HealthScoreDto.ScoreDetail consumptionDetail = calculateConsumptionScore(currentMonth);
        HealthScoreDto.ScoreDetail growthDetail = calculateGrowthScore(currentMonth, lastMonth);
        HealthScoreDto.ScoreDetail habitDetail = calculateHabitScore(currentMonth, lastMonth, twoMonthsAgo);

        // Calculate total score
        int totalScore = savingDetail.getScore() + balanceDetail.getScore() +
                consumptionDetail.getScore() + growthDetail.getScore() + habitDetail.getScore();

        // Generate suggestions
        List<String> suggestions = generateSuggestions(savingDetail, balanceDetail,
                consumptionDetail, growthDetail, habitDetail, currentMonth);

        HealthScoreDto dto = HealthScoreDto.builder()
                .totalScore(totalScore)
                .savingAbility(savingDetail.getScore())
                .balanceRatio(balanceDetail.getScore())
                .consumptionStructure(consumptionDetail.getScore())
                .assetGrowth(growthDetail.getScore())
                .recordingHabit(habitDetail.getScore())
                .savingDetail(savingDetail)
                .balanceDetail(balanceDetail)
                .consumptionDetail(consumptionDetail)
                .growthDetail(growthDetail)
                .habitDetail(habitDetail)
                .suggestions(suggestions)
                .build();

        dto.calculateGrade();
        return dto;
    }

    /**
     * Saving ability score (30 points max)
     * Based on savings rate compared to 20% target
     */
    private HealthScoreDto.ScoreDetail calculateSavingScore(MonthlySummaryDto summary) {
        BigDecimal savingsRate = summary.getSavingsRate() != null ? summary.getSavingsRate() : BigDecimal.ZERO;

        int score;
        String status;
        String description;

        if (savingsRate.compareTo(TARGET_SAVINGS_RATE) >= 0) {
            score = MAX_SAVING_SCORE;
            status = "good";
            description = String.format("储蓄率%.1f%%，达到目标", savingsRate.doubleValue());
        } else if (savingsRate.compareTo(BigDecimal.ZERO) > 0) {
            // Proportional score based on how close to target
            score = savingsRate.multiply(BigDecimal.valueOf(MAX_SAVING_SCORE))
                    .divide(TARGET_SAVINGS_RATE, 0, RoundingMode.HALF_UP)
                    .intValue();
            status = "average";
            description = String.format("储蓄率%.1f%%，距离20%%目标还有提升空间", savingsRate.doubleValue());
        } else {
            score = 0;
            status = "poor";
            description = "本月支出超过收入，需要控制支出";
        }

        return HealthScoreDto.ScoreDetail.builder()
                .name("储蓄能力")
                .score(score)
                .maxScore(MAX_SAVING_SCORE)
                .description(description)
                .status(status)
                .build();
    }

    /**
     * Balance ratio score (25 points max)
     * Based on expense/income ratio
     */
    private HealthScoreDto.ScoreDetail calculateBalanceScore(MonthlySummaryDto summary) {
        BigDecimal income = summary.getTotalIncome();
        BigDecimal expense = summary.getTotalExpense();

        int score;
        String status;
        String description;

        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            score = 0;
            status = "poor";
            description = "本月无收入记录";
        } else {
            BigDecimal ratio = expense.divide(income, 4, RoundingMode.HALF_UP);

            if (ratio.compareTo(BigDecimal.valueOf(0.6)) <= 0) {
                score = MAX_BALANCE_SCORE;
                status = "good";
                description = String.format("支出占收入%.0f%%，收支平衡良好", ratio.multiply(BigDecimal.valueOf(100)).doubleValue());
            } else if (ratio.compareTo(BigDecimal.valueOf(0.8)) <= 0) {
                score = (int) (MAX_BALANCE_SCORE * 0.8);
                status = "average";
                description = String.format("支出占收入%.0f%%，还有优化空间", ratio.multiply(BigDecimal.valueOf(100)).doubleValue());
            } else if (ratio.compareTo(BigDecimal.ONE) <= 0) {
                score = (int) (MAX_BALANCE_SCORE * 0.5);
                status = "average";
                description = String.format("支出占收入%.0f%%，建议适当控制", ratio.multiply(BigDecimal.valueOf(100)).doubleValue());
            } else {
                score = 0;
                status = "poor";
                description = "支出超过收入，需要调整消费习惯";
            }
        }

        return HealthScoreDto.ScoreDetail.builder()
                .name("收支平衡")
                .score(score)
                .maxScore(MAX_BALANCE_SCORE)
                .description(description)
                .status(status)
                .build();
    }

    /**
     * Consumption structure score (20 points max)
     * For now, simplified to give base score if there are transactions
     */
    private HealthScoreDto.ScoreDetail calculateConsumptionScore(MonthlySummaryDto summary) {
        int transactionCount = summary.getTransactionCount();

        int score;
        String status;
        String description;

        if (transactionCount == 0) {
            score = 0;
            status = "poor";
            description = "本月无交易记录";
        } else {
            // Simplified: give proportional score based on transaction count
            // More detailed analysis would require category breakdown
            score = Math.min(MAX_CONSUMPTION_SCORE, transactionCount);
            if (score >= MAX_CONSUMPTION_SCORE * 0.8) {
                status = "good";
                description = "消费结构合理";
            } else {
                status = "average";
                description = "记录更多交易以获得准确分析";
            }
        }

        return HealthScoreDto.ScoreDetail.builder()
                .name("消费结构")
                .score(score)
                .maxScore(MAX_CONSUMPTION_SCORE)
                .description(description)
                .status(status)
                .build();
    }

    /**
     * Asset growth score (15 points max)
     * Based on month-over-month savings improvement
     */
    private HealthScoreDto.ScoreDetail calculateGrowthScore(MonthlySummaryDto current, MonthlySummaryDto previous) {
        BigDecimal currentSavings = current.getBalance();
        BigDecimal previousSavings = previous.getBalance();

        int score;
        String status;
        String description;

        if (previousSavings.compareTo(BigDecimal.ZERO) <= 0 && currentSavings.compareTo(BigDecimal.ZERO) <= 0) {
            score = 0;
            status = "poor";
            description = "连续月份储蓄为负";
        } else if (currentSavings.compareTo(previousSavings) > 0) {
            score = MAX_GROWTH_SCORE;
            status = "good";
            description = "储蓄较上月增长";
        } else if (currentSavings.compareTo(BigDecimal.ZERO) > 0) {
            score = (int) (MAX_GROWTH_SCORE * 0.6);
            status = "average";
            description = "储蓄有所减少";
        } else {
            score = 0;
            status = "poor";
            description = "本月储蓄为负";
        }

        return HealthScoreDto.ScoreDetail.builder()
                .name("资产增长")
                .score(score)
                .maxScore(MAX_GROWTH_SCORE)
                .description(description)
                .status(status)
                .build();
    }

    /**
     * Recording habit score (10 points max)
     * Based on transaction frequency over 3 months
     */
    private HealthScoreDto.ScoreDetail calculateHabitScore(MonthlySummaryDto current,
            MonthlySummaryDto lastMonth, MonthlySummaryDto twoMonthsAgo) {

        int totalTransactions = current.getTransactionCount() +
                lastMonth.getTransactionCount() + twoMonthsAgo.getTransactionCount();

        int score;
        String status;
        String description;

        if (totalTransactions >= 30) { // ~10 per month
            score = MAX_HABIT_SCORE;
            status = "good";
            description = "记账习惯良好";
        } else if (totalTransactions >= 15) {
            score = (int) (MAX_HABIT_SCORE * 0.7);
            status = "average";
            description = "建议更频繁记录";
        } else if (totalTransactions > 0) {
            score = (int) (MAX_HABIT_SCORE * 0.3);
            status = "average";
            description = "记账次数较少";
        } else {
            score = 0;
            status = "poor";
            description = "近3月无记账记录";
        }

        return HealthScoreDto.ScoreDetail.builder()
                .name("记账习惯")
                .score(score)
                .maxScore(MAX_HABIT_SCORE)
                .description(description)
                .status(status)
                .build();
    }

    /**
     * Generate improvement suggestions based on scores
     */
    private List<String> generateSuggestions(HealthScoreDto.ScoreDetail saving,
            HealthScoreDto.ScoreDetail balance, HealthScoreDto.ScoreDetail consumption,
            HealthScoreDto.ScoreDetail growth, HealthScoreDto.ScoreDetail habit,
            MonthlySummaryDto summary) {

        List<String> suggestions = new ArrayList<>();

        if ("poor".equals(saving.getStatus()) || "average".equals(saving.getStatus())) {
            suggestions.add("建议将月收入的20%存入储蓄账户");
        }

        if ("poor".equals(balance.getStatus())) {
            suggestions.add("控制不必要的开支，建立消费预算");
        }

        if ("poor".equals(habit.getStatus()) || "average".equals(habit.getStatus())) {
            suggestions.add("养成每日记账习惯，更好地了解消费模式");
        }

        if ("poor".equals(growth.getStatus())) {
            suggestions.add("考虑减少非必要支出，增加储蓄");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("继续保持良好的财务习惯！");
        }

        return suggestions;
    }
}
