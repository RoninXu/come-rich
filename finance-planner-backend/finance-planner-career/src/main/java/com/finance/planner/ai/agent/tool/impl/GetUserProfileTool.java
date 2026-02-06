package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetUserProfileParams;
import com.finance.planner.career.dto.UserProfileDto;
import com.finance.planner.career.service.UserProfileService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_user_profile", description = "获取用户个人资料（职业、技能、兴趣等）", riskLevel = RiskLevel.LOW)
public class GetUserProfileTool extends AbstractTool<GetUserProfileParams> {

    private final UserProfileService userProfileService;

    public GetUserProfileTool(UserProfileService userProfileService) {
        super(GetUserProfileParams.class);
        this.userProfileService = userProfileService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetUserProfileParams params) {
        UserProfileDto profile = userProfileService.getProfile(userId);
        if (profile == null) {
            return ToolResult.failure("用户尚未填写个人资料");
        }
        return ToolResult.success(profile);
    }
}
