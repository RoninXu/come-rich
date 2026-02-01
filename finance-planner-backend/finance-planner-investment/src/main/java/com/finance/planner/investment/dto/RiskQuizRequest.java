package com.finance.planner.investment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskQuizRequest {

    @NotNull(message = "答案不能为空")
    @Size(min = 8, max = 8, message = "必须回答全部8道题目")
    private List<QuizAnswer> answers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizAnswer {
        private String questionId;
        private String answer;
    }
}
