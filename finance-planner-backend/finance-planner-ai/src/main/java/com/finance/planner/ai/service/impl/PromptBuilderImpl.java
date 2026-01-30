package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.entity.AiConversation;
import com.finance.planner.ai.service.ConversationService;
import com.finance.planner.ai.service.PromptBuilder;
import com.finance.planner.analysis.dto.HealthScoreDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;
import com.finance.planner.analysis.service.HealthScoreService;
import com.finance.planner.analysis.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromptBuilderImpl implements PromptBuilder {

    private final ConversationService conversationService;
    private final StatisticsService statisticsService;
    private final HealthScoreService healthScoreService;

    private static final String SYSTEM_PROMPT = """
            你是"Come Rich AI 财务顾问"，一位专业、友善的中文个人理财助手。

            你的职责：
            1. 分析用户的收支数据，提供个性化的财务建议
            2. 帮助用户理解自己的消费习惯和财务健康状况
            3. 提供储蓄、预算和理财规划建议
            4. 回答用户关于个人理财的各种问题

            重要规则：
            - 始终使用中文回答
            - 不推荐任何具体的金融产品（如特定基金、股票、保险产品）
            - 只建议投资类别（如"可以考虑配置一些低风险的固定收益类资产"）
            - 每次涉及投资建议时，必须附带风险提示
            - 回答要简洁实用，避免过于学术化的表述
            - 对于超出理财范围的问题，礼貌地引导回财务话题

            风险声明模板（需要时使用）：
            "以上建议仅供参考，不构成投资建议。投资有风险，请根据自身风险承受能力做出决策。"
            """;

    @Override
    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public List<Map<String, String>> buildMessages(Long userId, String sessionId, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 1. System prompt with financial context
        String systemContent = buildSystemPromptWithContext(userId);
        messages.add(createMessage("system", systemContent));

        // 2. Recent conversation history (last 5 rounds)
        List<AiConversation> recentMessages = conversationService.getRecentMessages(userId, sessionId, 5);
        for (AiConversation msg : recentMessages) {
            messages.add(createMessage(msg.getRole(), msg.getContent()));
        }

        // 3. Current user message
        messages.add(createMessage("user", userMessage));

        return messages;
    }

    private String buildSystemPromptWithContext(Long userId) {
        StringBuilder sb = new StringBuilder(SYSTEM_PROMPT);

        try {
            LocalDate now = LocalDate.now();
            MonthlySummaryDto summary = statisticsService.getMonthlySummary(userId, now.getYear(), now.getMonthValue());
            if (summary != null && summary.getTransactionCount() > 0) {
                sb.append("\n\n用户本月财务概况：\n");
                sb.append("- 本月总收入：").append(summary.getTotalIncome()).append(" 元\n");
                sb.append("- 本月总支出：").append(summary.getTotalExpense()).append(" 元\n");
                sb.append("- 本月结余：").append(summary.getBalance()).append(" 元\n");
                sb.append("- 储蓄率：").append(summary.getSavingsRate()).append("%\n");
                sb.append("- 交易笔数：").append(summary.getTransactionCount()).append("\n");
            }

            HealthScoreDto healthScore = healthScoreService.calculateHealthScore(userId);
            if (healthScore != null) {
                sb.append("\n用户财务健康评分：\n");
                sb.append("- 总分：").append(healthScore.getTotalScore()).append("/100\n");
                sb.append("- 等级：").append(healthScore.getGrade()).append("\n");
            }
        } catch (Exception e) {
            log.warn("Failed to build financial context for user {}: {}", userId, e.getMessage());
        }

        return sb.toString();
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}
