package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.service.RateLimitService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AiConfig aiConfig;

    private static final String KEY_PREFIX = "ai:ratelimit:";
    private static final String RESERVE_PREFIX = "ai:reserve:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration RESERVE_TTL = Duration.ofMinutes(10);

    @Override
    public boolean isAllowed(Long userId) {
        String key = buildKey(userId);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }
        int limit = aiConfig.getRateLimit().getDailyLimit();
        boolean allowed = count != null && count <= limit;
        if (!allowed) {
            log.info("Rate limit exceeded for user {}: {}/{}", userId, count, limit);
        }
        return allowed;
    }

    @Override
    public int getRemainingChats(Long userId) {
        String key = buildKey(userId);
        String value = stringRedisTemplate.opsForValue().get(key);
        int used = value != null ? Integer.parseInt(value) : 0;
        int limit = aiConfig.getRateLimit().getDailyLimit();
        return Math.max(0, limit - used);
    }

    @Override
    public String reserveQuota(Long userId) {
        String key = buildKey(userId);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }
        int limit = aiConfig.getRateLimit().getDailyLimit();
        if (count != null && count > limit) {
            // Over limit - roll back
            stringRedisTemplate.opsForValue().decrement(key);
            log.info("Rate limit reservation denied for user {}: {}/{}", userId, count, limit);
            throw new BusinessException(ErrorCode.AI_RATE_LIMIT_EXCEEDED);
        }
        // Store reservation
        String reservationId = UUID.randomUUID().toString().replace("-", "");
        String reserveKey = RESERVE_PREFIX + reservationId;
        stringRedisTemplate.opsForValue().set(reserveKey, userId.toString(), RESERVE_TTL);
        log.debug("Quota reserved for user {}: reservationId={}", userId, reservationId);
        return reservationId;
    }

    @Override
    public void commitQuota(String reservationId) {
        String reserveKey = RESERVE_PREFIX + reservationId;
        stringRedisTemplate.delete(reserveKey);
        log.debug("Quota committed: reservationId={}", reservationId);
    }

    @Override
    public void releaseQuota(String reservationId) {
        String reserveKey = RESERVE_PREFIX + reservationId;
        String userId = stringRedisTemplate.opsForValue().get(reserveKey);
        if (userId != null) {
            String key = buildKey(Long.parseLong(userId));
            stringRedisTemplate.opsForValue().decrement(key);
            stringRedisTemplate.delete(reserveKey);
            log.debug("Quota released for user {}: reservationId={}", userId, reservationId);
        }
    }

    private String buildKey(Long userId) {
        String date = LocalDate.now().format(DATE_FORMAT);
        return KEY_PREFIX + userId + ":" + date;
    }
}
