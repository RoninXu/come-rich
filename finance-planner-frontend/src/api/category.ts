import request from '@/utils/request'
import type { Category } from '@/types/accounting'
import type { ApiResponse } from '@/types/api'

/**
 * Get all categories as a flat list
 */
export function getCategories(type?: number) {
  return request.get<ApiResponse<Category[]>>('/categories', {
    params: type ? { type } : undefined
  })
}

/**
 * Get categories as a tree structure (parent with children)
 */
export function getCategoryTree(type?: number) {
  return request.get<ApiResponse<Category[]>>('/categories/tree', {
    params: type ? { type } : undefined
  })
}

/**
 * Get a single category by ID
 */
export function getCategory(id: number) {
  return request.get<ApiResponse<Category>>(`/categories/${id}`)
}

/**
 * Get expense categories (type=2)
 */
export function getExpenseCategories() {
  return getCategories(2)
}

/**
 * Get income categories (type=1)
 */
export function getIncomeCategories() {
  return getCategories(1)
}

/**
 * Get expense category tree
 */
export function getExpenseCategoryTree() {
  return getCategoryTree(2)
}

/**
 * Get income category tree
 */
export function getIncomeCategoryTree() {
  return getCategoryTree(1)
}
