package com.finance.planner.integration.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        lenient().when(valueOps.increment(anyString())).thenReturn(1L);
        lenient().when(valueOps.get(anyString())).thenReturn("0");
        return template;
    }
}
