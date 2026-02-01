package com.finance.planner.investment.service;

import com.finance.planner.investment.dto.RiskQuizQuestionDto;
import com.finance.planner.investment.dto.RiskQuizRequest;

import java.util.List;

public interface RiskQuizProvider {

    List<RiskQuizQuestionDto> getQuizQuestions();

    int calculateScore(List<RiskQuizRequest.QuizAnswer> answers);

    String determineRiskLevel(int score);
}
