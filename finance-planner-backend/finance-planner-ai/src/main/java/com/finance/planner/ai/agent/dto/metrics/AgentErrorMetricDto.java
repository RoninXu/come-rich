package com.finance.planner.ai.agent.dto.metrics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentErrorMetricDto {

    private String errorMessage;
    private Long count;
}

