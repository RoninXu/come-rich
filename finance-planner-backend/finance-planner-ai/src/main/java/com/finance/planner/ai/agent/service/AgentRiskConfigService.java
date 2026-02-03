package com.finance.planner.ai.agent.service;

import com.finance.planner.ai.config.AiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AgentRiskConfigService {

    private static final String KEY_PREFIX = "ai:agent:risk-threshold:";

    private final StringRedisTemplate stringRedisTemplate;
    private final AiConfig aiConfig;

    public BigDecimal getRiskThreshold(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(KEY_PREFIX + userId);
        if (value == null || value.isBlank()) {
            return aiConfig.getAgent().getRiskThreshold();
        }
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return aiConfig.getAgent().getRiskThreshold();
        }
    }

    public void setRiskThreshold(Long userId, BigDecimal threshold) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + userId, threshold.toPlainString());
    }
}
