package com.finance.planner.career.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "career_income", indexes = {
        @Index(name = "idx_career_income_plan_id", columnList = "career_plan_id"),
        @Index(name = "idx_career_income_date", columnList = "income_date")
})
public class CareerIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "career_plan_id", nullable = false)
    private Long careerPlanId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
