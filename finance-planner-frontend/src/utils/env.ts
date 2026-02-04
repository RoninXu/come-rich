/**
 * Environment Configuration Utility
 * Provides type-safe access to environment variables
 */

// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
export const API_TIMEOUT = parseInt(import.meta.env.VITE_API_TIMEOUT || '30000', 10)

// Application Configuration
export const APP_TITLE = import.meta.env.VITE_APP_TITLE || 'Come Rich - AI Financial Planner'
export const APP_VERSION = import.meta.env.VITE_APP_VERSION || '1.0.0'

// Feature Flags
export const FEATURE_FLAGS = {
  ocrEnabled: import.meta.env.VITE_FEATURE_OCR_ENABLED !== 'false',
  aiChatEnabled: import.meta.env.VITE_FEATURE_AI_CHAT_ENABLED !== 'false',
  investmentEnabled: import.meta.env.VITE_FEATURE_INVESTMENT_ENABLED !== 'false',
} as const

// Debug Mode
export const IS_DEBUG = import.meta.env.VITE_DEBUG === 'true'
export const IS_PRODUCTION = import.meta.env.PROD
export const IS_DEVELOPMENT = import.meta.env.DEV

// Third-party Services
export const ANALYTICS_ID = import.meta.env.VITE_ANALYTICS_ID || ''
export const SENTRY_DSN = import.meta.env.VITE_SENTRY_DSN || ''

/**
 * Check if a feature is enabled
 */
export function isFeatureEnabled(feature: keyof typeof FEATURE_FLAGS): boolean {
  return FEATURE_FLAGS[feature]
}

/**
 * Get environment info for debugging
 */
export function getEnvInfo() {
  return {
    apiBaseUrl: API_BASE_URL,
    apiTimeout: API_TIMEOUT,
    appTitle: APP_TITLE,
    appVersion: APP_VERSION,
    features: FEATURE_FLAGS,
    isDebug: IS_DEBUG,
    isProduction: IS_PRODUCTION,
    isDevelopment: IS_DEVELOPMENT,
  }
}

// Log environment info in development mode
if (IS_DEVELOPMENT && IS_DEBUG) {
  console.log('[ENV] Environment Configuration:', getEnvInfo())
}
