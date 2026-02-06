package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.*;
import com.finance.planner.ai.agent.tool.params.AddGoalProgressParams;
import com.finance.planner.ai.agent.util.DateParser;
import com.finance.planner.goal.dto.AddProgressRequest;
import com.finance.planner.goal.service.GoalService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@AgentTool(name = "add_goal_progress", description = "为理财目标添加存款进度记录", riskLevel = RiskLevel.MEDIUM)
public class AddGoalProgressTool extends AbstractTool<AddGoalProgressParams> implements RiskAwareTool<AddGoalProgressParams> {

    private final GoalService goalService;

    public AddGoalProgressTool(GoalService goalService) {
        super(AddGoalProgressParams.class);
        this.goalService = goalService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, AddGoalProgressParams params) {
        LocalDate recordDate;
        try {
            recordDate = DateParser.parseUserDate(params.getRecordDate());
        } catch (DateTimeParseException e) {
            return ToolResult.failure("recordDate 格式错误，请使用 yyyy-MM-dd 或相对日期（今天/昨天/明天/前天/后天）");
        }

        AddProgressRequest request = AddProgressRequest.builder()
                .amount(params.getAmount())
                .note(params.getNote())
                .recordDate(recordDate)
                .build();
        return ToolResult.success(goalService.addProgress(userId, params.getGoalId(), request));
    }

    @Override
    public RiskLevel evaluateRisk(AddGoalProgressParams params, BigDecimal threshold) {
        if (params.getAmount() != null && threshold != null && params.getAmount().compareTo(threshold) > 0) {
            return RiskLevel.HIGH;
        }
        return RiskLevel.MEDIUM;
    }
}
