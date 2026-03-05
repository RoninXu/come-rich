package com.finance.planner.ai.agent.dto.metrics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AgentMetricsTimelinePointDto {

    private LocalDate date;
    private Long totalCalls;
    private Long successfulCalls;
    private Long failedCalls;
    private Double successRate;
    private Double averageLatencyMs;
}

