package com.finance.planner.career.dto;

import com.finance.planner.career.entity.CareerIncome;
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
public class CareerIncomeDto {

    private Long id;
    private Long careerPlanId;
    private BigDecimal amount;
    private String description;
    private LocalDate incomeDate;
    private LocalDateTime createdAt;

    public static CareerIncomeDto fromEntity(CareerIncome income) {
        return CareerIncomeDto.builder()
                .id(income.getId())
                .careerPlanId(income.getCareerPlanId())
                .amount(income.getAmount())
                .description(income.getDescription())
                .incomeDate(income.getIncomeDate())
                .createdAt(income.getCreatedAt())
                .build();
    }
}
