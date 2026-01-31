package com.finance.planner.goal.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "financial_goal", indexes = {
        @Index(name = "idx_goal_user_id", columnList = "user_id"),
        @Index(name = "idx_goal_status", columnList = "status"),
        @Index(name = "idx_goal_user_status", columnList = "user_id, status")
})
public class FinancialGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "target_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate deadline;

    /** 1=active, 2=completed, 3=abandoned */
    @Column(nullable = false)
    private Short status = 1;

    /** 1=high, 2=medium, 3=low */
    @Column(nullable = false)
    private Short priority = 2;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status != null && status == 1;
    }

    public boolean isCompleted() {
        return status != null && status == 2;
    }
}
