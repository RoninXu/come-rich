package com.finance.planner.ai.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.agent.tool.ToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolCacheService {

    private static final String KEY_PREFIX = "agent:tool:cache:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private static final Set<String> CACHEABLE_TOOLS = Set.of(
            "list_transactions", "get_recent_transactions", "get_categories", "get_category_stats",
            "get_monthly_summary", "get_health_score", "get_budgets", "get_budget_summary",
            "get_budget_trend", "list_goals", "get_risk_assessment", "get_investment_advice",
            "get_asset_allocation", "list_career_plans", "get_user_profile"
    );

    private static final Map<String, Set<String>> INVALIDATION_MAP = Map.ofEntries(
            Map.entry("create_transaction", Set.of("list_transactions", "get_recent_transactions", "get_monthly_summary", "get_category_stats", "get_health_score")),
            Map.entry("update_transaction", Set.of("list_transactions", "get_recent_transactions", "get_monthly_summary", "get_category_stats", "get_health_score")),
            Map.entry("delete_transaction", Set.of("list_transactions", "get_recent_transactions", "get_monthly_summary", "get_category_stats", "get_health_score")),
            Map.entry("set_budget", Set.of("get_budgets", "get_budget_summary", "get_budget_trend")),
            Map.entry("delete_budget", Set.of("get_budgets", "get_budget_summary", "get_budget_trend")),
            Map.entry("create_goal", Set.of("list_goals")),
            Map.entry("update_goal", Set.of("list_goals")),
            Map.entry("delete_goal", Set.of("list_goals")),
            Map.entry("add_goal_progress", Set.of("list_goals")),
            Map.entry("create_career_plan", Set.of("list_career_plans"))
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<ToolResult> get(String sessionId, String toolName, Map<String, Object> arguments) {
        if (!isCacheable(toolName)) {
            return Optional.empty();
        }
        String key = buildKey(sessionId, toolName, arguments);
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null) {
            return Optional.empty();
        }
        try {
            ToolResult result = objectMapper.readValue(cached, ToolResult.class);
            log.debug("Tool cache hit: {} sessionId={}", toolName, sessionId);
            return Optional.of(result);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached tool result for {}: {}", toolName, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(String sessionId, String toolName, Map<String, Object> arguments, ToolResult result) {
        if (!isCacheable(toolName) || !result.isSuccess()) {
            return;
        }
        String key = buildKey(sessionId, toolName, arguments);
        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(key, json, DEFAULT_TTL);
            log.debug("Tool cache put: {} sessionId={}", toolName, sessionId);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache tool result for {}: {}", toolName, e.getMessage());
        }
    }

    public void invalidateRelated(String sessionId, String writeTool) {
        Set<String> relatedTools = INVALIDATION_MAP.get(writeTool);
        if (relatedTools == null || relatedTools.isEmpty()) {
            return;
        }
        for (String toolName : relatedTools) {
            String pattern = KEY_PREFIX + sessionId + ":" + toolName + ":*";
            Set<String> keys = stringRedisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.debug("Invalidated {} cache entries for tool {} in session {}", keys.size(), toolName, sessionId);
            }
        }
    }

    public boolean isCacheable(String toolName) {
        return CACHEABLE_TOOLS.contains(toolName);
    }

    private String buildKey(String sessionId, String toolName, Map<String, Object> arguments) {
        String argsHash = md5(arguments);
        return KEY_PREFIX + sessionId + ":" + toolName + ":" + argsHash;
    }

    private String md5(Map<String, Object> arguments) {
        try {
            String sorted = objectMapper.writeValueAsString(new TreeMap<>(arguments));
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sorted.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(arguments.hashCode());
        }
    }
}
