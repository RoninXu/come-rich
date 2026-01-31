package com.finance.planner.goal.service;

import com.finance.planner.goal.dto.GoalAiPlanDto;

public interface GoalAiService {

    GoalAiPlanDto generateAiPlan(Long userId, Long goalId);
}
