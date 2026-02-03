import { describe, it, expect, vi, beforeEach } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { useUserStore } from "../user";

// Mock auth API
vi.mock("@/api/auth", () => ({
  login: vi.fn(),
  register: vi.fn(),
  getCurrentUser: vi.fn(),
}));

// Mock auth utils
vi.mock("@/utils/auth", () => ({
  getToken: vi.fn(() => null),
  setToken: vi.fn(),
  getUser: vi.fn(() => null),
  setUser: vi.fn(),
  clearAuth: vi.fn(),
}));

import { login, register, getCurrentUser } from "@/api/auth";
import { getToken, setToken, getUser, setUser, clearAuth } from "@/utils/auth";

describe("user store", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setActivePinia(createPinia());
  });

  it("has initial state from localStorage (null when empty)", () => {
    vi.mocked(getToken).mockReturnValue(null);
    vi.mocked(getUser).mockReturnValue(null);

    const store = useUserStore();
    expect(store.token).toBeNull();
    expect(store.user).toBeNull();
  });

  it("isLoggedIn computed returns true when token exists", () => {
    vi.mocked(getToken).mockReturnValue("some-token");
    vi.mocked(getUser).mockReturnValue(null);

    // Need a fresh pinia to pick up the new mock return
    setActivePinia(createPinia());
    const store = useUserStore();
    // Manually set token since the store reads it at creation
    store.token = "some-token";
    expect(store.isLoggedIn).toBe(true);
  });

  it("isLoggedIn computed returns false when no token", () => {
    vi.mocked(getToken).mockReturnValue(null);
    const store = useUserStore();
    expect(store.isLoggedIn).toBe(false);
  });

  it("username computed returns username from user", () => {
    const store = useUserStore();
    store.user = { id: 1, username: "john", email: "john@test.com" } as any;
    expect(store.username).toBe("john");
  });

  it("username computed returns empty string when no user", () => {
    const store = useUserStore();
    expect(store.username).toBe("");
  });

  it("loginAction stores token and user from API response", async () => {
    const mockData = {
      token: "jwt-token-abc",
      user: { id: 1, username: "testuser", email: "test@test.com" },
    };
    vi.mocked(login).mockResolvedValue({
      data: { code: 200, data: mockData, message: "ok" },
    } as any);

    const store = useUserStore();
    const result = await store.loginAction({
      username: "testuser",
      password: "pass123",
    });

    expect(result).toEqual(mockData);
    expect(store.token).toBe("jwt-token-abc");
    expect(store.user).toEqual(mockData.user);
  });

  it("loginAction calls setToken and setUser", async () => {
    const mockData = {
      token: "jwt-token-abc",
      user: { id: 1, username: "testuser" },
    };
    vi.mocked(login).mockResolvedValue({
      data: { code: 200, data: mockData, message: "ok" },
    } as any);

    const store = useUserStore();
    await store.loginAction({ username: "testuser", password: "pass123" });

    expect(setToken).toHaveBeenCalledWith("jwt-token-abc");
    expect(setUser).toHaveBeenCalledWith(mockData.user);
  });

  it("registerAction calls register API", async () => {
    vi.mocked(register).mockResolvedValue({
      data: { code: 200, data: null, message: "ok" },
    } as any);

    const store = useUserStore();
    await store.registerAction({
      username: "newuser",
      password: "pass123",
      email: "new@test.com",
    });

    expect(register).toHaveBeenCalledWith({
      username: "newuser",
      password: "pass123",
      email: "new@test.com",
    });
  });

  it("fetchCurrentUser updates user state", async () => {
    const mockUser = {
      id: 1,
      username: "currentuser",
      email: "current@test.com",
    };
    vi.mocked(getCurrentUser).mockResolvedValue({
      data: { code: 200, data: mockUser, message: "ok" },
    } as any);

    const store = useUserStore();
    const result = await store.fetchCurrentUser();

    expect(result).toEqual(mockUser);
    expect(store.user).toEqual(mockUser);
    expect(setUser).toHaveBeenCalledWith(mockUser);
  });

  it("logout clears state and calls clearAuth", () => {
    const store = useUserStore();
    store.token = "some-token";
    store.user = { id: 1, username: "user1" } as any;

    store.logout();

    expect(store.token).toBeNull();
    expect(store.user).toBeNull();
    expect(clearAuth).toHaveBeenCalled();
  });
});
