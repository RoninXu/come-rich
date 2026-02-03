import request from "@/utils/request";
import type {
  Goal,
  GoalProgress,
  CreateGoalRequest,
  UpdateGoalRequest,
  AddProgressRequest,
  GoalAiPlan,
} from "@/types/goal";
import type { ApiResponse } from "@/types/api";

export function createGoal(data: CreateGoalRequest) {
  return request.post<ApiResponse<Goal>>("/goals", data);
}

export function getGoals(status?: number) {
  return request.get<ApiResponse<Goal[]>>("/goals", {
    params: status !== undefined ? { status } : undefined,
  });
}

export function getGoal(id: number) {
  return request.get<ApiResponse<Goal>>(`/goals/${id}`);
}

export function updateGoal(id: number, data: UpdateGoalRequest) {
  return request.put<ApiResponse<Goal>>(`/goals/${id}`, data);
}

export function deleteGoal(id: number) {
  return request.delete<ApiResponse<void>>(`/goals/${id}`);
}

export function addProgress(goalId: number, data: AddProgressRequest) {
  return request.post<ApiResponse<GoalProgress>>(
    `/goals/${goalId}/progress`,
    data,
  );
}

export function getProgressHistory(goalId: number) {
  return request.get<ApiResponse<GoalProgress[]>>(`/goals/${goalId}/progress`);
}

export function getActiveGoalCount() {
  return request.get<ApiResponse<number>>("/goals/active-count");
}

export function generateAiPlan(goalId: number) {
  return request.post<ApiResponse<GoalAiPlan>>(`/goals/${goalId}/ai-plan`);
}
