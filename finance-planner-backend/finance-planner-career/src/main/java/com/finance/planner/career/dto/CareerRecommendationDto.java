package com.finance.planner.career.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerRecommendationDto {

    private String careerType;
    private String title;
    private String description;
    private Integer matchScore;
    private BigDecimal estimatedMonthlyIncome;
    private String requiredSkills;
    private String timeCommitment;
}
