export interface Goal {
  id: number;
  title: string;
  description: string | null;
  targetAmount: number;
  currentAmount: number;
  deadline: string; // YYYY-MM-DD
  status: number; // 1=active, 2=completed, 3=abandoned
  priority: number; // 1=high, 2=medium, 3=low
  createdAt: string;
  updatedAt: string;
  // Calculated fields
  progressPercentage: number;
  remainingDays: number;
  monthlySavingsNeeded: number;
}

export interface GoalProgress {
  id: number;
  goalId: number;
  amount: number;
  note: string | null;
  recordDate: string; // YYYY-MM-DD
  createdAt: string;
}

export interface CreateGoalRequest {
  title: string;
  description?: string;
  targetAmount: number;
  deadline: string; // YYYY-MM-DD
  priority?: number;
}

export interface UpdateGoalRequest {
  title?: string;
  description?: string;
  targetAmount?: number;
  deadline?: string;
  priority?: number;
  status?: number;
}

export interface AddProgressRequest {
  amount: number;
  note?: string;
  recordDate: string; // YYYY-MM-DD
}

export interface GoalAiPlan {
  summary: string;
  steps: string[];
  tips: string[];
  riskWarning: string;
}
