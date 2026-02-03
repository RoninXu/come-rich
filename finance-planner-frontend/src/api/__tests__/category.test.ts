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
  getCategories,
  getCategoryTree,
  getCategory,
  getExpenseCategories,
  getIncomeCategories,
} from "../category";

describe("category API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("getCategories without type calls /categories with no params", () => {
    getCategories();
    expect(request.get).toHaveBeenCalledWith("/categories", {
      params: undefined,
    });
  });

  it("getCategories with type passes type param", () => {
    getCategories(1);
    expect(request.get).toHaveBeenCalledWith("/categories", {
      params: { type: 1 },
    });
  });

  it("getCategoryTree calls /categories/tree", () => {
    getCategoryTree(2);
    expect(request.get).toHaveBeenCalledWith("/categories/tree", {
      params: { type: 2 },
    });
  });

  it("getCategory by id calls /categories/:id", () => {
    getCategory(5);
    expect(request.get).toHaveBeenCalledWith("/categories/5");
  });

  it("getExpenseCategories calls getCategories with type 2", () => {
    getExpenseCategories();
    expect(request.get).toHaveBeenCalledWith("/categories", {
      params: { type: 2 },
    });
  });
});
