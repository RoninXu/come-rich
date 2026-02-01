package com.finance.planner.investment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.investment.dto.RiskAssessmentDto;
import com.finance.planner.investment.dto.RiskQuizQuestionDto;
import com.finance.planner.investment.dto.RiskQuizRequest;
import com.finance.planner.investment.entity.RiskAssessment;
import com.finance.planner.investment.repository.RiskAssessmentRepository;
import com.finance.planner.investment.service.RiskQuizProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RiskAssessmentServiceImpl Unit Tests")
class RiskAssessmentServiceImplTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private RiskQuizProvider riskQuizProvider;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RiskAssessmentServiceImpl riskAssessmentService;

    private static final Long USER_ID = 1L;

    // ========== Helper methods ==========

    private RiskAssessment createAssessment(Long id, Long userId, int score, String level) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setId(id);
        assessment.setUserId(userId);
        assessment.setAnswers("[{\"questionId\":\"Q1\",\"answer\":\"A\"}]");
        assessment.setRiskScore(score);
        assessment.setRiskLevel(level);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setCreatedAt(LocalDateTime.now());
        return assessment;
    }

    private List<RiskQuizRequest.QuizAnswer> createAnswers() {
        return List.of(
                RiskQuizRequest.QuizAnswer.builder().questionId("Q1").answer("A").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q2").answer("B").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q3").answer("A").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q4").answer("C").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q5").answer("A").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q6").answer("B").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q7").answer("A").build(),
                RiskQuizRequest.QuizAnswer.builder().questionId("Q8").answer("A").build()
        );
    }

    private RiskQuizRequest createQuizRequest() {
        return RiskQuizRequest.builder()
                .answers(createAnswers())
                .build();
    }

    // ========== getQuizQuestions ==========

    @Test
    @DisplayName("getQuizQuestions - delegates to provider and returns questions")
    void getQuizQuestions_returnsQuestions() {
        List<RiskQuizQuestionDto> questions = List.of(
                RiskQuizQuestionDto.builder()
                        .questionId("Q1")
                        .question("您的年龄段是?")
                        .options(List.of(
                                RiskQuizQuestionDto.QuizOption.builder().key("A").text("25岁以下").score(4).build(),
                                RiskQuizQuestionDto.QuizOption.builder().key("B").text("25-35岁").score(3).build()
                        ))
                        .build(),
                RiskQuizQuestionDto.builder()
                        .questionId("Q2")
                        .question("您的投资经验?")
                        .options(List.of(
                                RiskQuizQuestionDto.QuizOption.builder().key("A").text("无经验").score(1).build(),
                                RiskQuizQuestionDto.QuizOption.builder().key("B").text("1-3年").score(2).build()
                        ))
                        .build()
        );

        when(riskQuizProvider.getQuizQuestions()).thenReturn(questions);

        List<RiskQuizQuestionDto> result = riskAssessmentService.getQuizQuestions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestionId()).isEqualTo("Q1");
        assertThat(result.get(1).getQuestionId()).isEqualTo("Q2");
        verify(riskQuizProvider).getQuizQuestions();
    }

    // ========== submitQuiz ==========

    @Test
    @DisplayName("submitQuiz - score 0-10 maps to 保守型 (conservative)")
    void submitQuiz_conservative() throws Exception {
        RiskQuizRequest request = createQuizRequest();
        int score = 8;

        when(riskQuizProvider.calculateScore(request.getAnswers())).thenReturn(score);
        when(riskQuizProvider.determineRiskLevel(score)).thenReturn("保守型");
        when(objectMapper.writeValueAsString(request.getAnswers())).thenReturn("[{}]");
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment a = invocation.getArgument(0);
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        RiskAssessmentDto result = riskAssessmentService.submitQuiz(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getRiskScore()).isEqualTo(8);
        assertThat(result.getRiskLevel()).isEqualTo("保守型");
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        verify(riskQuizProvider).calculateScore(request.getAnswers());
        verify(riskQuizProvider).determineRiskLevel(score);
    }

    @Test
    @DisplayName("submitQuiz - score 11-20 maps to 稳健型 (moderate)")
    void submitQuiz_moderate() throws Exception {
        RiskQuizRequest request = createQuizRequest();
        int score = 16;

        when(riskQuizProvider.calculateScore(request.getAnswers())).thenReturn(score);
        when(riskQuizProvider.determineRiskLevel(score)).thenReturn("稳健型");
        when(objectMapper.writeValueAsString(request.getAnswers())).thenReturn("[{}]");
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment a = invocation.getArgument(0);
            a.setId(2L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        RiskAssessmentDto result = riskAssessmentService.submitQuiz(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getRiskScore()).isEqualTo(16);
        assertThat(result.getRiskLevel()).isEqualTo("稳健型");
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        verify(riskQuizProvider).calculateScore(request.getAnswers());
        verify(riskQuizProvider).determineRiskLevel(score);
    }

    @Test
    @DisplayName("submitQuiz - score 21-32 maps to 进取型 (aggressive)")
    void submitQuiz_aggressive() throws Exception {
        RiskQuizRequest request = createQuizRequest();
        int score = 28;

        when(riskQuizProvider.calculateScore(request.getAnswers())).thenReturn(score);
        when(riskQuizProvider.determineRiskLevel(score)).thenReturn("进取型");
        when(objectMapper.writeValueAsString(request.getAnswers())).thenReturn("[{}]");
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment a = invocation.getArgument(0);
            a.setId(3L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        RiskAssessmentDto result = riskAssessmentService.submitQuiz(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getRiskScore()).isEqualTo(28);
        assertThat(result.getRiskLevel()).isEqualTo("进取型");
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        verify(riskQuizProvider).calculateScore(request.getAnswers());
        verify(riskQuizProvider).determineRiskLevel(score);
    }

    @Test
    @DisplayName("submitQuiz - verifies assessment is saved to repository")
    void submitQuiz_savesAssessment() throws Exception {
        RiskQuizRequest request = createQuizRequest();
        int score = 15;

        when(riskQuizProvider.calculateScore(request.getAnswers())).thenReturn(score);
        when(riskQuizProvider.determineRiskLevel(score)).thenReturn("稳健型");
        when(objectMapper.writeValueAsString(request.getAnswers())).thenReturn("[{\"questionId\":\"Q1\",\"answer\":\"A\"}]");
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment a = invocation.getArgument(0);
            a.setId(4L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        riskAssessmentService.submitQuiz(USER_ID, request);

        verify(riskAssessmentRepository).save(argThat(assessment -> {
            assertThat(assessment.getUserId()).isEqualTo(USER_ID);
            assertThat(assessment.getRiskScore()).isEqualTo(15);
            assertThat(assessment.getRiskLevel()).isEqualTo("稳健型");
            assertThat(assessment.getAnswers()).isNotNull();
            assertThat(assessment.getAssessmentDate()).isEqualTo(LocalDate.now());
            return true;
        }));
    }

    // ========== getLatestAssessment ==========

    @Test
    @DisplayName("getLatestAssessment - returns latest assessment for user")
    void getLatestAssessment_success() {
        RiskAssessment assessment = createAssessment(1L, USER_ID, 20, "稳健型");

        when(riskAssessmentRepository.findFirstByUserIdOrderByAssessmentDateDesc(USER_ID))
                .thenReturn(Optional.of(assessment));

        RiskAssessmentDto result = riskAssessmentService.getLatestAssessment(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getRiskScore()).isEqualTo(20);
        assertThat(result.getRiskLevel()).isEqualTo("稳健型");
        assertThat(result.getAssessmentDate()).isEqualTo(LocalDate.now());
        verify(riskAssessmentRepository).findFirstByUserIdOrderByAssessmentDateDesc(USER_ID);
    }

    @Test
    @DisplayName("getLatestAssessment - throws RISK_ASSESSMENT_NOT_FOUND when none exists")
    void getLatestAssessment_notFound() {
        when(riskAssessmentRepository.findFirstByUserIdOrderByAssessmentDateDesc(USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskAssessmentService.getLatestAssessment(USER_ID))
                .isInstanceOf(BusinessException.class);
        verify(riskAssessmentRepository).findFirstByUserIdOrderByAssessmentDateDesc(USER_ID);
    }
}
