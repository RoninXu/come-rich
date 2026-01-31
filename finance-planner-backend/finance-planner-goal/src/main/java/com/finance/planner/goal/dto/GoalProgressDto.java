package com.finance.planner.goal.dto;

import com.finance.planner.goal.entity.GoalProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressDto {

    private Long id;
    private Long goalId;
    private BigDecimal amount;
    private String note;
    private LocalDate recordDate;
    private LocalDateTime createdAt;

    public static GoalProgressDto fromEntity(GoalProgress progress) {
        return GoalProgressDto.builder()
                .id(progress.getId())
                .goalId(progress.getGoalId())
                .amount(progress.getAmount())
                .note(progress.getNote())
                .recordDate(progress.getRecordDate())
                .createdAt(progress.getCreatedAt())
                .build();
    }
}
