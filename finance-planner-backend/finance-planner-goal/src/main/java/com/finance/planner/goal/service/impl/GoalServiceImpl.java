package com.finance.planner.goal.service.impl;

import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.goal.dto.*;
import com.finance.planner.goal.entity.FinancialGoal;
import com.finance.planner.goal.entity.GoalProgress;
import com.finance.planner.goal.repository.GoalProgressRepository;
import com.finance.planner.goal.repository.GoalRepository;
import com.finance.planner.goal.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;

    @Override
    @Transactional
    public GoalDto createGoal(Long userId, CreateGoalRequest request) {
        if (request.getDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.GOAL_DEADLINE_PAST);
        }

        FinancialGoal goal = new FinancialGoal();
        goal.setUserId(userId);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(request.getDeadline());
        goal.setStatus((short) 1);
        goal.setPriority(request.getPriority() != null ? request.getPriority() : (short) 2);

        FinancialGoal saved = goalRepository.save(goal);
        log.info("Created goal {} for user {}", saved.getId(), userId);
        return GoalDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalDto> listGoals(Long userId, Short status) {
        List<FinancialGoal> goals;
        if (status != null) {
            goals = goalRepository.findByUserIdAndStatusOrderByPriorityAscCreatedAtDesc(userId, status);
        } else {
            goals = goalRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(userId);
        }
        return goals.stream().map(GoalDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GoalDto getGoal(Long userId, Long goalId) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));
        return GoalDto.fromEntity(goal);
    }

    @Override
    @Transactional
    public GoalDto updateGoal(Long userId, Long goalId, UpdateGoalRequest request) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));

        if (request.getTitle() != null) {
            goal.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            goal.setDescription(request.getDescription());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getDeadline() != null) {
            goal.setDeadline(request.getDeadline());
        }
        if (request.getPriority() != null) {
            goal.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        FinancialGoal saved = goalRepository.save(goal);
        log.info("Updated goal {} for user {}", saved.getId(), userId);
        return GoalDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));
        goalRepository.delete(goal);
        log.info("Deleted goal {} for user {}", goalId, userId);
    }

    @Override
    @Transactional
    public GoalProgressDto addProgress(Long userId, Long goalId, AddProgressRequest request) {
        FinancialGoal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));

        if (goal.isCompleted()) {
            throw new BusinessException(ErrorCode.GOAL_ALREADY_COMPLETED);
        }

        GoalProgress progress = new GoalProgress();
        progress.setGoalId(goalId);
        progress.setAmount(request.getAmount());
        progress.setNote(request.getNote());
        progress.setRecordDate(request.getRecordDate());

        GoalProgress saved = goalProgressRepository.save(progress);

        // Update current amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
        // Auto-complete if target reached
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus((short) 2);
            log.info("Goal {} auto-completed for user {}", goalId, userId);
        }
        goalRepository.save(goal);

        log.info("Added progress {} to goal {} for user {}", saved.getId(), goalId, userId);
        return GoalProgressDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalProgressDto> getProgressHistory(Long userId, Long goalId) {
        // Verify goal belongs to user
        goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));

        return goalProgressRepository.findByGoalIdOrderByRecordDateDesc(goalId)
                .stream()
                .map(GoalProgressDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int getActiveGoalCount(Long userId) {
        return goalRepository.countByUserIdAndStatus(userId, (short) 1);
    }
}
