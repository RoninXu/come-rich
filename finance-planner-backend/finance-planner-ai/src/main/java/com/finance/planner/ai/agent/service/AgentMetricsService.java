package com.finance.planner.ai.agent.service;

import com.finance.planner.ai.agent.entity.AgentToolMetrics;
import com.finance.planner.ai.agent.repository.AgentToolMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentMetricsService {

    private final AgentToolMetricsRepository metricsRepository;

    @Async
    public void recordToolExecution(Long userId, String sessionId, String toolName,
                                     boolean success, long latencyMs, String errorMessage, boolean cached) {
        try {
            AgentToolMetrics metrics = new AgentToolMetrics();
            metrics.setUserId(userId);
            metrics.setSessionId(sessionId);
            metrics.setToolName(toolName);
            metrics.setSuccess(success);
            metrics.setLatencyMs((int) latencyMs);
            metrics.setErrorMessage(errorMessage);
            metrics.setCached(cached);
            metricsRepository.save(metrics);
            log.debug("Recorded tool metrics: tool={} success={} latency={}ms cached={}", toolName, success, latencyMs, cached);
        } catch (Exception e) {
            log.error("Failed to record tool metrics: {}", e.getMessage());
        }
    }
}
