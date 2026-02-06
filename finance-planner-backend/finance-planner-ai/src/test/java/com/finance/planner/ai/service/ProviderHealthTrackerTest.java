package com.finance.planner.ai.service;

import com.finance.planner.ai.config.AiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProviderHealthTracker Unit Tests")
class ProviderHealthTrackerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private AiConfig aiConfig;

    @InjectMocks
    private ProviderHealthTracker healthTracker;

    private static final String PROVIDER = "deepseek";
    private static final String KEY = "llm:health:deepseek";

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
        AiConfig.Fallback fallback = new AiConfig.Fallback();
        fallback.setFailureThreshold(3);
        fallback.setRecoveryTimeoutMinutes(5);
        lenient().when(aiConfig.getFallback()).thenReturn(fallback);
    }

    @Test
    @DisplayName("recordSuccess - should reset failure count and update latency")
    void recordSuccess_resetsFailures() {
        when(hashOperations.increment(KEY, "totalCalls", 1)).thenReturn(1L);
        when(hashOperations.get(KEY, "avgLatency")).thenReturn(null);

        healthTracker.recordSuccess(PROVIDER, 200);

        verify(hashOperations).put(KEY, "healthy", "true");
        verify(hashOperations).put(KEY, "consecutiveFailures", "0");
        verify(hashOperations).put(eq(KEY), eq("avgLatency"), eq("200"));
    }

    @Test
    @DisplayName("recordFailure - should mark unhealthy after threshold")
    void recordFailure_marksUnhealthyAfterThreshold() {
        when(hashOperations.increment(KEY, "consecutiveFailures", 1)).thenReturn(3L);

        healthTracker.recordFailure(PROVIDER, "connection timeout");

        verify(hashOperations).put(KEY, "healthy", "false");
        verify(stringRedisTemplate).expire(eq(KEY), any());
    }

    @Test
    @DisplayName("recordFailure - should not mark unhealthy below threshold")
    void recordFailure_belowThreshold() {
        when(hashOperations.increment(KEY, "consecutiveFailures", 1)).thenReturn(2L);

        healthTracker.recordFailure(PROVIDER, "timeout");

        verify(hashOperations, never()).put(KEY, "healthy", "false");
    }

    @Test
    @DisplayName("isHealthy - should return true when no data (new provider)")
    void isHealthy_noData() {
        when(hashOperations.entries(KEY)).thenReturn(Collections.emptyMap());

        boolean healthy = healthTracker.isHealthy(PROVIDER);

        assertThat(healthy).isTrue();
    }

    @Test
    @DisplayName("isHealthy - should return false when marked unhealthy")
    void isHealthy_markedUnhealthy() {
        Map<Object, Object> entries = new HashMap<>();
        entries.put("healthy", "false");
        when(hashOperations.entries(KEY)).thenReturn(entries);

        boolean healthy = healthTracker.isHealthy(PROVIDER);

        assertThat(healthy).isFalse();
    }

    @Test
    @DisplayName("recordSuccess - should compute average latency correctly")
    void recordSuccess_computesAverageLatency() {
        when(hashOperations.increment(KEY, "totalCalls", 1)).thenReturn(2L);
        when(hashOperations.get(KEY, "avgLatency")).thenReturn("100");

        healthTracker.recordSuccess(PROVIDER, 300);

        verify(hashOperations).put(eq(KEY), eq("avgLatency"), eq("200"));
    }
}
