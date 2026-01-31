package com.finance.planner.goal.dto;

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
public class AddProgressRequest {

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额最多保留两位小数")
    private BigDecimal amount;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String note;

    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
}
