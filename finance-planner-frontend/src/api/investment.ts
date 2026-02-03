import request from "@/utils/request";
import type {
  QuizQuestion,
  RiskQuizRequest,
  RiskAssessment,
  InvestmentRecommendation,
  InvestmentAdvice,
  AssetAllocation,
} from "@/types/investment";
import type { ApiResponse } from "@/types/api";

export function getQuizQuestions() {
  return request.get<ApiResponse<QuizQuestion[]>>("/investment/quiz");
}

export function submitQuiz(data: RiskQuizRequest) {
  return request.post<ApiResponse<RiskAssessment>>("/investment/quiz", data);
}

export function getLatestAssessment() {
  return request.get<ApiResponse<RiskAssessment>>("/investment/assessment");
}

export function getAssessmentHistory() {
  return request.get<ApiResponse<RiskAssessment[]>>(
    "/investment/assessment/history",
  );
}

export function generateRecommendations() {
  return request.post<ApiResponse<InvestmentAdvice>>(
    "/investment/recommendations",
  );
}

export function getActiveRecommendations() {
  return request.get<ApiResponse<InvestmentRecommendation[]>>(
    "/investment/recommendations",
  );
}

export function getAssetAllocation() {
  return request.get<ApiResponse<AssetAllocation>>("/investment/allocation");
}
