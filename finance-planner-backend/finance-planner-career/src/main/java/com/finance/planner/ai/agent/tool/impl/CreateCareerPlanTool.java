package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.CreateCareerPlanParams;
import com.finance.planner.ai.agent.util.DateParser;
import com.finance.planner.career.dto.CreateCareerPlanRequest;
import com.finance.planner.career.service.CareerPlanService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
@AgentTool(name = "create_career_plan", description = "创建副业计划", riskLevel = RiskLevel.MEDIUM)
public class CreateCareerPlanTool extends AbstractTool<CreateCareerPlanParams> {

    private final CareerPlanService careerPlanService;

    public CreateCareerPlanTool(CareerPlanService careerPlanService) {
        super(CreateCareerPlanParams.class);
        this.careerPlanService = careerPlanService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, CreateCareerPlanParams params) {
        LocalDate startDate = null;
        if (params.getStartDate() != null && !params.getStartDate().isBlank()) {
            try {
                startDate = DateParser.parseUserDate(params.getStartDate());
            } catch (DateTimeParseException e) {
                return ToolResult.failure("startDate 格式错误，请使用 yyyy-MM-dd 或相对日期（今天/昨天/明天/前天/后天）");
            }
        }

        CreateCareerPlanRequest request = CreateCareerPlanRequest.builder()
                .title(params.getTitle())
                .careerType(params.getCareerType())
                .description(params.getDescription())
                .matchScore(params.getMatchScore())
                .targetMonthlyIncome(params.getTargetMonthlyIncome())
                .startDate(startDate)
                .build();
        return ToolResult.success(careerPlanService.createPlan(userId, request));
    }
}
