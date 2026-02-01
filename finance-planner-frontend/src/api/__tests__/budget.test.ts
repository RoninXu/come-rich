import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/utils/request', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

import request from '@/utils/request'
import {
  setBudget,
  getBudgets,
  deleteBudget,
  setBudgetTotal,
  getBudgetTotal,
  getBudgetSummary,
  getBudgetTrend,
  copyBudgetFromPreviousMonth,
  getAiBudgetSuggestions
} from '../budget'

describe('budget API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('setBudget calls POST /budgets with data', () => {
    const data = { categoryId: 1, yearMonth: '2026-02', amount: 1000, note: '' }
    setBudget(data)
    expect(request.post).toHaveBeenCalledWith('/budgets', data)
  })

  it('getBudgets calls GET /budgets with yearMonth param', () => {
    getBudgets('2026-02')
    expect(request.get).toHaveBeenCalledWith('/budgets', {
      params: { yearMonth: '2026-02' }
    })
  })

  it('deleteBudget calls DELETE /budgets with categoryId and yearMonth', () => {
    deleteBudget(5, '2026-02')
    expect(request.delete).toHaveBeenCalledWith('/budgets', {
      params: { categoryId: 5, yearMonth: '2026-02' }
    })
  })

  it('setBudgetTotal calls POST /budgets/total', () => {
    const data = { yearMonth: '2026-02', totalAmount: 5000 }
    setBudgetTotal(data)
    expect(request.post).toHaveBeenCalledWith('/budgets/total', data)
  })

  it('getBudgetTotal calls GET /budgets/total', () => {
    getBudgetTotal('2026-02')
    expect(request.get).toHaveBeenCalledWith('/budgets/total', {
      params: { yearMonth: '2026-02' }
    })
  })

  it('getBudgetSummary calls GET /budgets/summary', () => {
    getBudgetSummary('2026-02')
    expect(request.get).toHaveBeenCalledWith('/budgets/summary', {
      params: { yearMonth: '2026-02' }
    })
  })

  it('getBudgetTrend calls GET /budgets/trend with default months', () => {
    getBudgetTrend()
    expect(request.get).toHaveBeenCalledWith('/budgets/trend', {
      params: { months: 6 }
    })
  })

  it('getBudgetTrend calls GET /budgets/trend with custom months', () => {
    getBudgetTrend(12)
    expect(request.get).toHaveBeenCalledWith('/budgets/trend', {
      params: { months: 12 }
    })
  })

  it('copyBudgetFromPreviousMonth calls POST /budgets/copy', () => {
    copyBudgetFromPreviousMonth('2026-03')
    expect(request.post).toHaveBeenCalledWith('/budgets/copy', null, {
      params: { targetMonth: '2026-03' }
    })
  })

  it('getAiBudgetSuggestions calls POST /budgets/ai-suggestions', () => {
    getAiBudgetSuggestions('2026-02')
    expect(request.post).toHaveBeenCalledWith('/budgets/ai-suggestions', null, {
      params: { yearMonth: '2026-02' }
    })
  })
})
