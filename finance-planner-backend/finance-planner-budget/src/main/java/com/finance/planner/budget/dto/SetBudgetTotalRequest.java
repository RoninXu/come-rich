package com.finance.planner.budget.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetBudgetTotalRequest {

    @NotBlank(message = "月份不能为空")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "月份格式必须为YYYY-MM")
    private String yearMonth;

    @NotNull(message = "总预算金额不能为空")
    @DecimalMin(value = "0.01", message = "总预算金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额最多保留两位小数")
    private BigDecimal totalAmount;
}
