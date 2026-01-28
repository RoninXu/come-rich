package com.finance.planner.analysis.service;

import com.finance.planner.analysis.dto.HealthScoreDto;

public interface HealthScoreService {

    /**
     * Calculate financial health score based on user's recent transactions
     * Uses data from the last 3 months
     */
    HealthScoreDto calculateHealthScore(Long userId);
}
