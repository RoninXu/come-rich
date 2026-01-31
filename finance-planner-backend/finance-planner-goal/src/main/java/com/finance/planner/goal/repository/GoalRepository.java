package com.finance.planner.goal.repository;

import com.finance.planner.goal.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<FinancialGoal, Long> {

    List<FinancialGoal> findByUserIdOrderByPriorityAscCreatedAtDesc(Long userId);

    List<FinancialGoal> findByUserIdAndStatusOrderByPriorityAscCreatedAtDesc(Long userId, Short status);

    Optional<FinancialGoal> findByIdAndUserId(Long id, Long userId);

    int countByUserIdAndStatus(Long userId, Short status);
}
