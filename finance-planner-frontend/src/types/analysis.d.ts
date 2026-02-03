// Monthly summary types
export interface MonthlySummary {
  year: number;
  month: number;
  totalIncome: number;
  totalExpense: number;
  balance: number;
  savingsRate: number;
  transactionCount: number;
}

// Category statistics types
export interface CategoryStat {
  categoryId: number;
  categoryName: string;
  categoryIcon: string | null;
  categoryColor: string | null;
  amount: number;
  percentage: number;
  transactionCount: number;
}

// Daily statistics types
export interface DailyStat {
  date: string; // YYYY-MM-DD
  income: number;
  expense: number;
  balance: number;
}

// Health score types
export interface HealthScore {
  totalScore: number;
  grade: string;
  savingAbility: number;
  balanceRatio: number;
  consumptionStructure: number;
  assetGrowth: number;
  recordingHabit: number;
  savingDetail: ScoreDetail;
  balanceDetail: ScoreDetail;
  consumptionDetail: ScoreDetail;
  growthDetail: ScoreDetail;
  habitDetail: ScoreDetail;
  suggestions: string[];
}

export interface ScoreDetail {
  name: string;
  score: number;
  maxScore: number;
  description: string;
  status: "good" | "average" | "poor";
}

// Dashboard types
export interface Dashboard {
  currentMonth: MonthlySummary;
  healthScore: number;
  healthGrade: string;
  recentTransactions: import("./accounting").Transaction[];
  incomeChange: number;
  expenseChange: number;
}
