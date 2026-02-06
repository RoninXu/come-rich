package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetInvestmentAdviceParams;
import com.finance.planner.investment.dto.InvestmentAdviceDto;
import com.finance.planner.investment.service.InvestmentAdviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInvestmentAdviceToolTest {

    @Mock
    private InvestmentAdviceService investmentAdviceService;

    private GetInvestmentAdviceTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetInvestmentAdviceTool(investmentAdviceService);
    }

    @Test
    @DisplayName("Should return investment advice successfully")
    void getAdvice_success() {
        GetInvestmentAdviceParams params = new GetInvestmentAdviceParams();
        InvestmentAdviceDto dto = InvestmentAdviceDto.builder()
                .recommendations(Collections.emptyList())
                .riskWarning("投资有风险")
                .disclaimer("仅供参考")
                .build();
        when(investmentAdviceService.generateRecommendations(1L)).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should fail when no assessment exists")
    void getAdvice_noAssessment() {
        GetInvestmentAdviceParams params = new GetInvestmentAdviceParams();
        when(investmentAdviceService.generateRecommendations(1L))
                .thenThrow(new RuntimeException("请先完成风险评估"));

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("无法生成投资建议");
    }
}
