package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.common.exception.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitService Reservation Tests")
class RateLimitServiceReservationTest {

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
    @DisplayName("reserveQuota - should return reservationId when under limit")
    void reserveQuota_underLimit() {
        when(valueOperations.increment(anyString())).thenReturn(5L);

        String reservationId = rateLimitService.reserveQuota(USER_ID);

        assertThat(reservationId).isNotNull().isNotEmpty();
        verify(valueOperations).set(startsWith("ai:reserve:"), eq(USER_ID.toString()), eq(Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("reserveQuota - should throw when over limit and rollback")
    void reserveQuota_overLimit() {
        when(valueOperations.increment(anyString())).thenReturn(11L);

        assertThatThrownBy(() -> rateLimitService.reserveQuota(USER_ID))
                .isInstanceOf(BusinessException.class);

        verify(valueOperations).decrement(anyString());
    }

    @Test
    @DisplayName("commitQuota - should delete reservation key")
    void commitQuota_deletesReservation() {
        rateLimitService.commitQuota("test-reservation-id");

        verify(stringRedisTemplate).delete("ai:reserve:test-reservation-id");
    }

    @Test
    @DisplayName("releaseQuota - should decrement count and delete reservation")
    void releaseQuota_decrementsAndDeletes() {
        when(valueOperations.get("ai:reserve:test-id")).thenReturn(USER_ID.toString());

        rateLimitService.releaseQuota("test-id");

        verify(valueOperations).decrement(anyString());
        verify(stringRedisTemplate).delete("ai:reserve:test-id");
    }
}
