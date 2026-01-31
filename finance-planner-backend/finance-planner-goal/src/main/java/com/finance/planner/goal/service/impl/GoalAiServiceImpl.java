package com.finance.planner.goal.service.impl;

import com.finance.planner.ai.service.LlmClient;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.goal.dto.GoalAiPlanDto;
import com.finance.planner.goal.entity.FinancialGoal;
import com.finance.planner.goal.repository.GoalRepository;
import com.finance.planner.goal.service.GoalAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalAiServiceImpl implements GoalAiService {

    private final GoalRepository goalRepository;
    private final LlmClient llmClient;

    @Override
    @Transactional(readOnly = true)
    public GoalAiPlanDto generateAiPlan(Long userId, Long goalId) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));

        String prompt = buildPrompt(goal);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "你是一位专业的理财规划师。请根据用户的理财目标，给出具体可行的实现计划。" +
                        "请用以下格式回复：\n" +
                        "【总结】一段总结性建议\n" +
                        "【步骤】\n1. 步骤一\n2. 步骤二\n...\n" +
                        "【小贴士】\n- 贴士一\n- 贴士二\n...\n" +
                        "【风险提示】风险声明\n" +
                        "注意：不要推荐具体的金融产品，只推荐投资类别方向。"),
                Map.of("role", "user", "content", prompt)
        );

        try {
            // Collect all streamed chunks into a single response
            String fullResponse = llmClient.streamChat(messages)
                    .collect(Collectors.joining())
                    .block();

            return parseAiResponse(fullResponse);
        } catch (Exception e) {
            log.error("AI plan generation failed for goal {}: {}", goalId, e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private String buildPrompt(FinancialGoal goal) {
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        long days = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
        long months = Math.max(1, ChronoUnit.MONTHS.between(LocalDate.now(), goal.getDeadline()));
        BigDecimal monthlyNeeded = remaining.compareTo(BigDecimal.ZERO) > 0 ?
                remaining.divide(new BigDecimal(months), 2, RoundingMode.CEILING) : BigDecimal.ZERO;

        return String.format(
                "我的理财目标：\n" +
                "- 目标名称：%s\n" +
                "- 目标描述：%s\n" +
                "- 目标金额：%.2f 元\n" +
                "- 已存金额：%.2f 元\n" +
                "- 还差金额：%.2f 元\n" +
                "- 截止日期：%s（还剩 %d 天）\n" +
                "- 每月需存：%.2f 元\n\n" +
                "请为我制定一个详细的理财实现计划。",
                goal.getTitle(),
                goal.getDescription() != null ? goal.getDescription() : "无",
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                remaining,
                goal.getDeadline(),
                days,
                monthlyNeeded
        );
    }

    private GoalAiPlanDto parseAiResponse(String response) {
        if (response == null || response.isBlank()) {
            return GoalAiPlanDto.builder()
                    .summary("暂无法生成建议，请稍后重试。")
                    .steps(List.of())
                    .tips(List.of())
                    .riskWarning("投资有风险，决策需谨慎。本建议仅供参考，不构成投资建议。")
                    .build();
        }

        String summary = extractSection(response, "【总结】", "【步骤】");
        List<String> steps = extractListSection(response, "【步骤】", "【小贴士】");
        List<String> tips = extractListSection(response, "【小贴士】", "【风险提示】");
        String riskWarning = extractSection(response, "【风险提示】", null);

        // Fallback: if structured parsing fails, treat whole response as summary
        if (summary.isBlank() && steps.isEmpty()) {
            summary = response.length() > 500 ? response.substring(0, 500) + "..." : response;
        }
        if (riskWarning.isBlank()) {
            riskWarning = "投资有风险，决策需谨慎。本建议仅供参考，不构成投资建议。";
        }

        return GoalAiPlanDto.builder()
                .summary(summary.trim())
                .steps(steps)
                .tips(tips)
                .riskWarning(riskWarning.trim())
                .build();
    }

    private String extractSection(String text, String startTag, String endTag) {
        int start = text.indexOf(startTag);
        if (start == -1) return "";
        start += startTag.length();
        int end = endTag != null ? text.indexOf(endTag, start) : -1;
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    private List<String> extractListSection(String text, String startTag, String endTag) {
        String section = extractSection(text, startTag, endTag);
        if (section.isBlank()) return List.of();
        return Arrays.stream(section.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", "").replaceFirst("^-\\s*", ""))
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }
}
