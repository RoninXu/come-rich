package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.UpdateGoalParams;
import com.finance.planner.ai.agent.util.DateParser;
import com.finance.planner.goal.dto.UpdateGoalRequest;
import com.finance.planner.goal.service.GoalService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@AgentTool(name = "update_goal", description = "更新理财目标信息（标题、金额、截止日期、优先级、状态）", riskLevel = RiskLevel.MEDIUM)
public class UpdateGoalTool extends AbstractTool<UpdateGoalParams> {

    private final GoalService goalService;

    public UpdateGoalTool(GoalService goalService) {
        super(UpdateGoalParams.class);
        this.goalService = goalService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, UpdateGoalParams params) {
        LocalDate deadline = null;
        if (params.getDeadline() != null && !params.getDeadline().isBlank()) {
            try {
                deadline = DateParser.parseUserDate(params.getDeadline());
            } catch (DateTimeParseException e) {
                return ToolResult.failure("deadline 格式错误，请使用 yyyy-MM-dd 或相对日期（今天/昨天/明天/前天/后天）");
            }
        }

        UpdateGoalRequest request = UpdateGoalRequest.builder()
                .title(params.getTitle())
                .description(params.getDescription())
                .targetAmount(params.getTargetAmount())
                .deadline(deadline)
                .priority(params.getPriority())
                .status(params.getStatus())
                .build();
        return ToolResult.success(goalService.updateGoal(userId, params.getGoalId(), request));
    }
}
