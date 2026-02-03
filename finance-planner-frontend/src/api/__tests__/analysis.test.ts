import { describe, it, expect, vi, beforeEach } from "vitest";

// Mock request utility
vi.mock("@/utils/request", () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

import request from "@/utils/request";
import {
  getMonthlySummary,
  getCategoryStats,
  getDailyStats,
  getHealthScore,
  getDashboard,
} from "../analysis";

describe("analysis API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("getMonthlySummary calls /analysis/monthly with year and month params", () => {
    getMonthlySummary(2024, 6);
    expect(request.get).toHaveBeenCalledWith("/analysis/monthly", {
      params: { year: 2024, month: 6 },
    });
  });

  it("getCategoryStats calls /analysis/category with params", () => {
    getCategoryStats(2024, 3, 2);
    expect(request.get).toHaveBeenCalledWith("/analysis/category", {
      params: { year: 2024, month: 3, type: 2 },
    });
  });

  it("getDailyStats calls /analysis/daily with year and month", () => {
    getDailyStats(2024, 1);
    expect(request.get).toHaveBeenCalledWith("/analysis/daily", {
      params: { year: 2024, month: 1 },
    });
  });

  it("getHealthScore calls /analysis/health-score", () => {
    getHealthScore();
    expect(request.get).toHaveBeenCalledWith("/analysis/health-score");
  });

  it("getDashboard calls /analysis/dashboard", () => {
    getDashboard();
    expect(request.get).toHaveBeenCalledWith("/analysis/dashboard");
  });
});
