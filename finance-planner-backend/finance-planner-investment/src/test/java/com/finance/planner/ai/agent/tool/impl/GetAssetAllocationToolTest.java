package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetAssetAllocationParams;
import com.finance.planner.investment.dto.AssetAllocationDto;
import com.finance.planner.investment.service.InvestmentAdviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAssetAllocationToolTest {

    @Mock
    private InvestmentAdviceService investmentAdviceService;

    private GetAssetAllocationTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetAssetAllocationTool(investmentAdviceService);
    }

    @Test
    @DisplayName("Should return asset allocation successfully")
    void getAllocation_success() {
        GetAssetAllocationParams params = new GetAssetAllocationParams();
        AssetAllocationDto dto = AssetAllocationDto.builder()
                .tracks(List.of(
                        AssetAllocationDto.AllocationTrack.builder()
                                .name("债券")
                                .percentage(new BigDecimal("40"))
                                .color("#4CAF50")
                                .build()))
                .build();
        when(investmentAdviceService.getAssetAllocation(1L)).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should fail when no allocation available")
    void getAllocation_null() {
        GetAssetAllocationParams params = new GetAssetAllocationParams();
        when(investmentAdviceService.getAssetAllocation(1L)).thenReturn(null);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("暂无资产配置方案");
    }
}
