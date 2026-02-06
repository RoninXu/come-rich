package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateGoalAiPlanParams {

    @NotNull
    private Long goalId;
}
