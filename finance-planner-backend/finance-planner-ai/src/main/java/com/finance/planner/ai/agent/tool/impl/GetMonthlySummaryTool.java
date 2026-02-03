package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.MonthlySummaryParams;
import com.finance.planner.analysis.service.StatisticsService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_monthly_summary", description = "获取月度收支汇总", riskLevel = RiskLevel.LOW)
public class GetMonthlySummaryTool extends AbstractTool<MonthlySummaryParams> {

    private final StatisticsService statisticsService;

    public GetMonthlySummaryTool(StatisticsService statisticsService) {
        super(MonthlySummaryParams.class);
        this.statisticsService = statisticsService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, MonthlySummaryParams params) {
        return ToolResult.success(statisticsService.getMonthlySummary(userId, params.getYear(), params.getMonth()));
    }
}
