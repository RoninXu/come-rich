import request from '@/utils/request'
import type {
  Budget,
  BudgetTotal,
  SetBudgetRequest,
  SetBudgetTotalRequest,
  BudgetSummary,
  BudgetAiSuggestion
} from '@/types/budget'
import type { ApiResponse } from '@/types/api'

export function setBudget(data: SetBudgetRequest) {
  return request.post<ApiResponse<Budget>>('/budgets', data)
}

export function getBudgets(yearMonth: string) {
  return request.get<ApiResponse<Budget[]>>('/budgets', {
    params: { yearMonth }
  })
}

export function deleteBudget(categoryId: number, yearMonth: string) {
  return request.delete<ApiResponse<void>>('/budgets', {
    params: { categoryId, yearMonth }
  })
}

export function setBudgetTotal(data: SetBudgetTotalRequest) {
  return request.post<ApiResponse<BudgetTotal>>('/budgets/total', data)
}

export function getBudgetTotal(yearMonth: string) {
  return request.get<ApiResponse<BudgetTotal>>('/budgets/total', {
    params: { yearMonth }
  })
}

export function getBudgetSummary(yearMonth: string) {
  return request.get<ApiResponse<BudgetSummary>>('/budgets/summary', {
    params: { yearMonth }
  })
}

export function getBudgetTrend(months: number = 6) {
  return request.get<ApiResponse<BudgetSummary[]>>('/budgets/trend', {
    params: { months }
  })
}

export function copyBudgetFromPreviousMonth(targetMonth: string) {
  return request.post<ApiResponse<Budget[]>>('/budgets/copy', null, {
    params: { targetMonth }
  })
}

export function getAiBudgetSuggestions(yearMonth: string) {
  return request.post<ApiResponse<BudgetAiSuggestion>>('/budgets/ai-suggestions', null, {
    params: { yearMonth }
  })
}
