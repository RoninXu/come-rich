package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetCareerRecommendationsParams;
import com.finance.planner.career.service.CareerRecommendationService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_career_recommendations", description = "获取AI副业推荐（需先填写个人资料）", riskLevel = RiskLevel.LOW)
public class GetCareerRecommendationsTool extends AbstractTool<GetCareerRecommendationsParams> {

    private final CareerRecommendationService careerRecommendationService;

    public GetCareerRecommendationsTool(CareerRecommendationService careerRecommendationService) {
        super(GetCareerRecommendationsParams.class);
        this.careerRecommendationService = careerRecommendationService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetCareerRecommendationsParams params) {
        try {
            return ToolResult.success(careerRecommendationService.getRecommendations(userId));
        } catch (Exception e) {
            return ToolResult.failure("无法获取副业推荐，请先前往副业页面填写个人资料");
        }
    }
}
