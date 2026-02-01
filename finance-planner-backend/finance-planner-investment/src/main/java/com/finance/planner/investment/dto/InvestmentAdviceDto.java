package com.finance.planner.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentAdviceDto {

    private RiskAssessmentDto assessment;
    private List<InvestmentRecommendationDto> recommendations;
    private String riskWarning;
    private String disclaimer;
}
