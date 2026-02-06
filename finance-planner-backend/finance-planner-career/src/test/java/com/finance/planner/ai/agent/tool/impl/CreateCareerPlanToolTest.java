package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.CreateCareerPlanParams;
import com.finance.planner.career.dto.CareerPlanDto;
import com.finance.planner.career.dto.CreateCareerPlanRequest;
import com.finance.planner.career.service.CareerPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCareerPlanToolTest {

    @Mock
    private CareerPlanService careerPlanService;

    private CreateCareerPlanTool tool;

    @BeforeEach
    void setUp() {
        tool = new CreateCareerPlanTool(careerPlanService);
    }

    @Test
    @DisplayName("Should create career plan with all fields")
    void createPlan_success() {
        CreateCareerPlanParams params = new CreateCareerPlanParams();
        params.setTitle("短视频副业");
        params.setCareerType("自媒体");
        params.setDescription("拍摄短视频");
        params.setMatchScore(85);
        params.setTargetMonthlyIncome(new BigDecimal("5000"));
        params.setStartDate("2025-02-01");

        CareerPlanDto dto = CareerPlanDto.builder().id(1L).title("短视频副业").build();
        when(careerPlanService.createPlan(eq(1L), any(CreateCareerPlanRequest.class))).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<CreateCareerPlanRequest> captor = ArgumentCaptor.forClass(CreateCareerPlanRequest.class);
        verify(careerPlanService).createPlan(eq(1L), captor.capture());
        assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(captor.getValue().getTargetMonthlyIncome()).isEqualTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("Should create career plan without optional fields")
    void createPlan_minimalFields() {
        CreateCareerPlanParams params = new CreateCareerPlanParams();
        params.setTitle("写作副业");

        when(careerPlanService.createPlan(eq(1L), any(CreateCareerPlanRequest.class)))
                .thenReturn(CareerPlanDto.builder().id(2L).title("写作副业").build());

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<CreateCareerPlanRequest> captor = ArgumentCaptor.forClass(CreateCareerPlanRequest.class);
        verify(careerPlanService).createPlan(eq(1L), captor.capture());
        assertThat(captor.getValue().getStartDate()).isNull();
    }

    @Test
    @DisplayName("Should fail with invalid start date")
    void createPlan_invalidDate() {
        CreateCareerPlanParams params = new CreateCareerPlanParams();
        params.setTitle("测试计划");
        params.setStartDate("invalid");

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("startDate 格式错误");
    }
}
