package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.HealthScoreParams;
import com.finance.planner.analysis.service.HealthScoreService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_health_score", description = "获取财务健康评分", riskLevel = RiskLevel.LOW)
public class GetHealthScoreTool extends AbstractTool<HealthScoreParams> {

    private final HealthScoreService healthScoreService;

    public GetHealthScoreTool(HealthScoreService healthScoreService) {
        super(HealthScoreParams.class);
        this.healthScoreService = healthScoreService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, HealthScoreParams params) {
        return ToolResult.success(healthScoreService.calculateHealthScore(userId));
    }
}
