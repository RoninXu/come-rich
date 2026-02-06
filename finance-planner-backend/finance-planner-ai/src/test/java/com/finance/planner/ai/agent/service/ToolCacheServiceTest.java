package com.finance.planner.ai.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.agent.tool.ToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToolCacheService Unit Tests")
class ToolCacheServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ToolCacheService toolCacheService;

    private static final String SESSION_ID = "session-123";

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        toolCacheService = new ToolCacheService(stringRedisTemplate, objectMapper);
    }

    @Test
    @DisplayName("get - should return cached result on cache hit")
    void get_cacheHit() throws Exception {
        ToolResult expected = ToolResult.success(Map.of("total", 5));
        String json = objectMapper.writeValueAsString(expected);
        when(valueOperations.get(anyString())).thenReturn(json);

        Optional<ToolResult> result = toolCacheService.get(SESSION_ID, "list_transactions", Map.of("page", 1));

        assertThat(result).isPresent();
        assertThat(result.get().isSuccess()).isTrue();
    }

    @Test
    @DisplayName("get - should return empty on cache miss")
    void get_cacheMiss() {
        when(valueOperations.get(anyString())).thenReturn(null);

        Optional<ToolResult> result = toolCacheService.get(SESSION_ID, "list_transactions", Map.of("page", 1));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("get - should skip non-cacheable tools")
    void get_nonCacheableTool() {
        Optional<ToolResult> result = toolCacheService.get(SESSION_ID, "create_transaction", Map.of("amount", 100));

        assertThat(result).isEmpty();
        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("put - should cache successful results with TTL")
    void put_successfulResult() {
        ToolResult result = ToolResult.success(Map.of("items", List.of()));

        toolCacheService.put(SESSION_ID, "list_transactions", Map.of(), result);

        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("put - should not cache failed results")
    void put_failedResult() {
        ToolResult result = ToolResult.failure("error");

        toolCacheService.put(SESSION_ID, "list_transactions", Map.of(), result);

        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("invalidateRelated - should delete related cache keys on write")
    void invalidateRelated_deletesKeys() {
        Set<String> keys = Set.of("key1", "key2");
        when(stringRedisTemplate.keys(anyString())).thenReturn(keys);

        toolCacheService.invalidateRelated(SESSION_ID, "create_transaction");

        // Should check for all related tools
        verify(stringRedisTemplate, atLeast(1)).keys(anyString());
        verify(stringRedisTemplate, atLeast(1)).delete(anySet());
    }
}
