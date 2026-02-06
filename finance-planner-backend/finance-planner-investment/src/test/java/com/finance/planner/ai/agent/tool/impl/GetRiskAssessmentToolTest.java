package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetRiskAssessmentParams;
import com.finance.planner.investment.dto.RiskAssessmentDto;
import com.finance.planner.investment.service.RiskAssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRiskAssessmentToolTest {

    @Mock
    private RiskAssessmentService riskAssessmentService;

    private GetRiskAssessmentTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetRiskAssessmentTool(riskAssessmentService);
    }

    @Test
    @DisplayName("Should return assessment when exists")
    void getAssessment_success() {
        GetRiskAssessmentParams params = new GetRiskAssessmentParams();
        RiskAssessmentDto dto = RiskAssessmentDto.builder()
                .id(1L)
                .riskScore(65)
                .riskLevel("中等风险")
                .build();
        when(riskAssessmentService.getLatestAssessment(1L)).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should fail when no assessment exists")
    void getAssessment_null() {
        GetRiskAssessmentParams params = new GetRiskAssessmentParams();
        when(riskAssessmentService.getLatestAssessment(1L)).thenReturn(null);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("尚未完成风险评估问卷");
    }

    @Test
    @DisplayName("Should have LOW risk level")
    void riskLevel() {
        assertThat(tool.getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }
}
