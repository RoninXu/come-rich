package com.finance.planner.career.service;

import com.finance.planner.career.dto.CareerRecommendationDto;

import java.util.List;

public interface CareerRecommendationService {

    List<CareerRecommendationDto> getRecommendations(Long userId);

    String generateStartupPlan(Long userId, Long planId);
}
