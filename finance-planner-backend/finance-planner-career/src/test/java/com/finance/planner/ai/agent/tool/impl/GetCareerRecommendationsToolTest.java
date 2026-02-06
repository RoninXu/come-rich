package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetCareerRecommendationsParams;
import com.finance.planner.career.dto.CareerRecommendationDto;
import com.finance.planner.career.service.CareerRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCareerRecommendationsToolTest {

    @Mock
    private CareerRecommendationService careerRecommendationService;

    private GetCareerRecommendationsTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetCareerRecommendationsTool(careerRecommendationService);
    }

    @Test
    @DisplayName("Should return recommendations successfully")
    void getRecommendations_success() {
        GetCareerRecommendationsParams params = new GetCareerRecommendationsParams();
        List<CareerRecommendationDto> recommendations = List.of(
                CareerRecommendationDto.builder().careerType("自媒体").title("短视频创作").matchScore(85).build());
        when(careerRecommendationService.getRecommendations(1L)).thenReturn(recommendations);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(recommendations);
    }

    @Test
    @DisplayName("Should fail when profile not filled")
    void getRecommendations_emptyProfile() {
        GetCareerRecommendationsParams params = new GetCareerRecommendationsParams();
        when(careerRecommendationService.getRecommendations(1L))
                .thenThrow(new RuntimeException("用户资料不完整"));

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("无法获取副业推荐");
    }
}
