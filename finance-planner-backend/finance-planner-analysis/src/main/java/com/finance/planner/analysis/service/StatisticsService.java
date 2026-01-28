package com.finance.planner.analysis.service;

import com.finance.planner.analysis.dto.CategoryStatDto;
import com.finance.planner.analysis.dto.DailyStatDto;
import com.finance.planner.analysis.dto.MonthlySummaryDto;

import java.util.List;

public interface StatisticsService {

    /**
     * Get monthly summary for a specific month
     */
    MonthlySummaryDto getMonthlySummary(Long userId, int year, int month);

    /**
     * Get category statistics for a month
     * @param type 1=income, 2=expense (null for all)
     */
    List<CategoryStatDto> getCategoryStats(Long userId, int year, int month, Short type);

    /**
     * Get daily statistics for a month
     */
    List<DailyStatDto> getDailyStats(Long userId, int year, int month);
}
