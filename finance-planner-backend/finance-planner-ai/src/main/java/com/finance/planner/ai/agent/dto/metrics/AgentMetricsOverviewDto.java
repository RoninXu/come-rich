package com.finance.planner.ai.agent.dto.metrics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentMetricsOverviewDto {

    private Long totalCalls;
    private Long successfulCalls;
    private Long failedCalls;
    private Long totalSessions;
    private Double successRate;
    private Double averageLatencyMs;
    private Double cacheHitRate;
}

