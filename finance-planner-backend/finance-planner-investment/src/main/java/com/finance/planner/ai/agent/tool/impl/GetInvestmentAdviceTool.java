package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetInvestmentAdviceParams;
import com.finance.planner.investment.service.InvestmentAdviceService;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_investment_advice", description = "获取投资建议和推荐方案（需先完成风险评估）", riskLevel = RiskLevel.LOW)
public class GetInvestmentAdviceTool extends AbstractTool<GetInvestmentAdviceParams> {

    private final InvestmentAdviceService investmentAdviceService;

    public GetInvestmentAdviceTool(InvestmentAdviceService investmentAdviceService) {
        super(GetInvestmentAdviceParams.class);
        this.investmentAdviceService = investmentAdviceService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetInvestmentAdviceParams params) {
        try {
            return ToolResult.success(investmentAdviceService.generateRecommendations(userId));
        } catch (Exception e) {
            return ToolResult.failure("无法生成投资建议：" + e.getMessage());
        }
    }
}
