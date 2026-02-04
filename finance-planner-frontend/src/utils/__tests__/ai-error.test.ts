import { describe, it, expect } from "vitest";
import { mapAiError, RATE_LIMIT_CODE } from "../ai-error";

describe("ai-error utils", () => {
  it("returns friendly message for rate limit code", () => {
    expect(mapAiError(RATE_LIMIT_CODE, "x")).toBe(
      "今日对话次数已用完，请明天再试或升级会员",
    );
  });

  it("returns friendly message for rate limit text", () => {
    expect(mapAiError(undefined, "今日对话次数已用完")).toBe(
      "今日对话次数已用完，请明天再试或升级会员",
    );
  });

  it("returns original message for other errors", () => {
    expect(mapAiError(undefined, "AI 服务异常")).toBe("AI 服务异常");
  });

  it("returns undefined when message is empty", () => {
    expect(mapAiError(undefined, undefined)).toBeUndefined();
  });
});
