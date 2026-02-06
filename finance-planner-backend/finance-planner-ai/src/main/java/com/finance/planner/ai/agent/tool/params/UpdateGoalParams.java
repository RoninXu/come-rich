package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateGoalParams {

    @NotNull
    private Long goalId;

    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @DecimalMin("0.01")
    private BigDecimal targetAmount;

    private String deadline;

    @Min(1)
    @Max(3)
    private Short priority;

    @Min(1)
    @Max(3)
    private Short status;
}
