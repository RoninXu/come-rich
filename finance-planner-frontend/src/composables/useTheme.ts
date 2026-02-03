import { ref, computed, watch, onMounted } from "vue";
import { darkTheme } from "naive-ui";
import type { GlobalTheme } from "naive-ui";
import { lightThemeOverrides, darkThemeOverrides } from "@/theme";
import type { ThemeMode } from "@/theme";

const STORAGE_KEY = "cr-theme-mode";

const mode = ref<ThemeMode>("light");

function detectSystemTheme(): ThemeMode {
  if (typeof window === "undefined") return "light";
  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

function applyThemeToDOM(m: ThemeMode) {
  document.documentElement.setAttribute("data-theme", m);
}

export function useTheme() {
  onMounted(() => {
    const stored = localStorage.getItem(STORAGE_KEY) as ThemeMode | null;
    mode.value = stored || detectSystemTheme();
    applyThemeToDOM(mode.value);

    // Listen for system theme changes
    window
      .matchMedia("(prefers-color-scheme: dark)")
      .addEventListener("change", (e) => {
        if (!localStorage.getItem(STORAGE_KEY)) {
          mode.value = e.matches ? "dark" : "light";
        }
      });
  });

  watch(mode, (m) => {
    applyThemeToDOM(m);
  });

  const isDark = computed(() => mode.value === "dark");

  const naiveTheme = computed<GlobalTheme | null>(() => {
    return isDark.value ? darkTheme : null;
  });

  const naiveThemeOverrides = computed(() => {
    return isDark.value ? darkThemeOverrides : lightThemeOverrides;
  });

  function toggleTheme() {
    mode.value = isDark.value ? "light" : "dark";
    localStorage.setItem(STORAGE_KEY, mode.value);
  }

  function setTheme(m: ThemeMode) {
    mode.value = m;
    localStorage.setItem(STORAGE_KEY, m);
  }

  return {
    mode,
    isDark,
    naiveTheme,
    naiveThemeOverrides,
    toggleTheme,
    setTheme,
  };
}
