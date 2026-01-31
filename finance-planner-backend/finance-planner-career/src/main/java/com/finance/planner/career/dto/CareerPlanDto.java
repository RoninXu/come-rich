package com.finance.planner.career.dto;

import com.finance.planner.career.entity.CareerPlan;
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
public class CareerPlanDto {

    private Long id;
    private String careerType;
    private String title;
    private String description;
    private Integer matchScore;
    private Short status;
    private BigDecimal targetMonthlyIncome;
    private BigDecimal actualMonthlyIncome;
    private LocalDate startDate;
    private LocalDate endDate;
    private String startupPlan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CareerPlanDto fromEntity(CareerPlan plan) {
        return CareerPlanDto.builder()
                .id(plan.getId())
                .careerType(plan.getCareerType())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .matchScore(plan.getMatchScore())
                .status(plan.getStatus())
                .targetMonthlyIncome(plan.getTargetMonthlyIncome())
                .actualMonthlyIncome(plan.getActualMonthlyIncome())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .startupPlan(plan.getStartupPlan())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
