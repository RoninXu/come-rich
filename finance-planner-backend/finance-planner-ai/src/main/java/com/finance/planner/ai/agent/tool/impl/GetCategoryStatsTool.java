package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.CategoryStatsParams;
import com.finance.planner.analysis.service.StatisticsService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_category_stats", description = "获取分类统计数据", riskLevel = RiskLevel.LOW)
public class GetCategoryStatsTool extends AbstractTool<CategoryStatsParams> {

    private final StatisticsService statisticsService;

    public GetCategoryStatsTool(StatisticsService statisticsService) {
        super(CategoryStatsParams.class);
        this.statisticsService = statisticsService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, CategoryStatsParams params) {
        return ToolResult.success(statisticsService.getCategoryStats(userId, params.getYear(), params.getMonth(), params.getType()));
    }
}
