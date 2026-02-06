package com.finance.planner.ai.agent.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TokenEstimatorTest {

    @Test
    @DisplayName("Should estimate tokens for null or empty string as 0")
    void estimateTokens_nullOrEmpty() {
        assertThat(TokenEstimator.estimateTokens(null)).isZero();
        assertThat(TokenEstimator.estimateTokens("")).isZero();
    }

    @Test
    @DisplayName("Should estimate tokens for string based on character count")
    void estimateTokens_normalString() {
        // 10 chars -> ~5 tokens
        assertThat(TokenEstimator.estimateTokens("1234567890")).isEqualTo(5);
        // 11 chars -> ~6 tokens
        assertThat(TokenEstimator.estimateTokens("12345678901")).isEqualTo(6);
    }

    @Test
    @DisplayName("Should estimate tokens for Chinese text")
    void estimateTokens_chinese() {
        String chinese = "你好世界测试"; // 6 chars -> 3 tokens
        assertThat(TokenEstimator.estimateTokens(chinese)).isEqualTo(3);
    }

    @Test
    @DisplayName("Should estimate message list tokens")
    void estimateMessages_list() {
        Map<String, Object> msg1 = new LinkedHashMap<>();
        msg1.put("role", "system");
        msg1.put("content", "1234567890"); // 5 tokens

        Map<String, Object> msg2 = new LinkedHashMap<>();
        msg2.put("role", "user");
        msg2.put("content", "12345678901234567890"); // 10 tokens

        List<Map<String, Object>> messages = List.of(msg1, msg2);
        assertThat(TokenEstimator.estimateMessages(messages)).isEqualTo(15);
    }

    @Test
    @DisplayName("Should handle null or empty message list")
    void estimateMessages_nullOrEmpty() {
        assertThat(TokenEstimator.estimateMessages(null)).isZero();
        assertThat(TokenEstimator.estimateMessages(List.of())).isZero();
    }
}
