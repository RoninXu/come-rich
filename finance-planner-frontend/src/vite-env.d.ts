/// <reference types="vite/client" />

declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

// Environment Variables Type Definitions
interface ImportMetaEnv {
  // API Configuration
  readonly VITE_API_BASE_URL: string;
  readonly VITE_API_TIMEOUT: string;

  // Application Configuration
  readonly VITE_APP_TITLE: string;
  readonly VITE_APP_VERSION: string;

  // Feature Flags
  readonly VITE_FEATURE_OCR_ENABLED: string;
  readonly VITE_FEATURE_AI_CHAT_ENABLED: string;
  readonly VITE_FEATURE_INVESTMENT_ENABLED: string;

  // Debug
  readonly VITE_DEBUG: string;

  // Third-party Services (optional)
  readonly VITE_ANALYTICS_ID?: string;
  readonly VITE_SENTRY_DSN?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
