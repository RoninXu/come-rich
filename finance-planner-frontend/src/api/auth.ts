import request from '@/utils/request'
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '@/types/user'
import type { ApiResponse } from '@/types/api'

export function login(data: LoginRequest) {
  return request.post<ApiResponse<LoginResponse>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<ApiResponse<void>>('/auth/register', data)
}

export function getCurrentUser() {
  return request.get<ApiResponse<User>>('/auth/me')
}
