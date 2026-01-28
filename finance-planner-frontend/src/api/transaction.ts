import request from '@/utils/request'
import type {
  Transaction,
  CreateTransactionRequest,
  UpdateTransactionRequest,
  TransactionQueryParams
} from '@/types/accounting'
import type { ApiResponse, PageResponse } from '@/types/api'

/**
 * Create a new transaction
 */
export function createTransaction(data: CreateTransactionRequest) {
  return request.post<ApiResponse<Transaction>>('/transactions', data)
}

/**
 * Update an existing transaction
 */
export function updateTransaction(id: number, data: UpdateTransactionRequest) {
  return request.put<ApiResponse<Transaction>>(`/transactions/${id}`, data)
}

/**
 * Delete a transaction (soft delete)
 */
export function deleteTransaction(id: number) {
  return request.delete<ApiResponse<void>>(`/transactions/${id}`)
}

/**
 * Get a single transaction by ID
 */
export function getTransaction(id: number) {
  return request.get<ApiResponse<Transaction>>(`/transactions/${id}`)
}

/**
 * List transactions with pagination and filters
 */
export function getTransactions(params?: TransactionQueryParams) {
  return request.get<ApiResponse<PageResponse<Transaction>>>('/transactions', {
    params
  })
}

/**
 * Get recent transactions (for dashboard)
 */
export function getRecentTransactions() {
  return request.get<ApiResponse<Transaction[]>>('/transactions/recent')
}

/**
 * Get transactions for a specific month
 */
export function getMonthlyTransactions(year: number, month: number) {
  return request.get<ApiResponse<Transaction[]>>(`/transactions/monthly/${year}/${month}`)
}
