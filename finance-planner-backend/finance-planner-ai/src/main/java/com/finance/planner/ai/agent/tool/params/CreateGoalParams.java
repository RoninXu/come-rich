package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGoalParams {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal targetAmount;

    @NotBlank
    private String deadline;

    @Min(1)
    @Max(3)
    private Short priority;
}
