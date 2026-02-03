import { defineStore } from "pinia";
import { ref } from "vue";
import type { Goal } from "@/types/goal";
import { getGoals } from "@/api/goal";

export const useGoalStore = defineStore("goal", () => {
  const goals = ref<Goal[]>([]);
  const loading = ref(false);

  async function fetchGoals(status?: number) {
    loading.value = true;
    try {
      const res = await getGoals(status);
      if (res.data.code === 200) {
        goals.value = res.data.data;
      }
    } finally {
      loading.value = false;
    }
  }

  return { goals, loading, fetchGoals };
});
