package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetAssetAllocationParams;
import com.finance.planner.investment.dto.AssetAllocationDto;
import com.finance.planner.investment.service.InvestmentAdviceService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_asset_allocation", description = "获取推荐的资产配置方案", riskLevel = RiskLevel.LOW)
public class GetAssetAllocationTool extends AbstractTool<GetAssetAllocationParams> {

    private final InvestmentAdviceService investmentAdviceService;

    public GetAssetAllocationTool(InvestmentAdviceService investmentAdviceService) {
        super(GetAssetAllocationParams.class);
        this.investmentAdviceService = investmentAdviceService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetAssetAllocationParams params) {
        AssetAllocationDto allocation = investmentAdviceService.getAssetAllocation(userId);
        if (allocation == null) {
            return ToolResult.failure("暂无资产配置方案，请先完成风险评估");
        }
        return ToolResult.success(allocation);
    }
}
