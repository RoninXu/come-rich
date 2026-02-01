export interface QuizOption {
  key: string
  text: string
  score: number
}

export interface QuizQuestion {
  questionId: string
  question: string
  options: QuizOption[]
}

export interface QuizAnswer {
  questionId: string
  answer: string
}

export interface RiskQuizRequest {
  answers: QuizAnswer[]
}

export interface RiskAssessment {
  id: number
  userId: number
  riskScore: number
  riskLevel: string
  assessmentDate: string
  createdAt: string
}

export interface InvestmentRecommendation {
  id: number
  userId: number
  riskAssessmentId: number
  trackName: string
  allocationPercentage: number
  description: string | null
  rationale: string | null
  riskLevel: string | null
  expectedAnnualReturn: string | null
  status: number
  createdAt: string
}

export interface AllocationTrack {
  name: string
  percentage: number
  color: string
}

export interface AssetAllocation {
  tracks: AllocationTrack[]
}

export interface InvestmentAdvice {
  assessment: RiskAssessment
  recommendations: InvestmentRecommendation[]
  riskWarning: string
  disclaimer: string
}
