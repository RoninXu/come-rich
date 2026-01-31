package com.finance.planner.goal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAiPlanDto {

    private String summary;
    private List<String> steps;
    private List<String> tips;
    private String riskWarning;
}
