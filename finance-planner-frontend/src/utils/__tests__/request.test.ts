import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock message object shared across test
const mockMessage = {
  error: vi.fn(),
  success: vi.fn(),
  warning: vi.fn(),
  info: vi.fn()
}

// Mock naive-ui before any imports that use it
vi.mock('naive-ui', () => ({
  createDiscreteApi: vi.fn(() => ({
    message: mockMessage
  }))
}))

// Mock router
vi.mock('@/router', () => ({
  default: {
    push: vi.fn()
  }
}))

// Mock auth utilities
vi.mock('../auth', () => ({
  getToken: vi.fn(),
  clearAuth: vi.fn()
}))

import { getToken, clearAuth } from '../auth'
import router from '@/router'

describe('request utils', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset module registry so request.ts re-evaluates
    vi.resetModules()
  })

  it('creates axios instance with correct baseURL', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default
    expect(request.defaults.baseURL).toBe('/api')
  })

  it('creates axios instance with correct timeout', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default
    expect(request.defaults.timeout).toBe(10000)
  })

  it('creates axios instance with JSON content type header', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default
    expect(request.defaults.headers['Content-Type']).toBe('application/json')
  })

  it('request interceptor adds auth header when token exists', async () => {
    vi.mocked(getToken).mockReturnValue('test-bearer-token')
    const mod = await import('../request')
    const request = mod.default

    // Access the request interceptor
    const interceptors = (request.interceptors.request as any).handlers
    expect(interceptors.length).toBeGreaterThan(0)

    const config = { headers: {} as any }
    const fulfilled = interceptors[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers.Authorization).toBe('Bearer test-bearer-token')
  })

  it('request interceptor skips auth header when no token', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default

    const interceptors = (request.interceptors.request as any).handlers
    const config = { headers: {} as any }
    const fulfilled = interceptors[0].fulfilled
    const result = fulfilled(config)
    expect(result.headers.Authorization).toBeUndefined()
  })

  it('response interceptor returns response for code 200', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default

    const interceptors = (request.interceptors.response as any).handlers
    const fulfilled = interceptors[0].fulfilled
    const mockResponse = { data: { code: 200, data: { id: 1 }, message: 'ok' } }
    const result = fulfilled(mockResponse)
    expect(result).toEqual(mockResponse)
  })

  it('response interceptor rejects for non-200 code', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default

    const interceptors = (request.interceptors.response as any).handlers
    const fulfilled = interceptors[0].fulfilled
    const mockResponse = { data: { code: 500, message: 'Internal error' } }

    await expect(Promise.resolve().then(() => fulfilled(mockResponse))).rejects.toThrow('Internal error')
    expect(mockMessage.error).toHaveBeenCalledWith('Internal error')
  })

  it('response interceptor handles 401 by clearing auth and redirecting', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default

    const interceptors = (request.interceptors.response as any).handlers
    const fulfilled = interceptors[0].fulfilled
    const mockResponse = { data: { code: 401, message: 'Unauthorized' } }

    await expect(Promise.resolve().then(() => fulfilled(mockResponse))).rejects.toThrow('Unauthorized')

    expect(clearAuth).toHaveBeenCalled()
    expect(router.push).toHaveBeenCalledWith('/login')
  })

  it('response interceptor handles network errors', async () => {
    vi.mocked(getToken).mockReturnValue(null)
    const mod = await import('../request')
    const request = mod.default

    const interceptors = (request.interceptors.response as any).handlers
    const rejected = interceptors[0].rejected
    const error = { message: 'Network Error', response: undefined }

    await expect(Promise.resolve().then(() => rejected(error))).rejects.toEqual(error)
    expect(mockMessage.error).toHaveBeenCalledWith('Network Error')
  })
})
