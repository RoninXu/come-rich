import { describe, it, expect, vi, beforeEach } from "vitest";
import { streamChat } from "../ai";

vi.mock("@/utils/auth", () => ({
  getToken: vi.fn(() => "test-token"),
}));

function makeStream(chunks: string[]) {
  return new ReadableStream({
    start(controller) {
      chunks.forEach((chunk) =>
        controller.enqueue(new TextEncoder().encode(chunk)),
      );
      controller.close();
    },
  });
}

describe("ai stream error handling", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("maps rate limit error from first JSON chunk", async () => {
    const stream = makeStream([
      JSON.stringify({ code: 4001, message: "今日对话次数已用完" }),
    ]);
    const response = new Response(stream, {
      headers: { "content-type": "text/event-stream" },
      status: 200,
    });
    vi.stubGlobal("fetch", vi.fn(() => Promise.resolve(response)) as any);

    const generator = streamChat("hi");
    const first = await generator.next();
    expect(first.value?.error).toBe("今日对话次数已用完，请明天再试或升级会员");
  });

  it("maps rate limit error from SSE data payload", async () => {
    const stream = makeStream([
      "data: " +
        JSON.stringify({ code: 4001, message: "今日对话次数已用完" }) +
        "\n\n",
    ]);
    const response = new Response(stream, {
      headers: { "content-type": "text/event-stream" },
      status: 200,
    });
    vi.stubGlobal("fetch", vi.fn(() => Promise.resolve(response)) as any);

    const generator = streamChat("hi");
    const first = await generator.next();
    expect(first.value?.error).toBe("今日对话次数已用完，请明天再试或升级会员");
  });
});
