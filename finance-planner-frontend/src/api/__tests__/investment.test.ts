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
  getQuizQuestions,
  submitQuiz,
  getLatestAssessment,
  getAssessmentHistory,
  generateRecommendations,
  getActiveRecommendations,
  getAssetAllocation,
} from "../investment";

describe("investment API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("getQuizQuestions calls GET /investment/quiz", () => {
    getQuizQuestions();
    expect(request.get).toHaveBeenCalledWith("/investment/quiz");
  });

  it("submitQuiz calls POST /investment/quiz with answers", () => {
    const data = {
      answers: [
        { questionId: 1, answer: "A" },
        { questionId: 2, answer: "B" },
      ],
    };
    submitQuiz(data);
    expect(request.post).toHaveBeenCalledWith("/investment/quiz", data);
  });

  it("getLatestAssessment calls GET /investment/assessment", () => {
    getLatestAssessment();
    expect(request.get).toHaveBeenCalledWith("/investment/assessment");
  });

  it("getAssessmentHistory calls GET /investment/assessment/history", () => {
    getAssessmentHistory();
    expect(request.get).toHaveBeenCalledWith("/investment/assessment/history");
  });

  it("generateRecommendations calls POST /investment/recommendations", () => {
    generateRecommendations();
    expect(request.post).toHaveBeenCalledWith("/investment/recommendations");
  });

  it("getActiveRecommendations calls GET /investment/recommendations", () => {
    getActiveRecommendations();
    expect(request.get).toHaveBeenCalledWith("/investment/recommendations");
  });

  it("getAssetAllocation calls GET /investment/allocation", () => {
    getAssetAllocation();
    expect(request.get).toHaveBeenCalledWith("/investment/allocation");
  });
});
