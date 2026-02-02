import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { createDiscreteApi } from 'naive-ui'
import { getToken, clearAuth } from './auth'
import router from '@/router'
import type { ApiResponse } from '@/types/api'

const { message } = createDiscreteApi(['message'])

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    if (res.code !== 200) {
      message.error(res.message || 'Request failed')

      // Handle specific error codes
      if (res.code === 401 || res.code === 1005 || res.code === 1006) {
        clearAuth()
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || 'Request failed'))
    }

    return response
  },
  (error) => {
    const status = error.response?.status

    if (status === 401 || status === 403) {
      clearAuth()
      router.push('/login')
      return Promise.reject(error)
    }

    const msg = error.response?.data?.message || error.message || 'Network error'
    message.error(msg)

    return Promise.reject(error)
  }
)

export default request
