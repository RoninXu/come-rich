package com.finance.planner.goal.service.impl;

import com.finance.planner.common.exception.BusinessException;
import com.finance.planner.goal.dto.*;
import com.finance.planner.goal.entity.FinancialGoal;
import com.finance.planner.goal.entity.GoalProgress;
import com.finance.planner.goal.repository.GoalProgressRepository;
import com.finance.planner.goal.repository.GoalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalServiceImpl Unit Tests")
class GoalServiceImplTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalProgressRepository goalProgressRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 10L;

    // ========== Helper methods ==========

    private FinancialGoal createGoal(Long id, Long userId, String title, BigDecimal target,
                                      BigDecimal current, LocalDate deadline, Short status) {
        FinancialGoal goal = new FinancialGoal();
        goal.setId(id);
        goal.setUserId(userId);
        goal.setTitle(title);
        goal.setDescription("Test goal");
        goal.setTargetAmount(target);
        goal.setCurrentAmount(current);
        goal.setDeadline(deadline);
        goal.setStatus(status);
        goal.setPriority((short) 2);
        return goal;
    }

    // ========== createGoal ==========

    @Test
    @DisplayName("createGoal - success")
    void createGoal_success() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .title("Buy a car")
                .description("Save for a new car")
                .targetAmount(new BigDecimal("100000.00"))
                .deadline(LocalDate.now().plusMonths(12))
                .priority((short) 1)
                .build();

        when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(invocation -> {
            FinancialGoal g = invocation.getArgument(0);
            g.setId(GOAL_ID);
            return g;
        });

        GoalDto result = goalService.createGoal(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Buy a car");
        assertThat(result.getTargetAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(result.getCurrentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getStatus()).isEqualTo((short) 1);
        verify(goalRepository).save(any(FinancialGoal.class));
    }

    @Test
    @DisplayName("createGoal - throws when deadline is in the past")
    void createGoal_deadlinePast_throws() {
        CreateGoalRequest request = CreateGoalRequest.builder()
                .title("Buy a car")
                .targetAmount(new BigDecimal("100000.00"))
                .deadline(LocalDate.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> goalService.createGoal(USER_ID, request))
                .isInstanceOf(BusinessException.class);
        verify(goalRepository, never()).save(any());
    }

    // ========== listGoals ==========

    @Test
    @DisplayName("listGoals - returns all goals when no status filter")
    void listGoals_noFilter() {
        FinancialGoal g1 = createGoal(1L, USER_ID, "Goal 1", new BigDecimal("10000"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(6), (short) 1);
        FinancialGoal g2 = createGoal(2L, USER_ID, "Goal 2", new BigDecimal("20000"), new BigDecimal("5000"),
                LocalDate.now().plusMonths(12), (short) 2);

        when(goalRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(USER_ID))
                .thenReturn(List.of(g1, g2));

        List<GoalDto> result = goalService.listGoals(USER_ID, null);

        assertThat(result).hasSize(2);
        verify(goalRepository).findByUserIdOrderByPriorityAscCreatedAtDesc(USER_ID);
    }

    @Test
    @DisplayName("listGoals - returns filtered goals by status")
    void listGoals_withStatus() {
        FinancialGoal g1 = createGoal(1L, USER_ID, "Goal 1", new BigDecimal("10000"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(6), (short) 1);

        when(goalRepository.findByUserIdAndStatusOrderByPriorityAscCreatedAtDesc(USER_ID, (short) 1))
                .thenReturn(List.of(g1));

        List<GoalDto> result = goalService.listGoals(USER_ID, (short) 1);

        assertThat(result).hasSize(1);
        verify(goalRepository).findByUserIdAndStatusOrderByPriorityAscCreatedAtDesc(USER_ID, (short) 1);
    }

    // ========== getGoal ==========

    @Test
    @DisplayName("getGoal - success with calculated fields")
    void getGoal_success() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Travel Fund",
                new BigDecimal("50000"), new BigDecimal("10000"),
                LocalDate.now().plusMonths(6), (short) 1);

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        GoalDto result = goalService.getGoal(USER_ID, GOAL_ID);

        assertThat(result).isNotNull();
        assertThat(result.getProgressPercentage()).isNotNull();
        assertThat(result.getRemainingDays()).isGreaterThan(0);
        assertThat(result.getMonthlySavingsNeeded()).isNotNull();
    }

    @Test
    @DisplayName("getGoal - throws when not found")
    void getGoal_notFound() {
        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getGoal(USER_ID, GOAL_ID))
                .isInstanceOf(BusinessException.class);
    }

    // ========== updateGoal ==========

    @Test
    @DisplayName("updateGoal - partial update success")
    void updateGoal_partialUpdate() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Old Title",
                new BigDecimal("50000"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(6), (short) 1);

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(i -> i.getArgument(0));

        UpdateGoalRequest request = UpdateGoalRequest.builder()
                .title("New Title")
                .build();

        GoalDto result = goalService.updateGoal(USER_ID, GOAL_ID, request);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getTargetAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        verify(goalRepository).save(any(FinancialGoal.class));
    }

    @Test
    @DisplayName("updateGoal - throws when not found")
    void updateGoal_notFound() {
        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.updateGoal(USER_ID, GOAL_ID, new UpdateGoalRequest()))
                .isInstanceOf(BusinessException.class);
        verify(goalRepository, never()).save(any());
    }

    // ========== deleteGoal ==========

    @Test
    @DisplayName("deleteGoal - success")
    void deleteGoal_success() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Goal",
                new BigDecimal("10000"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(6), (short) 1);

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        goalService.deleteGoal(USER_ID, GOAL_ID);

        verify(goalRepository).delete(goal);
    }

    @Test
    @DisplayName("deleteGoal - throws when not found")
    void deleteGoal_notFound() {
        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.deleteGoal(USER_ID, GOAL_ID))
                .isInstanceOf(BusinessException.class);
        verify(goalRepository, never()).delete(any(FinancialGoal.class));
    }

    // ========== addProgress ==========

    @Test
    @DisplayName("addProgress - success and updates current amount")
    void addProgress_success() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Goal",
                new BigDecimal("10000"), new BigDecimal("2000"),
                LocalDate.now().plusMonths(6), (short) 1);

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalProgressRepository.save(any(GoalProgress.class))).thenAnswer(invocation -> {
            GoalProgress p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });
        when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(i -> i.getArgument(0));

        AddProgressRequest request = AddProgressRequest.builder()
                .amount(new BigDecimal("1000"))
                .note("Monthly savings")
                .recordDate(LocalDate.now())
                .build();

        GoalProgressDto result = goalService.addProgress(USER_ID, GOAL_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(goal.getCurrentAmount()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(goal.getStatus()).isEqualTo((short) 1); // Still active
        verify(goalProgressRepository).save(any(GoalProgress.class));
        verify(goalRepository).save(goal);
    }

    @Test
    @DisplayName("addProgress - auto-completes goal when target reached")
    void addProgress_autoComplete() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Goal",
                new BigDecimal("10000"), new BigDecimal("9500"),
                LocalDate.now().plusMonths(6), (short) 1);

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalProgressRepository.save(any(GoalProgress.class))).thenAnswer(invocation -> {
            GoalProgress p = invocation.getArgument(0);
            p.setId(100L);
            return p;
        });
        when(goalRepository.save(any(FinancialGoal.class))).thenAnswer(i -> i.getArgument(0));

        AddProgressRequest request = AddProgressRequest.builder()
                .amount(new BigDecimal("500"))
                .recordDate(LocalDate.now())
                .build();

        goalService.addProgress(USER_ID, GOAL_ID, request);

        assertThat(goal.getCurrentAmount()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(goal.getStatus()).isEqualTo((short) 2); // Auto-completed
    }

    @Test
    @DisplayName("addProgress - throws when goal already completed")
    void addProgress_alreadyCompleted() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Goal",
                new BigDecimal("10000"), new BigDecimal("10000"),
                LocalDate.now().plusMonths(6), (short) 2);

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));

        AddProgressRequest request = AddProgressRequest.builder()
                .amount(new BigDecimal("500"))
                .recordDate(LocalDate.now())
                .build();

        assertThatThrownBy(() -> goalService.addProgress(USER_ID, GOAL_ID, request))
                .isInstanceOf(BusinessException.class);
        verify(goalProgressRepository, never()).save(any());
    }

    // ========== getProgressHistory ==========

    @Test
    @DisplayName("getProgressHistory - success")
    void getProgressHistory_success() {
        FinancialGoal goal = createGoal(GOAL_ID, USER_ID, "Goal",
                new BigDecimal("10000"), BigDecimal.ZERO,
                LocalDate.now().plusMonths(6), (short) 1);

        GoalProgress p1 = new GoalProgress();
        p1.setId(1L);
        p1.setGoalId(GOAL_ID);
        p1.setAmount(new BigDecimal("1000"));
        p1.setRecordDate(LocalDate.now());

        when(goalRepository.findByIdAndUserId(GOAL_ID, USER_ID)).thenReturn(Optional.of(goal));
        when(goalProgressRepository.findByGoalIdOrderByRecordDateDesc(GOAL_ID))
                .thenReturn(List.of(p1));

        List<GoalProgressDto> result = goalService.getProgressHistory(USER_ID, GOAL_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    // ========== getActiveGoalCount ==========

    @Test
    @DisplayName("getActiveGoalCount - returns count")
    void getActiveGoalCount_success() {
        when(goalRepository.countByUserIdAndStatus(USER_ID, (short) 1)).thenReturn(3);

        int count = goalService.getActiveGoalCount(USER_ID);

        assertThat(count).isEqualTo(3);
    }
}
