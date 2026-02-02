/**
 * Design tokens for Come Rich — Apple/macOS-inspired design language.
 * Provides light & dark color palettes, spacing, radius, shadows, and typography.
 */

export const colors = {
  light: {
    primary: '#007AFF',
    primaryHover: '#0066D6',
    primaryPressed: '#004DB3',
    success: '#34C759',
    warning: '#FF9500',
    error: '#FF3B30',
    info: '#5AC8FA',

    bgPage: '#F2F2F7',
    bgCard: 'rgba(255, 255, 255, 0.72)',
    bgCardSolid: '#FFFFFF',
    bgSidebar: 'rgba(246, 246, 246, 0.80)',
    bgHeader: 'rgba(255, 255, 255, 0.72)',
    bgInput: 'rgba(118, 118, 128, 0.08)',
    bgHover: 'rgba(0, 0, 0, 0.04)',

    textPrimary: '#1D1D1F',
    textSecondary: '#86868B',
    textTertiary: '#AEAEB2',
    textInverse: '#FFFFFF',

    border: 'rgba(0, 0, 0, 0.08)',
    borderLight: 'rgba(0, 0, 0, 0.04)',
    divider: 'rgba(0, 0, 0, 0.06)',
  },
  dark: {
    primary: '#0A84FF',
    primaryHover: '#409CFF',
    primaryPressed: '#0066CC',
    success: '#30D158',
    warning: '#FF9F0A',
    error: '#FF453A',
    info: '#64D2FF',

    bgPage: '#1C1C1E',
    bgCard: 'rgba(44, 44, 46, 0.72)',
    bgCardSolid: '#2C2C2E',
    bgSidebar: 'rgba(28, 28, 30, 0.85)',
    bgHeader: 'rgba(44, 44, 46, 0.72)',
    bgInput: 'rgba(118, 118, 128, 0.24)',
    bgHover: 'rgba(255, 255, 255, 0.06)',

    textPrimary: '#F5F5F7',
    textSecondary: '#98989D',
    textTertiary: '#636366',
    textInverse: '#1D1D1F',

    border: 'rgba(255, 255, 255, 0.10)',
    borderLight: 'rgba(255, 255, 255, 0.06)',
    divider: 'rgba(255, 255, 255, 0.08)',
  },
} as const

export const radius = {
  xs: '4px',
  sm: '6px',
  md: '8px',
  lg: '12px',
  xl: '14px',
  xxl: '20px',
  full: '9999px',
} as const

export const spacing = {
  xs: '4px',
  sm: '8px',
  md: '12px',
  lg: '16px',
  xl: '20px',
  xxl: '24px',
  xxxl: '32px',
} as const

export const shadows = {
  light: {
    sm: '0 1px 3px rgba(0, 0, 0, 0.04)',
    md: '0 4px 12px rgba(0, 0, 0, 0.06)',
    lg: '0 8px 24px rgba(0, 0, 0, 0.08)',
    xl: '0 16px 48px rgba(0, 0, 0, 0.12)',
  },
  dark: {
    sm: '0 1px 3px rgba(0, 0, 0, 0.20)',
    md: '0 4px 12px rgba(0, 0, 0, 0.30)',
    lg: '0 8px 24px rgba(0, 0, 0, 0.40)',
    xl: '0 16px 48px rgba(0, 0, 0, 0.50)',
  },
} as const

export const typography = {
  fontFamily: "-apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC', 'Helvetica Neue', Helvetica, 'Microsoft YaHei', Arial, sans-serif",
  fontSizeXs: '11px',
  fontSizeSm: '13px',
  fontSizeMd: '14px',
  fontSizeLg: '17px',
  fontSizeXl: '22px',
  fontSizeXxl: '28px',
  fontSizeDisplay: '34px',
  fontWeightRegular: 400,
  fontWeightMedium: 500,
  fontWeightSemibold: 600,
  fontWeightBold: 700,
  lineHeight: 1.5,
} as const

export const blur = {
  sm: '10px',
  md: '20px',
  lg: '40px',
} as const

export type ThemeMode = 'light' | 'dark'
