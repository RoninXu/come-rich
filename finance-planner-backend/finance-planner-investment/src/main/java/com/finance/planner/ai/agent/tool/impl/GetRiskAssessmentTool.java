package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetRiskAssessmentParams;
import com.finance.planner.investment.dto.RiskAssessmentDto;
import com.finance.planner.investment.service.RiskAssessmentService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_risk_assessment", description = "获取用户最新的风险评估结果", riskLevel = RiskLevel.LOW)
public class GetRiskAssessmentTool extends AbstractTool<GetRiskAssessmentParams> {

    private final RiskAssessmentService riskAssessmentService;

    public GetRiskAssessmentTool(RiskAssessmentService riskAssessmentService) {
        super(GetRiskAssessmentParams.class);
        this.riskAssessmentService = riskAssessmentService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetRiskAssessmentParams params) {
        RiskAssessmentDto assessment = riskAssessmentService.getLatestAssessment(userId);
        if (assessment == null) {
            return ToolResult.failure("尚未完成风险评估问卷，请先前往投资页面完成评估");
        }
        return ToolResult.success(assessment);
    }
}
