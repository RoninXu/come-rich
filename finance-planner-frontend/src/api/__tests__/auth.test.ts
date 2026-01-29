import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock request utility
vi.mock('@/utils/request', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

import request from '@/utils/request'
import { login, register, getCurrentUser } from '../auth'

describe('auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login posts to /auth/login with credentials', () => {
    const credentials = { username: 'testuser', password: 'password123' }
    login(credentials)
    expect(request.post).toHaveBeenCalledWith('/auth/login', credentials)
  })

  it('register posts to /auth/register with user data', () => {
    const userData = { username: 'newuser', password: 'pass123', email: 'new@test.com' }
    register(userData)
    expect(request.post).toHaveBeenCalledWith('/auth/register', userData)
  })

  it('getCurrentUser gets /auth/me', () => {
    getCurrentUser()
    expect(request.get).toHaveBeenCalledWith('/auth/me')
  })
})
