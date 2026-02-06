package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetUserProfileParams;
import com.finance.planner.career.dto.UserProfileDto;
import com.finance.planner.career.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserProfileToolTest {

    @Mock
    private UserProfileService userProfileService;

    private GetUserProfileTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetUserProfileTool(userProfileService);
    }

    @Test
    @DisplayName("Should return user profile successfully")
    void getProfile_success() {
        GetUserProfileParams params = new GetUserProfileParams();
        UserProfileDto dto = UserProfileDto.builder()
                .id(1L)
                .occupation("软件工程师")
                .skills("Java, Python")
                .availableHoursPerWeek(10)
                .build();
        when(userProfileService.getProfile(1L)).thenReturn(dto);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    @Test
    @DisplayName("Should fail when profile not found")
    void getProfile_null() {
        GetUserProfileParams params = new GetUserProfileParams();
        when(userProfileService.getProfile(1L)).thenReturn(null);

        ToolResult result = tool.execute(1L, params);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("用户尚未填写个人资料");
    }
}
