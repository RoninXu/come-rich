import { defineStore } from "pinia";
import { ref } from "vue";
import type { CareerPlan } from "@/types/career";
import { getCareerPlans } from "@/api/career";

export const useCareerStore = defineStore("career", () => {
  const plans = ref<CareerPlan[]>([]);
  const loading = ref(false);

  async function fetchPlans() {
    loading.value = true;
    try {
      const res = await getCareerPlans();
      if (res.data.code === 200) {
        plans.value = res.data.data;
      }
    } finally {
      loading.value = false;
    }
  }

  return { plans, loading, fetchPlans };
});
