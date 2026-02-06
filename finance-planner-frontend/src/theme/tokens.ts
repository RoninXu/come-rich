/**
 * Copilot-inspired visual token system for Come Rich.
 * Keep it neutral, dense, and finance-first.
 */

export const colors = {
  light: {
    primary: "#1463ff",
    primaryHover: "#0f55e0",
    primaryPressed: "#0a42b8",
    success: "#16a34a",
    warning: "#f59e0b",
    error: "#dc2626",
    info: "#0284c7",

    bgPage: "#f5f7fb",
    bgElevated: "#ffffff",
    bgCard: "#ffffff",
    bgSidebar: "#eef3ff",
    bgHeader: "rgba(255, 255, 255, 0.92)",
    bgInput: "#f1f5f9",
    bgHover: "rgba(20, 99, 255, 0.08)",

    textPrimary: "#0f172a",
    textSecondary: "#475569",
    textTertiary: "#64748b",
    textInverse: "#ffffff",

    border: "#e2e8f0",
    borderLight: "#edf2f7",
    divider: "#e2e8f0",
  },
  dark: {
    primary: "#4f8dff",
    primaryHover: "#6aa0ff",
    primaryPressed: "#3277ff",
    success: "#22c55e",
    warning: "#f59e0b",
    error: "#ef4444",
    info: "#38bdf8",

    bgPage: "#0b1220",
    bgElevated: "#111b2d",
    bgCard: "#152238",
    bgSidebar: "#10192b",
    bgHeader: "rgba(11, 18, 32, 0.9)",
    bgInput: "#1e293b",
    bgHover: "rgba(79, 141, 255, 0.16)",

    textPrimary: "#e2e8f0",
    textSecondary: "#cbd5e1",
    textTertiary: "#94a3b8",
    textInverse: "#0b1220",

    border: "#24354f",
    borderLight: "#1f2d42",
    divider: "#24354f",
  },
} as const;

export const radius = {
  xs: "6px",
  sm: "8px",
  md: "12px",
  lg: "16px",
  xl: "20px",
  xxl: "24px",
  full: "9999px",
} as const;

export const spacing = {
  xs: "4px",
  sm: "8px",
  md: "12px",
  lg: "16px",
  xl: "20px",
  xxl: "24px",
  xxxl: "32px",
  display: "40px",
} as const;

export const shadows = {
  light: {
    sm: "0 1px 2px rgba(15, 23, 42, 0.06)",
    md: "0 8px 24px rgba(15, 23, 42, 0.08)",
    lg: "0 14px 38px rgba(15, 23, 42, 0.12)",
    xl: "0 24px 60px rgba(15, 23, 42, 0.16)",
  },
  dark: {
    sm: "0 1px 2px rgba(2, 6, 23, 0.4)",
    md: "0 8px 24px rgba(2, 6, 23, 0.42)",
    lg: "0 14px 38px rgba(2, 6, 23, 0.52)",
    xl: "0 24px 60px rgba(2, 6, 23, 0.62)",
  },
} as const;

export const typography = {
  fontFamily:
    "'PingFang SC', 'SF Pro Display', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  fontMono:
    "'SF Mono', 'JetBrains Mono', 'Fira Code', Menlo, Monaco, Consolas, monospace",
  fontSizeXs: "11px",
  fontSizeSm: "13px",
  fontSizeMd: "14px",
  fontSizeLg: "16px",
  fontSizeXl: "20px",
  fontSizeXxl: "28px",
  fontSizeDisplay: "34px",
  fontWeightRegular: 400,
  fontWeightMedium: 500,
  fontWeightSemibold: 600,
  fontWeightBold: 700,
  lineHeight: 1.45,
} as const;

export const blur = {
  sm: "4px",
  md: "10px",
  lg: "18px",
} as const;

export type ThemeMode = "light" | "dark";
