package com.finance.planner.career.service.impl;

import com.finance.planner.ai.service.LlmClient;
import com.finance.planner.career.dto.CareerRecommendationDto;
import com.finance.planner.career.entity.CareerPlan;
import com.finance.planner.career.entity.UserProfile;
import com.finance.planner.career.repository.CareerPlanRepository;
import com.finance.planner.career.repository.UserProfileRepository;
import com.finance.planner.career.service.CareerRecommendationService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerRecommendationServiceImpl implements CareerRecommendationService {

    private final UserProfileRepository userProfileRepository;
    private final CareerPlanRepository careerPlanRepository;
    private final LlmClient llmClient;

    @Override
    @Transactional(readOnly = true)
    public List<CareerRecommendationDto> getRecommendations(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PROFILE_INCOMPLETE));

        if (profile.getOccupation() == null || profile.getSkills() == null) {
            throw new BusinessException(ErrorCode.CAREER_PROFILE_INCOMPLETE);
        }

        String prompt = buildRecommendationPrompt(profile);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "你是一位职业规划顾问。根据用户的背景信息，推荐3-5个适合的副业方向。\n" +
                        "请用以下格式回复每个推荐，用空行分隔：\n" +
                        "【推荐N】\n" +
                        "类型: 副业类型\n" +
                        "名称: 具体副业名称\n" +
                        "描述: 简要描述\n" +
                        "匹配度: 1-100的数字\n" +
                        "预估月收入: 金额范围\n" +
                        "所需技能: 相关技能\n" +
                        "时间投入: 每周需要投入的时间\n" +
                        "注意：不要推荐违法或高风险的副业。"),
                Map.of("role", "user", "content", prompt)
        );

        try {
            String fullResponse = llmClient.streamChat(messages)
                    .collect(Collectors.joining())
                    .block();

            return parseRecommendations(fullResponse);
        } catch (Exception e) {
            log.error("AI career recommendation failed for user {}: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.CAREER_RECOMMENDATION_FAILED);
        }
    }

    @Override
    @Transactional
    public String generateStartupPlan(Long userId, Long planId) {
        CareerPlan plan = careerPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAREER_PLAN_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);

        String prompt = buildStartupPlanPrompt(plan, profile);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "你是一位副业创业顾问。请为用户制定一个详细的90天启动计划。" +
                        "计划应包含每周的具体行动步骤、里程碑和注意事项。" +
                        "请用markdown格式回复。"),
                Map.of("role", "user", "content", prompt)
        );

        try {
            String startupPlan = llmClient.streamChat(messages)
                    .collect(Collectors.joining())
                    .block();

            plan.setStartupPlan(startupPlan);
            careerPlanRepository.save(plan);
            log.info("Generated startup plan for career plan {} user {}", planId, userId);

            return startupPlan;
        } catch (Exception e) {
            log.error("AI startup plan generation failed for plan {}: {}", planId, e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private String buildRecommendationPrompt(UserProfile profile) {
        return String.format(
                "我的背景信息：\n" +
                "- 职业：%s\n" +
                "- 技能：%s\n" +
                "- 兴趣爱好：%s\n" +
                "- 经验水平：%s\n" +
                "- 每周可用时间：%d 小时\n" +
                "- 期望月收入：%s 元\n\n" +
                "请根据我的背景推荐适合的副业方向。",
                profile.getOccupation(),
                profile.getSkills(),
                profile.getInterests() != null ? profile.getInterests() : "未填写",
                profile.getExperienceLevel() != null ? profile.getExperienceLevel() : "初级",
                profile.getAvailableHoursPerWeek() != null ? profile.getAvailableHoursPerWeek() : 10,
                profile.getIncomeExpectation() != null ? profile.getIncomeExpectation().toString() : "3000-5000"
        );
    }

    private String buildStartupPlanPrompt(CareerPlan plan, UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("副业计划：\n");
        sb.append("- 名称：").append(plan.getTitle()).append("\n");
        sb.append("- 类型：").append(plan.getCareerType() != null ? plan.getCareerType() : "未分类").append("\n");
        sb.append("- 目标月收入：").append(plan.getTargetMonthlyIncome() != null ? plan.getTargetMonthlyIncome() + " 元" : "待定").append("\n");
        if (plan.getDescription() != null) {
            sb.append("- 描述：").append(plan.getDescription()).append("\n");
        }
        if (profile != null) {
            sb.append("\n我的背景：\n");
            sb.append("- 职业：").append(profile.getOccupation()).append("\n");
            sb.append("- 技能：").append(profile.getSkills()).append("\n");
            sb.append("- 每周可用时间：").append(profile.getAvailableHoursPerWeek()).append(" 小时\n");
        }
        sb.append("\n请制定90天启动计划。");
        return sb.toString();
    }

    private List<CareerRecommendationDto> parseRecommendations(String response) {
        List<CareerRecommendationDto> recommendations = new ArrayList<>();
        if (response == null || response.isBlank()) return recommendations;

        // Split by recommendation sections
        String[] sections = response.split("【推荐\\d+】");
        for (String section : sections) {
            if (section.isBlank()) continue;

            CareerRecommendationDto dto = CareerRecommendationDto.builder()
                    .careerType(extractField(section, "类型"))
                    .title(extractField(section, "名称"))
                    .description(extractField(section, "描述"))
                    .matchScore(parseScore(extractField(section, "匹配度")))
                    .estimatedMonthlyIncome(parseIncome(extractField(section, "预估月收入")))
                    .requiredSkills(extractField(section, "所需技能"))
                    .timeCommitment(extractField(section, "时间投入"))
                    .build();

            if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
                recommendations.add(dto);
            }
        }

        return recommendations;
    }

    private String extractField(String text, String field) {
        Pattern pattern = Pattern.compile(field + "[：:](.*?)(?:\n|$)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private Integer parseScore(String scoreStr) {
        if (scoreStr == null) return null;
        try {
            return Integer.parseInt(scoreStr.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseIncome(String incomeStr) {
        if (incomeStr == null) return null;
        Matcher matcher = Pattern.compile("(\\d+)").matcher(incomeStr);
        if (matcher.find()) {
            try {
                return new BigDecimal(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
