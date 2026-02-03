import { describe, it, expect, vi, beforeEach } from "vitest";

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
  exportTransactionsExcel,
  exportTransactionsCsv,
  exportMonthlyReport,
  exportAnnualReport,
} from "../export";

describe("export API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("exportTransactionsExcel calls GET /export/transactions/excel with blob responseType", () => {
    exportTransactionsExcel("2026-01-01", "2026-01-31");
    expect(request.get).toHaveBeenCalledWith("/export/transactions/excel", {
      params: {
        startDate: "2026-01-01",
        endDate: "2026-01-31",
        type: undefined,
        categoryId: undefined,
      },
      responseType: "blob",
    });
  });

  it("exportTransactionsExcel passes type and categoryId when provided", () => {
    exportTransactionsExcel("2026-01-01", "2026-01-31", 2, 10);
    expect(request.get).toHaveBeenCalledWith("/export/transactions/excel", {
      params: {
        startDate: "2026-01-01",
        endDate: "2026-01-31",
        type: 2,
        categoryId: 10,
      },
      responseType: "blob",
    });
  });

  it("exportTransactionsCsv calls GET /export/transactions/csv with blob responseType", () => {
    exportTransactionsCsv("2026-02-01", "2026-02-28");
    expect(request.get).toHaveBeenCalledWith("/export/transactions/csv", {
      params: {
        startDate: "2026-02-01",
        endDate: "2026-02-28",
        type: undefined,
        categoryId: undefined,
      },
      responseType: "blob",
    });
  });

  it("exportMonthlyReport calls GET /export/report/monthly with year and month", () => {
    exportMonthlyReport(2026, 2);
    expect(request.get).toHaveBeenCalledWith("/export/report/monthly", {
      params: { year: 2026, month: 2 },
      responseType: "blob",
    });
  });

  it("exportAnnualReport calls GET /export/report/annual with year", () => {
    exportAnnualReport(2026);
    expect(request.get).toHaveBeenCalledWith("/export/report/annual", {
      params: { year: 2026 },
      responseType: "blob",
    });
  });
});
