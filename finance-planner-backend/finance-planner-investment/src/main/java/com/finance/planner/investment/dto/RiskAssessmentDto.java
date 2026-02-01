package com.finance.planner.investment.dto;

import com.finance.planner.investment.entity.RiskAssessment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentDto {

    private Long id;
    private Long userId;
    private Integer riskScore;
    private String riskLevel;
    private LocalDate assessmentDate;
    private LocalDateTime createdAt;

    public static RiskAssessmentDto fromEntity(RiskAssessment entity) {
        return RiskAssessmentDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .riskScore(entity.getRiskScore())
                .riskLevel(entity.getRiskLevel())
                .assessmentDate(entity.getAssessmentDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
