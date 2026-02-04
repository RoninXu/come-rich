import { describe, it, expect, vi, beforeEach } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { useInvestmentStore } from "../investment";

vi.mock("@/api/investment", () => ({
  getLatestAssessment: vi.fn(),
  getActiveRecommendations: vi.fn(),
}));

import {
  getLatestAssessment,
  getActiveRecommendations,
} from "@/api/investment";

describe("investment store", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setActivePinia(createPinia());
  });

  it("has null initial state", () => {
    const store = useInvestmentStore();
    expect(store.assessment).toBeNull();
    expect(store.recommendations).toEqual([]);
    expect(store.loading).toBe(false);
  });

  it("fetchAssessment updates assessment on success", async () => {
    const mockAssessment = {
      id: 1,
      riskScore: 15,
      riskLevel: "稳健型",
      assessmentDate: "2026-02-01",
    };
    vi.mocked(getLatestAssessment).mockResolvedValue({
      data: { code: 200, data: mockAssessment, message: "ok" },
    } as any);

    const store = useInvestmentStore();
    await store.fetchAssessment();

    expect(getLatestAssessment).toHaveBeenCalled();
    expect(store.assessment).toEqual(mockAssessment);
  });

  it("fetchAssessment sets assessment to null on error", async () => {
    vi.mocked(getLatestAssessment).mockRejectedValue(
      new Error("network error"),
    );

    const store = useInvestmentStore();
    store.assessment = { id: 1 } as any;
    await store.fetchAssessment();

    expect(store.assessment).toBeNull();
  });

  it("fetchRecommendations updates recommendations on success", async () => {
    const mockRecs = [
      { id: 1, trackName: "债券基金", allocationPercentage: 40 },
      { id: 2, trackName: "指数基金", allocationPercentage: 60 },
    ];
    vi.mocked(getActiveRecommendations).mockResolvedValue({
      data: { code: 200, data: mockRecs, message: "ok" },
    } as any);

    const store = useInvestmentStore();
    await store.fetchRecommendations();

    expect(getActiveRecommendations).toHaveBeenCalled();
    expect(store.recommendations).toEqual(mockRecs);
    expect(store.loading).toBe(false);
  });

  it("fetchRecommendations sets loading during fetch", async () => {
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    vi.mocked(getActiveRecommendations).mockReturnValue(promise as any);

    const store = useInvestmentStore();
    const fetchPromise = store.fetchRecommendations();

    expect(store.loading).toBe(true);

    resolvePromise!({ data: { code: 200, data: [], message: "ok" } });
    await fetchPromise;

    expect(store.loading).toBe(false);
  });
});
