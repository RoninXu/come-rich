package com.finance.planner.ai.agent.dto.metrics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentToolMetricDto {

    private String toolName;
    private Long totalCalls;
    private Long successfulCalls;
    private Long failedCalls;
    private Double successRate;
    private Double averageLatencyMs;
    private Double cacheHitRate;
}

