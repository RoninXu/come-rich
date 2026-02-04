import request from "@/utils/request";
import type {
  CareerPlan,
  CreateCareerPlanRequest,
  CareerRecommendation,
  CareerIncome,
  AddCareerIncomeRequest,
} from "@/types/career";
import type { ApiResponse } from "@/types/api";

export function getRecommendations() {
  return request.get<ApiResponse<CareerRecommendation[]>>(
    "/career/recommendations",
    {
      timeout: 60000,
    },
  );
}

export function createCareerPlan(data: CreateCareerPlanRequest) {
  return request.post<ApiResponse<CareerPlan>>("/career/plans", data);
}

export function getCareerPlans() {
  return request.get<ApiResponse<CareerPlan[]>>("/career/plans");
}

export function getCareerPlan(id: number) {
  return request.get<ApiResponse<CareerPlan>>(`/career/plans/${id}`);
}

export function updateCareerPlan(id: number, data: CreateCareerPlanRequest) {
  return request.put<ApiResponse<CareerPlan>>(`/career/plans/${id}`, data);
}

export function deleteCareerPlan(id: number) {
  return request.delete<ApiResponse<void>>(`/career/plans/${id}`);
}

export function addCareerIncome(planId: number, data: AddCareerIncomeRequest) {
  return request.post<ApiResponse<CareerIncome>>(
    `/career/plans/${planId}/income`,
    data,
  );
}

export function getCareerIncomeHistory(planId: number) {
  return request.get<ApiResponse<CareerIncome[]>>(
    `/career/plans/${planId}/income`,
  );
}

export function generateStartupPlan(planId: number) {
  return request.post<ApiResponse<string>>(
    `/career/plans/${planId}/startup-plan`,
    null,
    {
      timeout: 60000,
    },
  );
}
