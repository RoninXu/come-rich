package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;
    private final AiConfig aiConfig;

    private static final String KEY_PREFIX = "ai:ratelimit:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

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

    private String buildKey(Long userId) {
        String date = LocalDate.now().format(DATE_FORMAT);
        return KEY_PREFIX + userId + ":" + date;
    }
}
