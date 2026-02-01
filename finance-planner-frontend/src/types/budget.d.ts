export interface Budget {
  id: number
  userId: number
  categoryId: number
  yearMonth: string
  amount: number
  note: string | null
  createdAt: string
  updatedAt: string
  categoryName: string | null
  categoryIcon: string | null
  categoryColor: string | null
}

export interface BudgetTotal {
  id: number
  userId: number
  yearMonth: string
  totalAmount: number
  createdAt: string
  updatedAt: string
}

export interface SetBudgetRequest {
  categoryId: number
  yearMonth: string
  amount: number
  note?: string
}

export interface SetBudgetTotalRequest {
  yearMonth: string
  totalAmount: number
}

export interface BudgetComparison {
  categoryId: number
  categoryName: string
  categoryIcon: string | null
  categoryColor: string | null
  budgetAmount: number
  actualAmount: number
  remainingAmount: number
  utilizationPercentage: number
  overBudget: boolean
}

export interface BudgetSummary {
  yearMonth: string
  totalBudget: number
  totalSpent: number
  totalRemaining: number
  overallUtilization: number
  categories: BudgetComparison[]
}

export interface BudgetAiSuggestion {
  summary: string
  suggestions: string[]
  riskWarning: string
}
