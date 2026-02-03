package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryStatsParams {

    @NotNull
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer month;

    private Short type;
}
