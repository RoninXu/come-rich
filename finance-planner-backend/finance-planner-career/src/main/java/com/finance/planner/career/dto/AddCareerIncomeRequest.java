package com.finance.planner.career.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCareerIncomeRequest {

    @NotNull(message = "收入金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;

    @NotNull(message = "收入日期不能为空")
    private LocalDate incomeDate;
}
