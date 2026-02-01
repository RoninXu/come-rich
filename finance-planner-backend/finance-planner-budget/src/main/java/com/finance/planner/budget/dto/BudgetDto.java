package com.finance.planner.budget.dto;

import com.finance.planner.accounting.entity.Category;
import com.finance.planner.budget.entity.Budget;
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
public class BudgetDto {

    private Long id;
    private Long userId;
    private Long categoryId;
    private String yearMonth;
    private BigDecimal amount;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Category info
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;

    public static BudgetDto fromEntity(Budget budget, Category category) {
        BudgetDtoBuilder builder = BudgetDto.builder()
                .id(budget.getId())
                .userId(budget.getUserId())
                .categoryId(budget.getCategoryId())
                .yearMonth(budget.getYearMonth())
                .amount(budget.getAmount())
                .note(budget.getNote())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt());

        if (category != null) {
            builder.categoryName(category.getName())
                    .categoryIcon(category.getIcon())
                    .categoryColor(category.getColor());
        }

        return builder.build();
    }
}
