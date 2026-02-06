package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCareerPlanParams {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 100)
    private String careerType;

    private String description;

    private Integer matchScore;

    @DecimalMin("0")
    private BigDecimal targetMonthlyIncome;

    private String startDate;
}
