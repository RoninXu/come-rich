package com.finance.planner.goal.dto;

import com.finance.planner.goal.entity.FinancialGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private Short status;
    private Short priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private BigDecimal progressPercentage;
    private Long remainingDays;
    private BigDecimal monthlySavingsNeeded;

    public static GoalDto fromEntity(FinancialGoal goal) {
        GoalDto dto = GoalDto.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetAmount(goal.getTargetAmount())
                .currentAmount(goal.getCurrentAmount())
                .deadline(goal.getDeadline())
                .status(goal.getStatus())
                .priority(goal.getPriority())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();

        // Calculate progress percentage
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            dto.setProgressPercentage(
                    goal.getCurrentAmount()
                            .multiply(new BigDecimal("100"))
                            .divide(goal.getTargetAmount(), 1, RoundingMode.HALF_UP));
        } else {
            dto.setProgressPercentage(BigDecimal.ZERO);
        }

        // Calculate remaining days
        long days = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
        dto.setRemainingDays(Math.max(0, days));

        // Calculate monthly savings needed
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) > 0 && days > 0) {
            long months = Math.max(1, ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline()));
            dto.setMonthlySavingsNeeded(remaining.divide(new BigDecimal(months), 2, RoundingMode.CEILING));
        } else {
            dto.setMonthlySavingsNeeded(BigDecimal.ZERO);
        }

        return dto;
    }
}
