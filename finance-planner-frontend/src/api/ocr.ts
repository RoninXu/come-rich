import request from '@/utils/request'
import type { OcrPreview, OcrConfirmRequest } from '@/types/ocr'
import type { ApiResponse } from '@/types/api'

export function uploadReceipt(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<OcrPreview>>('/accounting/ocr/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}

export function confirmOcrRecord(id: number, data: OcrConfirmRequest) {
  return request.post<ApiResponse<OcrPreview>>(`/accounting/ocr/${id}/confirm`, data)
}

export function rejectOcrRecord(id: number) {
  return request.post<ApiResponse<void>>(`/accounting/ocr/${id}/reject`)
}

export function getPendingOcrRecords() {
  return request.get<ApiResponse<OcrPreview[]>>('/accounting/ocr/pending')
}
