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
  createTransaction,
  updateTransaction,
  deleteTransaction,
  getTransaction,
  getTransactions,
  getRecentTransactions,
  getMonthlyTransactions,
} from "../transaction";

describe("transaction API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("createTransaction posts to /transactions", () => {
    const data = { categoryId: 1, amount: 100, type: 2, note: "lunch" };
    createTransaction(data as any);
    expect(request.post).toHaveBeenCalledWith("/transactions", data);
  });

  it("updateTransaction puts to /transactions/:id", () => {
    const data = { categoryId: 1, amount: 150, type: 2, note: "dinner" };
    updateTransaction(42, data as any);
    expect(request.put).toHaveBeenCalledWith("/transactions/42", data);
  });

  it("deleteTransaction deletes /transactions/:id", () => {
    deleteTransaction(7);
    expect(request.delete).toHaveBeenCalledWith("/transactions/7");
  });

  it("getTransaction gets /transactions/:id", () => {
    getTransaction(15);
    expect(request.get).toHaveBeenCalledWith("/transactions/15");
  });

  it("getTransactions with params calls /transactions with query params", () => {
    const params = { page: 1, size: 10, type: 2 };
    getTransactions(params as any);
    expect(request.get).toHaveBeenCalledWith("/transactions", {
      params,
    });
  });

  it("getRecentTransactions gets /transactions/recent", () => {
    getRecentTransactions();
    expect(request.get).toHaveBeenCalledWith("/transactions/recent");
  });

  it("getMonthlyTransactions gets /transactions/monthly/:year/:month", () => {
    getMonthlyTransactions(2024, 3);
    expect(request.get).toHaveBeenCalledWith("/transactions/monthly/2024/3");
  });
});
