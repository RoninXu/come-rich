import request from '@/utils/request'
import type {
  MonthlySummary,
  CategoryStat,
  DailyStat,
  HealthScore,
  Dashboard
} from '@/types/analysis'
import type { ApiResponse } from '@/types/api'

/**
 * Get monthly summary (income, expense, balance)
 */
export function getMonthlySummary(year?: number, month?: number) {
  return request.get<ApiResponse<MonthlySummary>>('/analysis/monthly', {
    params: { year, month }
  })
}

/**
 * Get category statistics
 * @param type 1=income, 2=expense
 */
export function getCategoryStats(year?: number, month?: number, type?: number) {
  return request.get<ApiResponse<CategoryStat[]>>('/analysis/category', {
    params: { year, month, type }
  })
}

/**
 * Get daily statistics for a month
 */
export function getDailyStats(year?: number, month?: number) {
  return request.get<ApiResponse<DailyStat[]>>('/analysis/daily', {
    params: { year, month }
  })
}

/**
 * Get financial health score
 */
export function getHealthScore() {
  return request.get<ApiResponse<HealthScore>>('/analysis/health-score')
}

/**
 * Get dashboard data
 */
export function getDashboard() {
  return request.get<ApiResponse<Dashboard>>('/analysis/dashboard')
}
