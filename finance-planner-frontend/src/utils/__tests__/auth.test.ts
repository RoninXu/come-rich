import { describe, it, expect, beforeEach } from "vitest";
import {
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
  removeUser,
  clearAuth,
  isAuthenticated,
} from "../auth";

describe("auth utils", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("getToken returns null when no token is stored", () => {
    expect(getToken()).toBeNull();
  });

  it("setToken stores token in localStorage", () => {
    setToken("test-token-123");
    expect(localStorage.getItem("finance_planner_token")).toBe(
      "test-token-123",
    );
  });

  it("getToken returns stored token", () => {
    setToken("my-jwt-token");
    expect(getToken()).toBe("my-jwt-token");
  });

  it("setUser stores user as JSON string", () => {
    const user = { id: 1, username: "testuser" };
    setUser(user);
    expect(localStorage.getItem("finance_planner_user")).toBe(
      JSON.stringify(user),
    );
  });

  it("getUser parses and returns stored JSON user", () => {
    const user = { id: 1, username: "testuser", email: "test@example.com" };
    setUser(user);
    expect(getUser()).toEqual(user);
  });

  it("getUser returns null when no user is stored", () => {
    expect(getUser()).toBeNull();
  });

  it("removeToken removes token from localStorage", () => {
    setToken("token-to-remove");
    expect(getToken()).toBe("token-to-remove");
    removeToken();
    expect(getToken()).toBeNull();
  });

  it("clearAuth removes both token and user", () => {
    setToken("some-token");
    setUser({ id: 1, username: "user1" });
    clearAuth();
    expect(getToken()).toBeNull();
    expect(getUser()).toBeNull();
  });

  it("isAuthenticated returns true when token exists", () => {
    setToken("valid-token");
    expect(isAuthenticated()).toBe(true);
  });

  it("isAuthenticated returns false when no token exists", () => {
    expect(isAuthenticated()).toBe(false);
  });
});
