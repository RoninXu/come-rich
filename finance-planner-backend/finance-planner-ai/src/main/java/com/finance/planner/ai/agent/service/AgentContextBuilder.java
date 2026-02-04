package com.finance.planner.ai.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.entity.AiConversation;
import com.finance.planner.ai.service.ConversationService;
import com.finance.planner.analysis.dto.HealthScoreDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import com.finance.planner.analysis.service.HealthScoreService;
import com.finance.planner.analysis.service.StatisticsService;
import com.finance.planner.ai.time.TimeContext;
import com.finance.planner.ai.time.TimeContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentContextBuilder {

    private final ConversationService conversationService;
    private final StatisticsService statisticsService;
    private final HealthScoreService healthScoreService;
    private final TimeContextProvider timeContextProvider;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            你是“Come Rich AI Agent”，一个可以执行记账、分析、预算等操作的智能助理。
            你可以通过系统提供的工具来读取或更新数据，不要编造数据。
            当用户提出明确的操作请求时，优先调用工具。
            涉及删除或高金额写入操作时必须等待确认。
            回答使用中文，简洁清晰。
            """;

    public List<Map<String, Object>> buildMessages(Long userId, String sessionId, String userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(buildSystemMessage(userId));

        List<AiConversation> recentMessages = conversationService.getRecentMessagesWithLimit(userId, sessionId, 20);
        for (AiConversation msg : recentMessages) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("role", msg.getRole());
            map.put("content", msg.getContent());
            if (msg.getToolCalls() != null) {
                map.put("tool_calls", parseToolCalls(msg.getToolCalls()));
            }
            if (msg.getToolCallId() != null) {
                map.put("tool_call_id", msg.getToolCallId());
            }
            messages.add(map);
        }

        Map<String, Object> currentUser = new LinkedHashMap<>();
        currentUser.put("role", "user");
        currentUser.put("content", userMessage);
        messages.add(currentUser);

        return messages;
    }

    private Map<String, Object> buildSystemMessage(Long userId) {
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("role", "system");
        system.put("content", buildSystemPromptWithContext(userId));
        return system;
    }

    private String buildSystemPromptWithContext(Long userId) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);
        try {
            TimeContext timeContext = timeContextProvider.getTimeContext(userId, null);
            sb.append("\nCurrent date: ")
                    .append(timeContext.getServerDate())
                    .append(" (")
                    .append(timeContext.getTimezone())
                    .append("). Use this to resolve relative dates like 今天/昨天/明天/前天/后天.");
            LocalDate now = LocalDate.parse(timeContext.getServerDate());
            MonthlySummaryDto summary = statisticsService.getMonthlySummary(userId, now.getYear(), now.getMonthValue());
            if (summary != null && summary.getTransactionCount() > 0) {
                sb.append("\n当前月度概况：\n");
                sb.append("- 总收入：").append(summary.getTotalIncome()).append(" 元\n");
                sb.append("- 总支出：").append(summary.getTotalExpense()).append(" 元\n");
                sb.append("- 结余：").append(summary.getBalance()).append(" 元\n");
                sb.append("- 储蓄率：").append(summary.getSavingsRate()).append("%\n");
            }
            HealthScoreDto healthScore = healthScoreService.calculateHealthScore(userId);
            if (healthScore != null) {
                sb.append("\n财务健康评分：").append(healthScore.getTotalScore()).append("/100，等级：")
                        .append(healthScore.getGrade()).append("\n");
            }
        } catch (Exception e) {
            log.warn("Failed to build agent context for user {}: {}", userId, e.getMessage());
        }
        return sb.toString();
    }

    private Object parseToolCalls(String toolCallsJson) {
        try {
            return objectMapper.readValue(toolCallsJson, Object.class);
        } catch (Exception e) {
            return toolCallsJson;
        }
    }
}
