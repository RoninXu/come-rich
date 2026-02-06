package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ListGoalsParams {

    @Min(1)
    @Max(3)
    private Short status;
}
