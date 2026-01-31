export interface OcrPreview {
  id: number
  originalFilename: string | null
  ocrRawText: string | null
  extractedAmount: number | null
  extractedMerchant: string | null
  extractedDate: string | null // YYYY-MM-DD
  suggestedCategoryId: number | null
  suggestedCategoryName: string | null
  status: number // 1=pending, 2=confirmed, 3=rejected
  createdAt: string
}

export interface OcrConfirmRequest {
  amount: number
  categoryId: number
  transactionDate: string // YYYY-MM-DD
  description?: string
  merchant?: string
}
