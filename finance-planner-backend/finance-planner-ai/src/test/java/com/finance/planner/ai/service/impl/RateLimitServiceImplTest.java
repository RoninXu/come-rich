package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitServiceImpl Unit Tests")
class RateLimitServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AiConfig aiConfig;

    @InjectMocks
    private RateLimitServiceImpl rateLimitService;

    private static final Long USER_ID = 1L;
    private static final int DAILY_LIMIT = 10;

    @BeforeEach
    void setUp() {
        AiConfig.RateLimit rateLimit = new AiConfig.RateLimit();
        rateLimit.setDailyLimit(DAILY_LIMIT);
        lenient().when(aiConfig.getRateLimit()).thenReturn(rateLimit);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("isAllowed - should allow when under limit")
    void isAllowed_underLimit() {
        when(valueOperations.increment(any())).thenReturn(5L);

        boolean allowed = rateLimitService.isAllowed(USER_ID);

        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("isAllowed - should set expiry on first request")
    void isAllowed_setsExpiryOnFirstRequest() {
        when(valueOperations.increment(any())).thenReturn(1L);

        rateLimitService.isAllowed(USER_ID);

        verify(stringRedisTemplate).expire(any(), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("isAllowed - should not set expiry on subsequent requests")
    void isAllowed_noExpiryOnSubsequent() {
        when(valueOperations.increment(any())).thenReturn(3L);

        rateLimitService.isAllowed(USER_ID);

        verify(stringRedisTemplate, never()).expire(any(), any(Duration.class));
    }

    @Test
    @DisplayName("isAllowed - should deny when at limit")
    void isAllowed_atLimit() {
        when(valueOperations.increment(any())).thenReturn(11L);

        boolean allowed = rateLimitService.isAllowed(USER_ID);

        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("isAllowed - should allow at exact limit")
    void isAllowed_exactLimit() {
        when(valueOperations.increment(any())).thenReturn(10L);

        boolean allowed = rateLimitService.isAllowed(USER_ID);

        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("getRemainingChats - should return remaining count")
    void getRemainingChats_returnsRemaining() {
        String key = buildExpectedKey(USER_ID);
        when(valueOperations.get(key)).thenReturn("7");

        int remaining = rateLimitService.getRemainingChats(USER_ID);

        assertThat(remaining).isEqualTo(3);
    }

    @Test
    @DisplayName("getRemainingChats - should return full limit when no usage")
    void getRemainingChats_noUsage() {
        String key = buildExpectedKey(USER_ID);
        when(valueOperations.get(key)).thenReturn(null);

        int remaining = rateLimitService.getRemainingChats(USER_ID);

        assertThat(remaining).isEqualTo(DAILY_LIMIT);
    }

    @Test
    @DisplayName("getRemainingChats - should return zero when exceeded")
    void getRemainingChats_exceeded() {
        String key = buildExpectedKey(USER_ID);
        when(valueOperations.get(key)).thenReturn("15");

        int remaining = rateLimitService.getRemainingChats(USER_ID);

        assertThat(remaining).isEqualTo(0);
    }

    private String buildExpectedKey(Long userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ai:ratelimit:" + userId + ":" + date;
    }
}
