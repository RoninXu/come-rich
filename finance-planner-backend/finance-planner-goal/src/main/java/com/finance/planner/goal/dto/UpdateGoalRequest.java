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
public class UpdateGoalRequest {

    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    @DecimalMin(value = "0.01", message = "目标金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额最多保留两位小数")
    private BigDecimal targetAmount;

    private LocalDate deadline;

    @Min(value = 1, message = "优先级必须是1-3")
    @Max(value = 3, message = "优先级必须是1-3")
    private Short priority;

    @Min(value = 1, message = "状态必须是1-3")
    @Max(value = 3, message = "状态必须是1-3")
    private Short status;
}
