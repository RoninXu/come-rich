package com.finance.planner.goal.repository;

import com.finance.planner.goal.entity.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalProgressRepository extends JpaRepository<GoalProgress, Long> {

    List<GoalProgress> findByGoalIdOrderByRecordDateDesc(Long goalId);
}
