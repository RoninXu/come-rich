package com.finance.planner.budget.service.impl;

import com.finance.planner.ai.service.LlmClient;
import com.finance.planner.budget.dto.BudgetAiSuggestionDto;
import com.finance.planner.budget.dto.BudgetComparisonDto;
import com.finance.planner.budget.dto.BudgetSummaryDto;
import com.finance.planner.budget.service.BudgetAiService;
import com.finance.planner.budget.service.BudgetService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetAiServiceImpl implements BudgetAiService {

    private final BudgetService budgetService;
    private final LlmClient llmClient;

    @Override
    @Transactional(readOnly = true)
    public BudgetAiSuggestionDto generateSuggestions(Long userId, String yearMonth) {
        BudgetSummaryDto summary = budgetService.getBudgetSummary(userId, yearMonth);

        String prompt = buildPrompt(summary);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "你是一位专业的预算优化顾问。请根据用户的预算执行情况，给出具体可行的优化建议。" +
                        "请用以下格式回复：\n" +
                        "【总结】一段总结性分析\n" +
                        "【建议】\n1. 建议一\n2. 建议二\n...\n" +
                        "【风险提示】风险声明\n" +
                        "注意：不要推荐具体的金融产品，只推荐投资类别方向。"),
                Map.of("role", "user", "content", prompt)
        );

        try {
            String fullResponse = llmClient.streamChat(messages)
                    .collect(Collectors.joining())
                    .block();

            return parseAiResponse(fullResponse);
        } catch (Exception e) {
            log.error("AI budget suggestion generation failed for user {} month {}: {}",
                    userId, yearMonth, e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private String buildPrompt(BudgetSummaryDto summary) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("我的%s月预算执行情况：\n", summary.getYearMonth()));
        sb.append(String.format("- 总预算：%.2f 元\n", summary.getTotalBudget()));
        sb.append(String.format("- 总支出：%.2f 元\n", summary.getTotalSpent()));
        sb.append(String.format("- 剩余预算：%.2f 元\n", summary.getTotalRemaining()));
        sb.append(String.format("- 整体使用率：%.1f%%\n\n", summary.getOverallUtilization()));

        List<BudgetComparisonDto> categories = summary.getCategories();
        if (categories != null && !categories.isEmpty()) {
            sb.append("分类预算明细：\n");
            for (BudgetComparisonDto cat : categories) {
                sb.append(String.format("- %s：预算 %.2f 元，实际支出 %.2f 元，使用率 %.1f%%%s\n",
                        cat.getCategoryName() != null ? cat.getCategoryName() : "未知分类",
                        cat.getBudgetAmount(),
                        cat.getActualAmount(),
                        cat.getUtilizationPercentage(),
                        cat.isOverBudget() ? "（超支）" : ""));
            }
        }

        sb.append("\n请为我分析预算执行情况并给出优化建议。");
        return sb.toString();
    }

    private BudgetAiSuggestionDto parseAiResponse(String response) {
        if (response == null || response.isBlank()) {
            return BudgetAiSuggestionDto.builder()
                    .summary("暂无法生成建议，请稍后重试。")
                    .suggestions(List.of())
                    .riskWarning("投资有风险，决策需谨慎。本建议仅供参考，不构成投资建议。")
                    .build();
        }

        String summaryText = extractSection(response, "【总结】", "【建议】");
        List<String> suggestions = extractListSection(response, "【建议】", "【风险提示】");
        String riskWarning = extractSection(response, "【风险提示】", null);

        // Fallback: if structured parsing fails, treat whole response as summary
        if (summaryText.isBlank() && suggestions.isEmpty()) {
            summaryText = response.length() > 500 ? response.substring(0, 500) + "..." : response;
        }
        if (riskWarning.isBlank()) {
            riskWarning = "投资有风险，决策需谨慎。本建议仅供参考，不构成投资建议。";
        }

        return BudgetAiSuggestionDto.builder()
                .summary(summaryText.trim())
                .suggestions(suggestions)
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
