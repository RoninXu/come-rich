package com.finance.planner.budget.repository;

import com.finance.planner.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Find all budgets for a user in a specific month
     */
    List<Budget> findByUserIdAndYearMonth(Long userId, String yearMonth);

    /**
     * Find a specific category budget for a user in a month
     */
    Optional<Budget> findByUserIdAndCategoryIdAndYearMonth(Long userId, Long categoryId, String yearMonth);

    /**
     * Delete a specific category budget for a user in a month
     */
    void deleteByUserIdAndCategoryIdAndYearMonth(Long userId, Long categoryId, String yearMonth);
}
