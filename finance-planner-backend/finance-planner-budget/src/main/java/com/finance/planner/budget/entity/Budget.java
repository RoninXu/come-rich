package com.finance.planner.budget.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "budget", indexes = {
        @Index(name = "idx_budget_user_id", columnList = "user_id"),
        @Index(name = "idx_budget_year_month", columnList = "year_month"),
        @Index(name = "idx_budget_user_month", columnList = "user_id, year_month"),
        @Index(name = "idx_budget_user_category_month", columnList = "user_id, category_id, year_month")
})
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
