package com.finance.planner.ai.service;

import com.finance.planner.ai.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderHealthTracker {

    private static final String KEY_PREFIX = "llm:health:";
    private static final String FIELD_HEALTHY = "healthy";
    private static final String FIELD_CONSECUTIVE_FAILURES = "consecutiveFailures";
    private static final String FIELD_LAST_FAILURE = "lastFailure";
    private static final String FIELD_AVG_LATENCY = "avgLatency";
    private static final String FIELD_TOTAL_CALLS = "totalCalls";

    private final StringRedisTemplate stringRedisTemplate;
    private final AiConfig aiConfig;

    public void recordSuccess(String provider, long latencyMs) {
        String key = KEY_PREFIX + provider;
        stringRedisTemplate.opsForHash().put(key, FIELD_HEALTHY, "true");
        stringRedisTemplate.opsForHash().put(key, FIELD_CONSECUTIVE_FAILURES, "0");
        updateAverageLatency(key, latencyMs);
        log.debug("Provider {} success, latency={}ms", provider, latencyMs);
    }

    public void recordFailure(String provider, String errorMessage) {
        String key = KEY_PREFIX + provider;
        Long failures = stringRedisTemplate.opsForHash().increment(key, FIELD_CONSECUTIVE_FAILURES, 1);
        stringRedisTemplate.opsForHash().put(key, FIELD_LAST_FAILURE, String.valueOf(System.currentTimeMillis()));

        int threshold = aiConfig.getFallback().getFailureThreshold();
        if (failures != null && failures >= threshold) {
            stringRedisTemplate.opsForHash().put(key, FIELD_HEALTHY, "false");
            int recoveryMinutes = aiConfig.getFallback().getRecoveryTimeoutMinutes();
            stringRedisTemplate.expire(key, Duration.ofMinutes(recoveryMinutes));
            log.warn("Provider {} marked unhealthy after {} consecutive failures: {}",
                    provider, failures, errorMessage);
        } else {
            log.info("Provider {} failure {}/{}: {}", provider, failures, threshold, errorMessage);
        }
    }

    public boolean isHealthy(String provider) {
        String key = KEY_PREFIX + provider;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return true; // No data means never failed, assume healthy
        }
        String healthy = (String) entries.get(FIELD_HEALTHY);
        return !"false".equals(healthy);
    }

    public long getAverageLatency(String provider) {
        String key = KEY_PREFIX + provider;
        Object avgLatency = stringRedisTemplate.opsForHash().get(key, FIELD_AVG_LATENCY);
        if (avgLatency == null) {
            return 0;
        }
        try {
            return Long.parseLong(avgLatency.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateAverageLatency(String key, long latencyMs) {
        Long totalCalls = stringRedisTemplate.opsForHash().increment(key, FIELD_TOTAL_CALLS, 1);
        Object currentAvg = stringRedisTemplate.opsForHash().get(key, FIELD_AVG_LATENCY);
        long prevAvg = 0;
        if (currentAvg != null) {
            try {
                prevAvg = Long.parseLong(currentAvg.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        long newAvg = totalCalls != null && totalCalls > 1
                ? prevAvg + (latencyMs - prevAvg) / totalCalls
                : latencyMs;
        stringRedisTemplate.opsForHash().put(key, FIELD_AVG_LATENCY, String.valueOf(newAvg));
    }
}
