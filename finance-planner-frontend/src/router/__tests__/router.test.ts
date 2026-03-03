import { describe, it, expect, vi, beforeEach } from "vitest";

vi.mock("@/utils/auth", () => ({
  isAuthenticated: vi.fn(() => false),
}));

import { isAuthenticated } from "@/utils/auth";

describe("router", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.resetModules();
  });

  it("has login route", async () => {
    const mod = await import("../index");
    const router = mod.default;
    const routes = router.getRoutes();
    const loginRoute = routes.find((r) => r.name === "Login");
    expect(loginRoute).toBeDefined();
    expect(loginRoute!.path).toBe("/login");
  });

  it("has register route", async () => {
    const mod = await import("../index");
    const router = mod.default;
    const routes = router.getRoutes();
    const registerRoute = routes.find((r) => r.name === "Register");
    expect(registerRoute).toBeDefined();
    expect(registerRoute!.path).toBe("/register");
  });

  it("has dashboard route", async () => {
    const mod = await import("../index");
    const router = mod.default;
    const routes = router.getRoutes();
    const dashboardRoute = routes.find((r) => r.name === "Dashboard");
    expect(dashboardRoute).toBeDefined();
    expect(dashboardRoute!.path).toBe("/dashboard");
  });

  it("has agent metrics route", async () => {
    const mod = await import("../index");
    const router = mod.default;
    const routes = router.getRoutes();
    const metricsRoute = routes.find((r) => r.name === "AgentMetrics");
    expect(metricsRoute).toBeDefined();
    expect(metricsRoute!.path).toBe("/ai/agent-metrics");
  });

  it("has accounting routes", async () => {
    const mod = await import("../index");
    const router = mod.default;
    const routes = router.getRoutes();
    const accountingList = routes.find((r) => r.name === "AccountingList");
    const accountingNew = routes.find((r) => r.name === "AccountingNew");
    const accountingEdit = routes.find((r) => r.name === "AccountingEdit");
    expect(accountingList).toBeDefined();
    expect(accountingNew).toBeDefined();
    expect(accountingEdit).toBeDefined();
  });

  it(
    "redirects to login when not authenticated for protected route",
    async () => {
      vi.mocked(isAuthenticated).mockReturnValue(false);
      const mod = await import("../index");
      const router = mod.default;

      await router.push("/dashboard");
      await router.isReady();

      expect(router.currentRoute.value.path).toBe("/login");
    },
    12000,
  );

  it(
    "allows login page when not authenticated",
    async () => {
      vi.mocked(isAuthenticated).mockReturnValue(false);
      const mod = await import("../index");
      const router = mod.default;

      await router.push("/login");
      await router.isReady();

      expect(router.currentRoute.value.path).toBe("/login");
    },
    12000,
  );

  it("redirects to dashboard when authenticated and visiting login", async () => {
    vi.mocked(isAuthenticated).mockReturnValue(true);
    const mod = await import("../index");
    const router = mod.default;

    await router.push("/login");
    await router.isReady();

    expect(router.currentRoute.value.path).toBe("/dashboard");
  });
});
