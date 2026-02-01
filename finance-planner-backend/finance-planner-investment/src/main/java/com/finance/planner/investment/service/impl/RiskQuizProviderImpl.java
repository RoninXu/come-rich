package com.finance.planner.investment.service.impl;

import com.finance.planner.investment.dto.RiskQuizQuestionDto;
import com.finance.planner.investment.dto.RiskQuizQuestionDto.QuizOption;
import com.finance.planner.investment.dto.RiskQuizRequest;
import com.finance.planner.investment.service.RiskQuizProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RiskQuizProviderImpl implements RiskQuizProvider {

    private static final List<RiskQuizQuestionDto> QUESTIONS = buildQuestions();

    @Override
    public List<RiskQuizQuestionDto> getQuizQuestions() {
        return QUESTIONS;
    }

    @Override
    public int calculateScore(List<RiskQuizRequest.QuizAnswer> answers) {
        // Build a lookup map: questionId -> (answerKey -> score)
        Map<String, Map<String, Integer>> scoreMap = QUESTIONS.stream()
                .collect(Collectors.toMap(
                        RiskQuizQuestionDto::getQuestionId,
                        q -> q.getOptions().stream()
                                .collect(Collectors.toMap(QuizOption::getKey, QuizOption::getScore))
                ));

        int totalScore = 0;
        for (RiskQuizRequest.QuizAnswer answer : answers) {
            Map<String, Integer> optionScores = scoreMap.get(answer.getQuestionId());
            if (optionScores != null && optionScores.containsKey(answer.getAnswer())) {
                totalScore += optionScores.get(answer.getAnswer());
            }
        }
        return totalScore;
    }

    @Override
    public String determineRiskLevel(int score) {
        if (score <= 10) {
            return "保守型";
        } else if (score <= 20) {
            return "稳健型";
        } else {
            return "进取型";
        }
    }

    private static List<RiskQuizQuestionDto> buildQuestions() {
        List<RiskQuizQuestionDto> questions = new ArrayList<>();

        // Q1: 投资经验
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q1")
                .question("您的投资经验有多久？")
                .options(List.of(
                        QuizOption.builder().key("A").text("无经验").score(0).build(),
                        QuizOption.builder().key("B").text("1年以下").score(1).build(),
                        QuizOption.builder().key("C").text("1-3年").score(2).build(),
                        QuizOption.builder().key("D").text("3-5年").score(3).build(),
                        QuizOption.builder().key("E").text("5年以上").score(4).build()
                ))
                .build());

        // Q2: 投资期限
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q2")
                .question("您计划的投资期限是多长？")
                .options(List.of(
                        QuizOption.builder().key("A").text("随时可能用到").score(0).build(),
                        QuizOption.builder().key("B").text("1年内").score(1).build(),
                        QuizOption.builder().key("C").text("1-3年").score(2).build(),
                        QuizOption.builder().key("D").text("3-5年").score(3).build(),
                        QuizOption.builder().key("E").text("5年以上").score(4).build()
                ))
                .build());

        // Q3: 风险承受
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q3")
                .question("您对投资风险的承受能力如何？")
                .options(List.of(
                        QuizOption.builder().key("A").text("不能承受任何亏损").score(0).build(),
                        QuizOption.builder().key("B").text("能承受轻微波动").score(1).build(),
                        QuizOption.builder().key("C").text("能承受一定波动").score(2).build(),
                        QuizOption.builder().key("D").text("能承受较大波动").score(3).build(),
                        QuizOption.builder().key("E").text("能承受大幅波动").score(4).build()
                ))
                .build());

        // Q4: 收入稳定性
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q4")
                .question("您的收入稳定性如何？")
                .options(List.of(
                        QuizOption.builder().key("A").text("不稳定").score(0).build(),
                        QuizOption.builder().key("B").text("一般").score(1).build(),
                        QuizOption.builder().key("C").text("较稳定").score(2).build(),
                        QuizOption.builder().key("D").text("稳定").score(3).build(),
                        QuizOption.builder().key("E").text("非常稳定").score(4).build()
                ))
                .build());

        // Q5: 投资目标
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q5")
                .question("您的投资目标是什么？")
                .options(List.of(
                        QuizOption.builder().key("A").text("保值，不亏损即可").score(0).build(),
                        QuizOption.builder().key("B").text("稳健增值，略高于通胀").score(1).build(),
                        QuizOption.builder().key("C").text("平衡增值，适度风险").score(2).build(),
                        QuizOption.builder().key("D").text("较高收益，可接受波动").score(3).build(),
                        QuizOption.builder().key("E").text("高收益，愿承担高风险").score(4).build()
                ))
                .build());

        // Q6: 亏损承受
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q6")
                .question("您能接受的最大投资亏损比例是多少？")
                .options(List.of(
                        QuizOption.builder().key("A").text("0%，不能接受任何亏损").score(0).build(),
                        QuizOption.builder().key("B").text("5%以内").score(1).build(),
                        QuizOption.builder().key("C").text("10%以内").score(2).build(),
                        QuizOption.builder().key("D").text("20%以内").score(3).build(),
                        QuizOption.builder().key("E").text("30%以上也可以接受").score(4).build()
                ))
                .build());

        // Q7: 投资知识
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q7")
                .question("您对投资理财知识的了解程度如何？")
                .options(List.of(
                        QuizOption.builder().key("A").text("了解很少").score(0).build(),
                        QuizOption.builder().key("B").text("了解基础概念").score(1).build(),
                        QuizOption.builder().key("C").text("了解较多").score(2).build(),
                        QuizOption.builder().key("D").text("熟悉各类投资工具").score(3).build(),
                        QuizOption.builder().key("E").text("精通投资理论与实践").score(4).build()
                ))
                .build());

        // Q8: 资产配置偏好
        questions.add(RiskQuizQuestionDto.builder()
                .questionId("Q8")
                .question("您倾向于如何配置资产？")
                .options(List.of(
                        QuizOption.builder().key("A").text("全部存银行").score(0).build(),
                        QuizOption.builder().key("B").text("大部分存款，少量投资").score(1).build(),
                        QuizOption.builder().key("C").text("存款与投资均衡配置").score(2).build(),
                        QuizOption.builder().key("D").text("大部分投资，少量存款").score(3).build(),
                        QuizOption.builder().key("E").text("全部用于投资").score(4).build()
                ))
                .build());

        return questions;
    }
}
