package com.finance.planner.career.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "career_plan", indexes = {
        @Index(name = "idx_career_plan_user_id", columnList = "user_id"),
        @Index(name = "idx_career_plan_status", columnList = "status"),
        @Index(name = "idx_career_plan_user_status", columnList = "user_id, status")
})
public class CareerPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "career_type", length = 100)
    private String careerType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "match_score")
    private Integer matchScore;

    /** 1=exploring, 2=active, 3=paused, 4=completed */
    @Column(nullable = false)
    private Short status = 1;

    @Column(name = "target_monthly_income", precision = 12, scale = 2)
    private BigDecimal targetMonthlyIncome;

    @Column(name = "actual_monthly_income", precision = 12, scale = 2)
    private BigDecimal actualMonthlyIncome = BigDecimal.ZERO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "startup_plan", columnDefinition = "TEXT")
    private String startupPlan;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
