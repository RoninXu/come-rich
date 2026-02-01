package com.finance.planner.investment.service;

import com.finance.planner.investment.dto.AssetAllocationDto;
import com.finance.planner.investment.dto.InvestmentAdviceDto;
import com.finance.planner.investment.dto.InvestmentRecommendationDto;

import java.util.List;

public interface InvestmentAdviceService {

    InvestmentAdviceDto generateRecommendations(Long userId);

    List<InvestmentRecommendationDto> getActiveRecommendations(Long userId);

    AssetAllocationDto getAssetAllocation(Long userId);
}
