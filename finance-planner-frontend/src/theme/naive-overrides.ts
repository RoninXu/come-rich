import type { GlobalThemeOverrides } from "naive-ui";
import { colors, radius, typography } from "./tokens";

const baseCommon = {
  borderRadius: radius.md,
  borderRadiusSmall: radius.sm,
  fontFamily: typography.fontFamily,
  fontSize: typography.fontSizeMd,
  fontSizeMini: typography.fontSizeXs,
  fontSizeTiny: typography.fontSizeSm,
  fontSizeSmall: typography.fontSizeSm,
  fontSizeMedium: typography.fontSizeMd,
  fontSizeLarge: typography.fontSizeLg,
  fontSizeHuge: typography.fontSizeXl,
  heightMedium: "38px",
  heightSmall: "32px",
  heightLarge: "44px",
};

const shared: GlobalThemeOverrides = {
  Card: {
    borderRadius: radius.lg,
    paddingMedium: "20px",
    paddingLarge: "24px",
  },
  Button: {
    borderRadiusMedium: radius.md,
    borderRadiusSmall: radius.sm,
    borderRadiusLarge: radius.md,
    fontWeightStrong: String(typography.fontWeightSemibold),
    heightMedium: "38px",
    heightLarge: "44px",
  },
  Input: {
    borderRadius: radius.md,
    heightMedium: "38px",
    heightLarge: "44px",
  },
  Menu: {
    borderRadius: radius.md,
    itemHeight: "42px",
  },
  Tabs: {
    tabBorderRadius: radius.md,
  },
  Pagination: {
    itemBorderRadius: radius.sm,
  },
  Progress: {
    borderRadius: radius.full,
  },
};

export const lightThemeOverrides: GlobalThemeOverrides = {
  common: {
    ...baseCommon,
    primaryColor: colors.light.primary,
    primaryColorHover: colors.light.primaryHover,
    primaryColorPressed: colors.light.primaryPressed,
    primaryColorSuppl: colors.light.primary,
    successColor: colors.light.success,
    warningColor: colors.light.warning,
    errorColor: colors.light.error,
    infoColor: colors.light.info,
    textColorBase: colors.light.textPrimary,
    textColor1: colors.light.textPrimary,
    textColor2: colors.light.textSecondary,
    textColor3: colors.light.textTertiary,
    bodyColor: colors.light.bgPage,
    cardColor: colors.light.bgCard,
    modalColor: colors.light.bgCard,
    popoverColor: colors.light.bgCard,
    borderColor: colors.light.border,
    dividerColor: colors.light.divider,
    inputColor: colors.light.bgInput,
  },
  ...shared,
};

export const darkThemeOverrides: GlobalThemeOverrides = {
  common: {
    ...baseCommon,
    primaryColor: colors.dark.primary,
    primaryColorHover: colors.dark.primaryHover,
    primaryColorPressed: colors.dark.primaryPressed,
    primaryColorSuppl: colors.dark.primary,
    successColor: colors.dark.success,
    warningColor: colors.dark.warning,
    errorColor: colors.dark.error,
    infoColor: colors.dark.info,
    textColorBase: colors.dark.textPrimary,
    textColor1: colors.dark.textPrimary,
    textColor2: colors.dark.textSecondary,
    textColor3: colors.dark.textTertiary,
    bodyColor: colors.dark.bgPage,
    cardColor: colors.dark.bgCard,
    modalColor: colors.dark.bgCard,
    popoverColor: colors.dark.bgCard,
    borderColor: colors.dark.border,
    dividerColor: colors.dark.divider,
    inputColor: colors.dark.bgInput,
  },
  ...shared,
};
