import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { BudgetSummary } from '@/types/budget'
import { getBudgetSummary } from '@/api/budget'

export const useBudgetStore = defineStore('budget', () => {
  const summary = ref<BudgetSummary | null>(null)
  const loading = ref(false)

  async function fetchBudgetSummary(yearMonth: string) {
    loading.value = true
    try {
      const res = await getBudgetSummary(yearMonth)
      if (res.data.code === 200) {
        summary.value = res.data.data
      }
    } finally {
      loading.value = false
    }
  }

  return { summary, loading, fetchBudgetSummary }
})
