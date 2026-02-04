import { defineStore } from "pinia";
import { ref } from "vue";
import type {
  RiskAssessment,
  InvestmentRecommendation,
} from "@/types/investment";
import {
  getLatestAssessment,
  getActiveRecommendations,
} from "@/api/investment";

export const useInvestmentStore = defineStore("investment", () => {
  const assessment = ref<RiskAssessment | null>(null);
  const recommendations = ref<InvestmentRecommendation[]>([]);
  const loading = ref(false);

  async function fetchAssessment() {
    try {
      const res = await getLatestAssessment();
      if (res.data.code === 200) {
        assessment.value = res.data.data;
      }
    } catch {
      assessment.value = null;
    }
  }

  async function fetchRecommendations() {
    loading.value = true;
    try {
      const res = await getActiveRecommendations();
      if (res.data.code === 200) {
        recommendations.value = res.data.data;
      }
    } finally {
      loading.value = false;
    }
  }

  return {
    assessment,
    recommendations,
    loading,
    fetchAssessment,
    fetchRecommendations,
  };
});
