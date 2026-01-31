package com.finance.planner.goal.service;

import com.finance.planner.goal.dto.*;

import java.util.List;

public interface GoalService {

    GoalDto createGoal(Long userId, CreateGoalRequest request);

    List<GoalDto> listGoals(Long userId, Short status);

    GoalDto getGoal(Long userId, Long goalId);

    GoalDto updateGoal(Long userId, Long goalId, UpdateGoalRequest request);

    void deleteGoal(Long userId, Long goalId);

    GoalProgressDto addProgress(Long userId, Long goalId, AddProgressRequest request);

    List<GoalProgressDto> getProgressHistory(Long userId, Long goalId);

    int getActiveGoalCount(Long userId);
}
