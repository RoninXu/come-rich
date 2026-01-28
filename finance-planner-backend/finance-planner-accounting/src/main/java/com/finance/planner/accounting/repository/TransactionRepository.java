package com.finance.planner.accounting.repository;

import com.finance.planner.accounting.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all non-deleted transactions by user with pagination
     */
    Page<Transaction> findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find non-deleted transaction by id and user
     */
    Optional<Transaction> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    /**
     * Find transactions by user and date range
     */
    List<Transaction> findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Find transactions by user, type, and date range
     */
    List<Transaction> findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            Long userId, Short type, LocalDate startDate, LocalDate endDate);

    /**
     * Find transactions by user and type with pagination
     */
    Page<Transaction> findByUserIdAndTypeAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            Long userId, Short type, Pageable pageable);

    /**
     * Find transactions by user within date range with pagination
     */
    Page<Transaction> findByUserIdAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Find transactions by user, type, and date range with pagination
     */
    Page<Transaction> findByUserIdAndTypeAndTransactionDateBetweenAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            Long userId, Short type, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get recent transactions for a user (for dashboard)
     */
    List<Transaction> findTop10ByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(Long userId);

    /**
     * Count transactions by user and category (for checking if category is in use)
     */
    long countByUserIdAndCategoryIdAndIsDeletedFalse(Long userId, Long categoryId);

    /**
     * Count all transactions by category (for checking if category is in use globally)
     */
    long countByCategoryIdAndIsDeletedFalse(Long categoryId);
}
