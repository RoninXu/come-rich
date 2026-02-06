package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.CreateGoalParams;
import com.finance.planner.ai.agent.util.DateParser;
import com.finance.planner.goal.dto.CreateGoalRequest;
import com.finance.planner.goal.service.GoalService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@AgentTool(name = "create_goal", description = "创建理财目标，需要标题、目标金额和截止日期", riskLevel = RiskLevel.MEDIUM)
public class CreateGoalTool extends AbstractTool<CreateGoalParams> {

    private final GoalService goalService;

    public CreateGoalTool(GoalService goalService) {
        super(CreateGoalParams.class);
        this.goalService = goalService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, CreateGoalParams params) {
        LocalDate deadline;
        try {
            deadline = DateParser.parseUserDate(params.getDeadline());
        } catch (DateTimeParseException e) {
            return ToolResult.failure("deadline 格式错误，请使用 yyyy-MM-dd 或相对日期（今天/昨天/明天/前天/后天）");
        }

        CreateGoalRequest request = CreateGoalRequest.builder()
                .title(params.getTitle())
                .description(params.getDescription())
                .targetAmount(params.getTargetAmount())
                .deadline(deadline)
                .priority(params.getPriority() != null ? params.getPriority() : (short) 2)
                .build();
        return ToolResult.success(goalService.createGoal(userId, request));
    }
}
