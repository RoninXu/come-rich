package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.ListCareerPlansParams;
import com.finance.planner.career.dto.CareerPlanDto;
import com.finance.planner.career.service.CareerPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCareerPlansToolTest {

    @Mock
    private CareerPlanService careerPlanService;

    private ListCareerPlansTool tool;

    @BeforeEach
    void setUp() {
        tool = new ListCareerPlansTool(careerPlanService);
    }

    @Test
    @DisplayName("Should return career plans successfully")
    void listPlans_success() {
        ListCareerPlansParams params = new ListCareerPlansParams();
        List<CareerPlanDto> plans = List.of(CareerPlanDto.builder().id(1L).title("短视频副业").build());
        when(careerPlanService.listPlans(1L)).thenReturn(plans);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(plans);
    }

    @Test
    @DisplayName("Should return empty list when no plans")
    void listPlans_empty() {
        ListCareerPlansParams params = new ListCareerPlansParams();
        when(careerPlanService.listPlans(1L)).thenReturn(Collections.emptyList());

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(Collections.emptyList());
    }
}
