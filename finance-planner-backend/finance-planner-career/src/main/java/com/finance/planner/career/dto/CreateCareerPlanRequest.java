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
public class CreateCareerPlanRequest {

    @Size(max = 100, message = "类型不能超过100个字符")
    private String careerType;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 200, message = "名称不能超过200个字符")
    private String title;

    private String description;

    private Integer matchScore;

    @DecimalMin(value = "0", message = "目标月收入不能为负数")
    private BigDecimal targetMonthlyIncome;

    private LocalDate startDate;
}
