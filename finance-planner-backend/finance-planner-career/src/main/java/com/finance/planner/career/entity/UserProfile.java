package com.finance.planner.career.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profile", indexes = {
        @Index(name = "idx_user_profile_user_id", columnList = "user_id")
})
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(length = 100)
    private String occupation;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "available_hours_per_week")
    private Integer availableHoursPerWeek;

    @Column(name = "income_expectation", precision = 12, scale = 2)
    private BigDecimal incomeExpectation;

    @Column(columnDefinition = "TEXT")
    private String interests;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(length = 64)
    private String timezone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
