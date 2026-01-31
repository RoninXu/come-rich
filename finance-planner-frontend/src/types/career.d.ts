export interface UserProfile {
  id: number
  occupation: string | null
  skills: string | null
  availableHoursPerWeek: number | null
  incomeExpectation: number | null
  interests: string | null
  experienceLevel: string | null
}

export interface SaveProfileRequest {
  occupation?: string
  skills?: string
  availableHoursPerWeek?: number
  incomeExpectation?: number
  interests?: string
  experienceLevel?: string
}

export interface CareerPlan {
  id: number
  careerType: string | null
  title: string
  description: string | null
  matchScore: number | null
  status: number // 1=exploring, 2=active, 3=paused, 4=completed
  targetMonthlyIncome: number | null
  actualMonthlyIncome: number | null
  startDate: string | null
  endDate: string | null
  startupPlan: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateCareerPlanRequest {
  careerType?: string
  title: string
  description?: string
  matchScore?: number
  targetMonthlyIncome?: number
  startDate?: string
}

export interface CareerRecommendation {
  careerType: string | null
  title: string | null
  description: string | null
  matchScore: number | null
  estimatedMonthlyIncome: number | null
  requiredSkills: string | null
  timeCommitment: string | null
}

export interface CareerIncome {
  id: number
  careerPlanId: number
  amount: number
  description: string | null
  incomeDate: string
  createdAt: string
}

export interface AddCareerIncomeRequest {
  amount: number
  description?: string
  incomeDate: string
}
