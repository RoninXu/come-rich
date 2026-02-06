package com.finance.planner.ai.agent.util;

import java.util.List;
import java.util.Map;

public final class TokenEstimator {

    private TokenEstimator() {
    }

    /**
     * Estimates token count for a string.
     * Approximation: ~1 token per 2 characters for mixed Chinese/English content.
     */
    public static int estimateTokens(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return (content.length() + 1) / 2;
    }

    /**
     * Estimates total token count for a list of chat messages.
     */
    public static int estimateMessages(List<Map<String, Object>> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Map<String, Object> message : messages) {
            Object content = message.get("content");
            if (content instanceof String s) {
                total += estimateTokens(s);
            }
            Object toolCalls = message.get("tool_calls");
            if (toolCalls != null) {
                total += estimateTokens(toolCalls.toString());
            }
        }
        return total;
    }
}
