// Category types
export interface Category {
  id: number;
  name: string;
  parentId: number | null;
  type: number; // 1=income, 2=expense
  icon: string | null;
  color: string | null;
  sortOrder: number;
  isSystem: boolean;
  children?: Category[];
}

// Transaction types
export interface Transaction {
  id: number;
  amount: number;
  type: number; // 1=income, 2=expense
  categoryId: number;
  categoryName: string | null;
  categoryIcon: string | null;
  categoryColor: string | null;
  description: string | null;
  transactionDate: string; // YYYY-MM-DD
  paymentMethod: string | null;
  merchant: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTransactionRequest {
  amount: number;
  type: number; // 1=income, 2=expense
  categoryId: number;
  description?: string;
  transactionDate: string; // YYYY-MM-DD
  paymentMethod?: string;
  merchant?: string;
}

export interface UpdateTransactionRequest {
  amount?: number;
  type?: number;
  categoryId?: number;
  description?: string;
  transactionDate?: string;
  paymentMethod?: string;
  merchant?: string;
}

export interface TransactionQueryParams {
  type?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  pageSize?: number;
}
