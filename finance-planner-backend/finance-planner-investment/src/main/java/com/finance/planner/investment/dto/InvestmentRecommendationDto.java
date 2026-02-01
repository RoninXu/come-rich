package com.finance.planner.investment.dto;

import com.finance.planner.investment.entity.InvestmentRecommendation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentRecommendationDto {

    private Long id;
    private Long userId;
    private Long riskAssessmentId;
    private String trackName;
    private BigDecimal allocationPercentage;
    private String description;
    private String rationale;
    private String riskLevel;
    private String expectedAnnualReturn;
    private Short status;
    private LocalDateTime createdAt;

    public static InvestmentRecommendationDto fromEntity(InvestmentRecommendation entity) {
        return InvestmentRecommendationDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .riskAssessmentId(entity.getRiskAssessmentId())
                .trackName(entity.getTrackName())
                .allocationPercentage(entity.getAllocationPercentage())
                .description(entity.getDescription())
                .rationale(entity.getRationale())
                .riskLevel(entity.getRiskLevel())
                .expectedAnnualReturn(entity.getExpectedAnnualReturn())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
