package com.finance.planner.budget.repository;

import com.finance.planner.budget.entity.BudgetTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetTotalRepository extends JpaRepository<BudgetTotal, Long> {

    /**
     * Find the total budget for a user in a specific month
     */
    Optional<BudgetTotal> findByUserIdAndYearMonth(Long userId, String yearMonth);
}
