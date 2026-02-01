package com.finance.planner.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskQuizQuestionDto {

    private String questionId;
    private String question;
    private List<QuizOption> options;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizOption {
        private String key;
        private String text;
        private int score;
    }
}
