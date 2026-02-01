package com.finance.planner.budget.dto;

import com.finance.planner.budget.entity.BudgetTotal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetTotalDto {

    private Long id;
    private Long userId;
    private String yearMonth;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BudgetTotalDto fromEntity(BudgetTotal budgetTotal) {
        return BudgetTotalDto.builder()
                .id(budgetTotal.getId())
                .userId(budgetTotal.getUserId())
                .yearMonth(budgetTotal.getYearMonth())
                .totalAmount(budgetTotal.getTotalAmount())
                .createdAt(budgetTotal.getCreatedAt())
                .updatedAt(budgetTotal.getUpdatedAt())
                .build();
    }
}
