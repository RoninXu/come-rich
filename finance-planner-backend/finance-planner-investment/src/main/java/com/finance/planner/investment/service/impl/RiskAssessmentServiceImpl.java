package com.finance.planner.investment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.investment.dto.RiskAssessmentDto;
import com.finance.planner.investment.dto.RiskQuizQuestionDto;
import com.finance.planner.investment.dto.RiskQuizRequest;
import com.finance.planner.investment.entity.RiskAssessment;
import com.finance.planner.investment.repository.RiskAssessmentRepository;
import com.finance.planner.investment.service.RiskAssessmentService;
import com.finance.planner.investment.service.RiskQuizProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskQuizProvider riskQuizProvider;
    private final ObjectMapper objectMapper;

    @Override
    public List<RiskQuizQuestionDto> getQuizQuestions() {
        return riskQuizProvider.getQuizQuestions();
    }

    @Override
    @Transactional
    public RiskAssessmentDto submitQuiz(Long userId, RiskQuizRequest request) {
        // Calculate score
        int score = riskQuizProvider.calculateScore(request.getAnswers());
        String riskLevel = riskQuizProvider.determineRiskLevel(score);

        // Serialize answers to JSON
        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(request.getAnswers());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize quiz answers: {}", e.getMessage());
            throw new BusinessException(ErrorCode.RISK_QUIZ_INVALID);
        }

        // Save assessment
        RiskAssessment assessment = new RiskAssessment();
        assessment.setUserId(userId);
        assessment.setAnswers(answersJson);
        assessment.setRiskScore(score);
        assessment.setRiskLevel(riskLevel);
        assessment.setAssessmentDate(LocalDate.now());

        RiskAssessment saved = riskAssessmentRepository.save(assessment);
        log.info("Risk assessment completed for user {}: score={}, level={}", userId, score, riskLevel);

        return RiskAssessmentDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RiskAssessmentDto getLatestAssessment(Long userId) {
        return riskAssessmentRepository.findFirstByUserIdOrderByAssessmentDateDesc(userId)
                .map(RiskAssessmentDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskAssessmentDto> getAssessmentHistory(Long userId) {
        return riskAssessmentRepository.findAllByUserIdOrderByAssessmentDateDesc(userId)
                .stream()
                .map(RiskAssessmentDto::fromEntity)
                .collect(Collectors.toList());
    }
}
