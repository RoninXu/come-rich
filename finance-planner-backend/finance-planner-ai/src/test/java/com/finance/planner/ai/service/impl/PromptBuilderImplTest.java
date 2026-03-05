package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.entity.AiConversation;
import com.finance.planner.ai.service.ConversationService;
import com.finance.planner.ai.time.TimeContext;
import com.finance.planner.ai.time.TimeContextProvider;
import com.finance.planner.analysis.dto.HealthScoreDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import com.finance.planner.analysis.service.HealthScoreService;
import com.finance.planner.analysis.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromptBuilderImpl Unit Tests")
class PromptBuilderImplTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private HealthScoreService healthScoreService;

    @Mock
    private TimeContextProvider timeContextProvider;

    @InjectMocks
    private PromptBuilderImpl promptBuilder;

    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "test-session";

    @Test
    @DisplayName("buildSystemPrompt - should contain key elements")
    void buildSystemPrompt_containsKeyElements() {
        String prompt = promptBuilder.buildSystemPrompt();

        assertThat(prompt).contains("Come Rich AI 财务顾问");
        assertThat(prompt).contains("不推荐任何具体的金融产品");
        assertThat(prompt).contains("风险声明");
        assertThat(prompt).contains("中文");
    }

    @Test
    @DisplayName("buildMessages - should include system, history, and user message")
    void buildMessages_includesAllParts() {
        when(timeContextProvider.getTimeContext(eq(USER_ID), isNull()))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .timezone("Asia/Shanghai")
                        .build());
        when(statisticsService.getMonthlySummary(eq(USER_ID), eq(2026), eq(2)))
                .thenReturn(MonthlySummaryDto.empty(2026, 2));
        when(healthScoreService.calculateHealthScore(USER_ID))
                .thenReturn(null);
        when(conversationService.getRecentMessages(eq(USER_ID), eq(SESSION_ID), eq(5)))
                .thenReturn(Collections.emptyList());

        List<Map<String, String>> messages = promptBuilder.buildMessages(USER_ID, SESSION_ID, "Hello");

        assertThat(messages).hasSize(2); // system + user (no history)
        assertThat(messages.get(0).get("role")).isEqualTo("system");
        assertThat(messages.get(1).get("role")).isEqualTo("user");
        assertThat(messages.get(1).get("content")).isEqualTo("Hello");
    }

    @Test
    @DisplayName("buildMessages - should include conversation history")
    void buildMessages_includesHistory() {
        when(timeContextProvider.getTimeContext(eq(USER_ID), isNull()))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .timezone("Asia/Shanghai")
                        .build());
        when(statisticsService.getMonthlySummary(eq(USER_ID), eq(2026), eq(2)))
                .thenReturn(MonthlySummaryDto.empty(2026, 2));
        when(healthScoreService.calculateHealthScore(USER_ID))
                .thenReturn(null);

        AiConversation userMsg = new AiConversation();
        userMsg.setRole("user");
        userMsg.setContent("Previous question");
        AiConversation assistantMsg = new AiConversation();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent("Previous answer");

        when(conversationService.getRecentMessages(eq(USER_ID), eq(SESSION_ID), eq(5)))
                .thenReturn(Arrays.asList(userMsg, assistantMsg));

        List<Map<String, String>> messages = promptBuilder.buildMessages(USER_ID, SESSION_ID, "New question");

        assertThat(messages).hasSize(4); // system + 2 history + user
        assertThat(messages.get(0).get("role")).isEqualTo("system");
        assertThat(messages.get(1).get("role")).isEqualTo("user");
        assertThat(messages.get(1).get("content")).isEqualTo("Previous question");
        assertThat(messages.get(2).get("role")).isEqualTo("assistant");
        assertThat(messages.get(2).get("content")).isEqualTo("Previous answer");
        assertThat(messages.get(3).get("role")).isEqualTo("user");
        assertThat(messages.get(3).get("content")).isEqualTo("New question");
    }

    @Test
    @DisplayName("buildMessages - should inject financial context into system prompt")
    void buildMessages_injectsFinancialContext() {
        when(timeContextProvider.getTimeContext(eq(USER_ID), isNull()))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .timezone("Asia/Shanghai")
                        .build());
        MonthlySummaryDto summary = MonthlySummaryDto.builder()
                .year(2026)
                .month(2)
                .totalIncome(new BigDecimal("10000"))
                .totalExpense(new BigDecimal("6000"))
                .balance(new BigDecimal("4000"))
                .savingsRate(new BigDecimal("40.00"))
                .transactionCount(25)
                .build();
        when(statisticsService.getMonthlySummary(eq(USER_ID), eq(2026), eq(2)))
                .thenReturn(summary);

        HealthScoreDto healthScore = HealthScoreDto.builder()
                .totalScore(78)
                .grade("C")
                .build();
        when(healthScoreService.calculateHealthScore(USER_ID)).thenReturn(healthScore);
        when(conversationService.getRecentMessages(eq(USER_ID), eq(SESSION_ID), eq(5)))
                .thenReturn(Collections.emptyList());

        List<Map<String, String>> messages = promptBuilder.buildMessages(USER_ID, SESSION_ID, "分析");

        String systemContent = messages.get(0).get("content");
        assertThat(systemContent).contains("10000");
        assertThat(systemContent).contains("6000");
        assertThat(systemContent).contains("78");
        assertThat(systemContent).contains("C");
    }

    @Test
    @DisplayName("buildMessages - should handle financial context errors gracefully")
    void buildMessages_handlesContextErrors() {
        when(timeContextProvider.getTimeContext(eq(USER_ID), isNull()))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .timezone("Asia/Shanghai")
                        .build());
        when(statisticsService.getMonthlySummary(eq(USER_ID), eq(2026), eq(2)))
                .thenThrow(new RuntimeException("DB error"));
        when(conversationService.getRecentMessages(eq(USER_ID), eq(SESSION_ID), eq(5)))
                .thenReturn(Collections.emptyList());

        List<Map<String, String>> messages = promptBuilder.buildMessages(USER_ID, SESSION_ID, "Hello");

        // Should still work, just without financial context
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).get("role")).isEqualTo("system");
    }

    @Test
    @DisplayName("buildMessages - should inject current date context")
    void buildMessages_injectsCurrentDate() {
        when(timeContextProvider.getTimeContext(eq(USER_ID), isNull()))
                .thenReturn(TimeContext.builder()
                        .serverDate("2026-02-04")
                        .timezone("Asia/Shanghai")
                        .build());
        when(statisticsService.getMonthlySummary(anyLong(), anyInt(), anyInt()))
                .thenReturn(MonthlySummaryDto.empty(2026, 2));
        when(conversationService.getRecentMessages(eq(USER_ID), eq(SESSION_ID), eq(5)))
                .thenReturn(Collections.emptyList());

        List<Map<String, String>> messages = promptBuilder.buildMessages(USER_ID, SESSION_ID, "Hello");

        String systemContent = messages.get(0).get("content");
        assertThat(systemContent).contains("Current date: 2026-02-04 (Asia/Shanghai)");
    }
}
