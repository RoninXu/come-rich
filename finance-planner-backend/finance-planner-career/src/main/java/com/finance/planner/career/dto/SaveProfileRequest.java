package com.finance.planner.career.dto;

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
public class SaveProfileRequest {

    @Size(max = 100, message = "职业不能超过100个字符")
    private String occupation;

    private String skills;

    @Min(value = 1, message = "每周可用时间至少1小时")
    @Max(value = 168, message = "每周可用时间不能超过168小时")
    private Integer availableHoursPerWeek;

    @DecimalMin(value = "0", message = "期望收入不能为负数")
    private BigDecimal incomeExpectation;

    private String interests;

    @Size(max = 50, message = "经验等级不能超过50个字符")
    private String experienceLevel;
}
