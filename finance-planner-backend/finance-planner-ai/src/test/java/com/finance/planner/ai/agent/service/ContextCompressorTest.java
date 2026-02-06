package com.finance.planner.ai.agent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ContextCompressorTest {

    private ContextCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = new ContextCompressor();
        ReflectionTestUtils.setField(compressor, "maxContextTokens", 100);
        ReflectionTestUtils.setField(compressor, "compressionEnabled", true);
    }

    @Test
    @DisplayName("Should not compress when under token limit")
    void compressIfNeeded_underLimit() {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(createMessage("system", "short"));
        messages.add(createMessage("user", "hi"));

        List<Map<String, Object>> result = compressor.compressIfNeeded(messages);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should not compress when disabled")
    void compressIfNeeded_disabled() {
        ReflectionTestUtils.setField(compressor, "compressionEnabled", false);

        List<Map<String, Object>> messages = createLargeConversation();

        List<Map<String, Object>> result = compressor.compressIfNeeded(messages);

        assertThat(result).isEqualTo(messages);
    }

    @Test
    @DisplayName("Should truncate tool results in older messages")
    void compressIfNeeded_truncateToolResults() {
        ReflectionTestUtils.setField(compressor, "maxContextTokens", 200);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(createMessage("system", "sys"));
        // Old tool result with long content
        Map<String, Object> toolMsg = createMessage("tool", "x".repeat(500));
        toolMsg.put("tool_call_id", "call_1");
        messages.add(toolMsg);
        // Recent messages
        messages.add(createMessage("assistant", "ok"));
        messages.add(createMessage("user", "more"));
        messages.add(createMessage("assistant", "done"));
        messages.add(createMessage("user", "end"));
        // Current user message
        messages.add(createMessage("user", "latest"));

        List<Map<String, Object>> result = compressor.compressIfNeeded(messages);

        // System + compressed older + recent + current user should be present
        assertThat(result).isNotEmpty();
        // First message should be system
        assertThat(result.get(0).get("role")).isEqualTo("system");
        // Last message should be current user
        assertThat(result.get(result.size() - 1).get("content")).isEqualTo("latest");
    }

    @Test
    @DisplayName("Should drop oldest pairs when still over limit")
    void compressIfNeeded_dropOldestPairs() {
        ReflectionTestUtils.setField(compressor, "maxContextTokens", 50);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(createMessage("system", "system prompt text"));
        // Many old conversation pairs
        for (int i = 0; i < 10; i++) {
            messages.add(createMessage("user", "question " + i + " with extra text padding"));
            messages.add(createMessage("assistant", "answer " + i + " with extra text padding"));
        }
        // Current user message
        messages.add(createMessage("user", "current question"));

        List<Map<String, Object>> result = compressor.compressIfNeeded(messages);

        // Should have fewer messages than original
        assertThat(result.size()).isLessThan(messages.size());
        // Should keep system message
        assertThat(result.get(0).get("role")).isEqualTo("system");
        // Should keep current user message
        assertThat(result.get(result.size() - 1).get("content")).isEqualTo("current question");
    }

    @Test
    @DisplayName("Should handle null or small message list gracefully")
    void compressIfNeeded_nullOrSmall() {
        assertThat(compressor.compressIfNeeded(null)).isNull();
        List<Map<String, Object>> single = List.of(createMessage("system", "hi"));
        assertThat(compressor.compressIfNeeded(single)).isEqualTo(single);
    }

    private List<Map<String, Object>> createLargeConversation() {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(createMessage("system", "x".repeat(300)));
        for (int i = 0; i < 5; i++) {
            messages.add(createMessage("user", "q" + i));
            messages.add(createMessage("assistant", "a" + i));
        }
        messages.add(createMessage("user", "current"));
        return messages;
    }

    private Map<String, Object> createMessage(String role, String content) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}
