package com.finance.planner.ai.ocr.dto;

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
public class OcrConfirmRequest {

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "日期不能为空")
    private LocalDate transactionDate;

    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;

    @Size(max = 100, message = "商家名称不能超过100个字符")
    private String merchant;
}
