package com.finance.planner.investment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "investment_recommendation", indexes = {
        @Index(name = "idx_investment_rec_user_id", columnList = "user_id"),
        @Index(name = "idx_investment_rec_user_status", columnList = "user_id, status")
})
public class InvestmentRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "risk_assessment_id")
    private Long riskAssessmentId;

    @Column(name = "track_name", nullable = false, length = 100)
    private String trackName;

    @Column(name = "allocation_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal allocationPercentage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "risk_level", length = 30)
    private String riskLevel;

    @Column(name = "expected_annual_return", length = 50)
    private String expectedAnnualReturn;

    /** 1=active, 2=archived */
    @Column(nullable = false)
    private Short status = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
