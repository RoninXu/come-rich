package com.finance.planner.career.repository;

import com.finance.planner.career.entity.CareerPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerPlanRepository extends JpaRepository<CareerPlan, Long> {

    List<CareerPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CareerPlan> findByIdAndUserId(Long id, Long userId);

    int countByUserIdAndStatusIn(Long userId, List<Short> statuses);
}
