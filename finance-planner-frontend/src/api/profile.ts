import request from "@/utils/request";
import type { UserProfile, SaveProfileRequest } from "@/types/career";
import type { ApiResponse } from "@/types/api";

export function getProfile() {
  return request.get<ApiResponse<UserProfile>>("/profile");
}

export function saveProfile(data: SaveProfileRequest) {
  return request.put<ApiResponse<UserProfile>>("/profile", data);
}
