package com.finance.planner.investment.service.impl;

import com.finance.planner.ai.service.LlmClient;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.investment.dto.*;
import com.finance.planner.investment.entity.InvestmentRecommendation;
import com.finance.planner.investment.entity.RiskAssessment;
import com.finance.planner.investment.repository.InvestmentRecommendationRepository;
import com.finance.planner.investment.repository.RiskAssessmentRepository;
import com.finance.planner.investment.service.InvestmentAdviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentAdviceServiceImpl implements InvestmentAdviceService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final InvestmentRecommendationRepository recommendationRepository;
    private final LlmClient llmClient;

    private static final String[] TRACK_COLORS = {
            "#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399", "#AB47BC", "#26A69A"
    };

    @Override
    @Transactional
    public InvestmentAdviceDto generateRecommendations(Long userId) {
        // Get latest risk assessment
        RiskAssessment assessment = riskAssessmentRepository.findFirstByUserIdOrderByAssessmentDateDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVESTMENT_NO_ASSESSMENT));

        // Archive old active recommendations
        List<InvestmentRecommendation> oldRecs = recommendationRepository.findByUserIdAndStatus(userId, (short) 1);
        for (InvestmentRecommendation rec : oldRecs) {
            rec.setStatus((short) 2);
        }
        if (!oldRecs.isEmpty()) {
            recommendationRepository.saveAll(oldRecs);
        }

        // Build AI prompt
        String prompt = buildPrompt(assessment);
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "你是一位专业的投资顾问。根据用户的风险评估结果，推荐3-5个投资类别方向。" +
                        "请严格用以下格式回复（每个投资方向用【投资方向N】开头）：\n" +
                        "【投资方向1】\n名称：xxx\n配比：xx%\n说明：xxx\n理由：xxx\n风险等级：低/中/高\n预期年化：x%-x%\n\n" +
                        "【投资方向2】\n名称：xxx\n配比：xx%\n说明：xxx\n理由：xxx\n风险等级：低/中/高\n预期年化：x%-x%\n\n" +
                        "...\n\n" +
                        "【免责声明】\n声明内容\n\n" +
                        "注意：不要推荐具体金融产品，只推荐投资类别方向。所有配比之和必须等于100%。"),
                Map.of("role", "user", "content", prompt)
        );

        try {
            String fullResponse = llmClient.streamChat(messages)
                    .collect(Collectors.joining())
                    .block();

            List<InvestmentRecommendation> recommendations = parseRecommendations(fullResponse, userId, assessment.getId());

            // Save recommendations
            List<InvestmentRecommendation> saved = recommendationRepository.saveAll(recommendations);
            log.info("Generated {} investment recommendations for user {}", saved.size(), userId);

            return InvestmentAdviceDto.builder()
                    .assessment(RiskAssessmentDto.fromEntity(assessment))
                    .recommendations(saved.stream().map(InvestmentRecommendationDto::fromEntity).collect(Collectors.toList()))
                    .riskWarning("投资有风险，入市需谨慎。以上建议仅供参考，不构成具体投资建议。")
                    .disclaimer("本平台不推荐具体金融产品，仅提供投资类别方向参考。请根据自身情况谨慎决策。")
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI recommendation generation failed for user {}: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.INVESTMENT_RECOMMENDATION_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentRecommendationDto> getActiveRecommendations(Long userId) {
        return recommendationRepository.findByUserIdAndStatus(userId, (short) 1)
                .stream()
                .map(InvestmentRecommendationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetAllocationDto getAssetAllocation(Long userId) {
        List<InvestmentRecommendation> recs = recommendationRepository.findByUserIdAndStatus(userId, (short) 1);

        List<AssetAllocationDto.AllocationTrack> tracks = new ArrayList<>();
        for (int i = 0; i < recs.size(); i++) {
            InvestmentRecommendation rec = recs.get(i);
            tracks.add(AssetAllocationDto.AllocationTrack.builder()
                    .name(rec.getTrackName())
                    .percentage(rec.getAllocationPercentage())
                    .color(TRACK_COLORS[i % TRACK_COLORS.length])
                    .build());
        }

        return AssetAllocationDto.builder().tracks(tracks).build();
    }

    private String buildPrompt(RiskAssessment assessment) {
        return String.format(
                "我的风险评估结果：\n" +
                "- 风险得分：%d / 32\n" +
                "- 风险等级：%s\n" +
                "- 评估日期：%s\n\n" +
                "请根据我的风险等级，推荐适合的投资类别组合（3-5个方向），并给出合理的资产配置比例。",
                assessment.getRiskScore(),
                assessment.getRiskLevel(),
                assessment.getAssessmentDate()
        );
    }

    private List<InvestmentRecommendation> parseRecommendations(String response, Long userId, Long assessmentId) {
        List<InvestmentRecommendation> recommendations = new ArrayList<>();

        if (response == null || response.isBlank()) {
            return createDefaultRecommendations(userId, assessmentId);
        }

        // Split by 【投资方向N】
        String[] sections = response.split("【投资方向\\d+】");

        for (int i = 1; i < sections.length; i++) {
            String section = sections[i].trim();
            if (section.contains("【免责声明】")) {
                section = section.substring(0, section.indexOf("【免责声明】")).trim();
            }

            InvestmentRecommendation rec = new InvestmentRecommendation();
            rec.setUserId(userId);
            rec.setRiskAssessmentId(assessmentId);
            rec.setStatus((short) 1);

            rec.setTrackName(extractField(section, "名称：", "\n"));
            String percentStr = extractField(section, "配比：", "\n").replace("%", "").trim();
            try {
                rec.setAllocationPercentage(new BigDecimal(percentStr));
            } catch (NumberFormatException e) {
                rec.setAllocationPercentage(new BigDecimal("20"));
            }
            rec.setDescription(extractField(section, "说明：", "\n"));
            rec.setRationale(extractField(section, "理由：", "\n"));
            rec.setRiskLevel(extractField(section, "风险等级：", "\n"));
            rec.setExpectedAnnualReturn(extractField(section, "预期年化：", "\n"));

            if (rec.getTrackName() != null && !rec.getTrackName().isBlank()) {
                recommendations.add(rec);
            }
        }

        if (recommendations.isEmpty()) {
            return createDefaultRecommendations(userId, assessmentId);
        }

        return recommendations;
    }

    private String extractField(String text, String prefix, String suffix) {
        int start = text.indexOf(prefix);
        if (start == -1) return "";
        start += prefix.length();
        int end = suffix != null ? text.indexOf(suffix, start) : -1;
        if (end == -1) end = text.length();
        return text.substring(start, end).trim();
    }

    private List<InvestmentRecommendation> createDefaultRecommendations(Long userId, Long assessmentId) {
        List<InvestmentRecommendation> defaults = new ArrayList<>();

        InvestmentRecommendation r1 = new InvestmentRecommendation();
        r1.setUserId(userId);
        r1.setRiskAssessmentId(assessmentId);
        r1.setTrackName("货币基金");
        r1.setAllocationPercentage(new BigDecimal("40"));
        r1.setDescription("低风险流动性资产");
        r1.setRationale("保证资金流动性和安全性");
        r1.setRiskLevel("低");
        r1.setExpectedAnnualReturn("2%-3%");
        r1.setStatus((short) 1);
        defaults.add(r1);

        InvestmentRecommendation r2 = new InvestmentRecommendation();
        r2.setUserId(userId);
        r2.setRiskAssessmentId(assessmentId);
        r2.setTrackName("债券基金");
        r2.setAllocationPercentage(new BigDecimal("35"));
        r2.setDescription("中低风险固收类资产");
        r2.setRationale("获取稳定收益，控制波动");
        r2.setRiskLevel("中低");
        r2.setExpectedAnnualReturn("4%-6%");
        r2.setStatus((short) 1);
        defaults.add(r2);

        InvestmentRecommendation r3 = new InvestmentRecommendation();
        r3.setUserId(userId);
        r3.setRiskAssessmentId(assessmentId);
        r3.setTrackName("指数基金");
        r3.setAllocationPercentage(new BigDecimal("25"));
        r3.setDescription("中等风险权益类资产");
        r3.setRationale("跟踪市场获取长期增长");
        r3.setRiskLevel("中");
        r3.setExpectedAnnualReturn("6%-10%");
        r3.setStatus((short) 1);
        defaults.add(r3);

        return defaults;
    }
}
