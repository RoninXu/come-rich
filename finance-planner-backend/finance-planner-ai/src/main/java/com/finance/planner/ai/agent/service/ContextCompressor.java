package com.finance.planner.ai.agent.service;

import com.finance.planner.ai.agent.util.TokenEstimator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ContextCompressor {

    private static final int TOOL_RESULT_MAX_LENGTH = 200;
    private static final int RECENT_MESSAGES_TO_KEEP = 4;

    @Value("${ai.agent.max-context-tokens:6000}")
    private int maxContextTokens;

    @Value("${ai.agent.compression-enabled:true}")
    private boolean compressionEnabled;

    public List<Map<String, Object>> compressIfNeeded(List<Map<String, Object>> messages) {
        if (!compressionEnabled || messages == null || messages.size() <= 2) {
            return messages;
        }

        int tokens = TokenEstimator.estimateMessages(messages);
        if (tokens <= maxContextTokens) {
            return messages;
        }

        log.info("Context compression triggered: estimated {} tokens exceeds limit {}", tokens, maxContextTokens);
        return compress(messages);
    }

    private List<Map<String, Object>> compress(List<Map<String, Object>> messages) {
        // System message is always first, current user message is always last
        Map<String, Object> systemMessage = messages.get(0);
        Map<String, Object> currentUserMessage = messages.get(messages.size() - 1);

        List<Map<String, Object>> middleMessages = messages.subList(1, messages.size() - 1);
        int middleSize = middleMessages.size();

        // Step 1: Split into older messages and recent messages
        int recentStart = Math.max(0, middleSize - RECENT_MESSAGES_TO_KEEP);
        List<Map<String, Object>> olderMessages = new ArrayList<>(middleMessages.subList(0, recentStart));
        List<Map<String, Object>> recentMessages = new ArrayList<>(middleMessages.subList(recentStart, middleSize));

        // Step 2: Truncate tool results in older messages
        List<Map<String, Object>> compressedOlder = new ArrayList<>();
        for (Map<String, Object> msg : olderMessages) {
            compressedOlder.add(truncateToolResult(msg));
        }

        // Step 3: Rebuild and check if still over limit
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(systemMessage);
        result.addAll(compressedOlder);
        result.addAll(recentMessages);
        result.add(currentUserMessage);

        int tokens = TokenEstimator.estimateMessages(result);
        if (tokens <= maxContextTokens) {
            log.info("Context compressed to {} tokens by truncating tool results", tokens);
            return result;
        }

        // Step 4: Drop oldest conversation pairs until under limit
        while (compressedOlder.size() >= 2 && TokenEstimator.estimateMessages(result) > maxContextTokens) {
            compressedOlder.remove(0);
            compressedOlder.remove(0);
            result = new ArrayList<>();
            result.add(systemMessage);
            result.addAll(compressedOlder);
            result.addAll(recentMessages);
            result.add(currentUserMessage);
        }

        // If still over limit and there are remaining older messages, drop them all
        if (TokenEstimator.estimateMessages(result) > maxContextTokens && !compressedOlder.isEmpty()) {
            result = new ArrayList<>();
            result.add(systemMessage);
            result.addAll(recentMessages);
            result.add(currentUserMessage);
        }

        log.info("Context compressed to {} tokens ({} messages)", TokenEstimator.estimateMessages(result), result.size());
        return result;
    }

    private Map<String, Object> truncateToolResult(Map<String, Object> message) {
        if (!"tool".equals(message.get("role"))) {
            return message;
        }

        Object content = message.get("content");
        if (content instanceof String s && s.length() > TOOL_RESULT_MAX_LENGTH) {
            Map<String, Object> truncated = new LinkedHashMap<>(message);
            truncated.put("content", s.substring(0, TOOL_RESULT_MAX_LENGTH) + "...[truncated]");
            return truncated;
        }
        return message;
    }
}
