package com.finance.planner.investment.service;

import com.finance.planner.investment.dto.RiskAssessmentDto;
import com.finance.planner.investment.dto.RiskQuizQuestionDto;
import com.finance.planner.investment.dto.RiskQuizRequest;

import java.util.List;

public interface RiskAssessmentService {

    List<RiskQuizQuestionDto> getQuizQuestions();

    RiskAssessmentDto submitQuiz(Long userId, RiskQuizRequest request);

    RiskAssessmentDto getLatestAssessment(Long userId);

    List<RiskAssessmentDto> getAssessmentHistory(Long userId);
}
