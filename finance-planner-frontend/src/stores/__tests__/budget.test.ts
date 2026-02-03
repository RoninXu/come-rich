import { describe, it, expect, vi, beforeEach } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { useBudgetStore } from "../budget";

vi.mock("@/api/budget", () => ({
  getBudgetSummary: vi.fn(),
}));

import { getBudgetSummary } from "@/api/budget";

describe("budget store", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setActivePinia(createPinia());
  });

  it("has null initial summary", () => {
    const store = useBudgetStore();
    expect(store.summary).toBeNull();
    expect(store.loading).toBe(false);
  });

  it("fetchBudgetSummary updates summary on success", async () => {
    const mockSummary = {
      yearMonth: "2026-02",
      totalBudget: 5000,
      totalSpent: 3000,
      totalRemaining: 2000,
      overallUtilization: 60,
      categories: [],
    };
    vi.mocked(getBudgetSummary).mockResolvedValue({
      data: { code: 200, data: mockSummary, message: "ok" },
    } as any);

    const store = useBudgetStore();
    await store.fetchBudgetSummary("2026-02");

    expect(getBudgetSummary).toHaveBeenCalledWith("2026-02");
    expect(store.summary).toEqual(mockSummary);
    expect(store.loading).toBe(false);
  });

  it("fetchBudgetSummary sets loading during fetch", async () => {
    let resolvePromise: (value: any) => void;
    const promise = new Promise((resolve) => {
      resolvePromise = resolve;
    });
    vi.mocked(getBudgetSummary).mockReturnValue(promise as any);

    const store = useBudgetStore();
    const fetchPromise = store.fetchBudgetSummary("2026-02");

    expect(store.loading).toBe(true);

    resolvePromise!({ data: { code: 200, data: null, message: "ok" } });
    await fetchPromise;

    expect(store.loading).toBe(false);
  });

  it("fetchBudgetSummary does not update summary on non-200 code", async () => {
    vi.mocked(getBudgetSummary).mockResolvedValue({
      data: { code: 400, data: null, message: "error" },
    } as any);

    const store = useBudgetStore();
    await store.fetchBudgetSummary("2026-02");

    expect(store.summary).toBeNull();
  });
});
