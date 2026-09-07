/**
 * OMES 管理端主题色 — 由 Fluent UI Theme 派生。
 * CSS 侧通过 styles/tokens.css 提供首屏 fallback；
 * 运行时由 applyTheme() 注入与 Fluent 同步的 --omes-* 变量。
 */
import type { Theme } from '@fluentui/react-theme'
import { microsoftLightTheme } from './fluent2'

export type OmesThemeTokens = ReturnType<typeof buildOmesThemeTokens>
export type OmesThemeTokenKey = keyof OmesThemeTokens

export function buildOmesThemeTokens(f: Theme) {
  return {
    colorPrimary: f.colorBrandBackground,
    colorPrimaryHover: f.colorBrandBackgroundHover,
    colorPrimaryActive: f.colorBrandBackgroundPressed,
    colorPrimaryBg: f.colorBrandBackground2,
    colorPrimaryBgHover: f.colorBrandBackground2Hover,
    colorPrimaryBorder: f.colorBrandStroke2,

    colorBgLayout: f.colorNeutralBackground3,
    colorBgContainer: f.colorNeutralBackground1,
    colorWhite: f.colorNeutralBackground1,
    colorBgElevated: f.colorNeutralBackground2,
    colorBgSpotlight: f.colorNeutralBackground2,
    colorBgMuted: f.colorNeutralBackground3,
    colorBgToolbarFrom: f.colorNeutralBackground2,
    colorBgToolbarTo: f.colorNeutralBackground3,
    colorBgTableHead: f.colorNeutralBackground2,
    colorBgTableHeadAlt: f.colorNeutralBackground3,
    colorBgTableHover: f.colorBrandBackground2,
    colorBgTableHoverAlt: f.colorBrandBackground2Hover,
    colorBgStandalone: f.colorNeutralBackgroundInverted,

    colorBorder: f.colorNeutralStroke3,
    colorBorderSecondary: f.colorNeutralStroke2,
    colorBorderTertiary: f.colorNeutralStroke4,
    colorBorderHover: f.colorNeutralStroke2,

    colorText: f.colorNeutralForeground1,
    colorTextSecondary: f.colorNeutralForeground2,
    colorTextTertiary: f.colorNeutralForeground3,
    colorTextQuaternary: f.colorNeutralForeground4,
    colorTextPlaceholder: f.colorNeutralForeground4,
    colorTextHeading: f.colorNeutralForeground1,
    colorTextLabel: f.colorNeutralForeground2,

    colorSuccess: f.colorStatusSuccessForeground1,
    colorSuccessBg: f.colorStatusSuccessBackground1,
    colorSuccessBorder: f.colorStatusSuccessBorder1,
    colorWarning: f.colorStatusWarningForeground1,
    colorError: f.colorStatusDangerForeground1,
    colorInfo: f.colorBrandBackground,

    colorAccentPurpleFrom: f.colorPalettePurpleBorderActive,
    colorAccentPurpleTo: f.colorPaletteGrapeBorderActive,
    colorAccentOrangeFrom: f.colorPaletteDarkOrangeBorderActive,
    colorAccentOrangeTo: f.colorPalettePumpkinBorderActive,
    colorAccentOrangeLightTo: f.colorPalettePeachBorderActive,
    colorAccentCyanFrom: f.colorPaletteTealBorderActive,
    colorAccentCyanTo: f.colorPaletteLightTealBorderActive,
    colorAccentCyanBg: f.colorPaletteLightTealBackground2,
    colorAccentCyanBorder: f.colorPaletteLightTealBorderActive,

    colorChartPalette0: f.colorBrandBackground,
    colorChartPalette1: f.colorPalettePurpleBorderActive,
    colorChartPalette2: f.colorPaletteTealBorderActive,
    colorChartPalette3: f.colorPaletteMagentaBorderActive,
    colorChartPalette4: f.colorPaletteMarigoldBorderActive,
    colorChartPalette5: f.colorPaletteGreenBorderActive,

    fontFamilyBase: f.fontFamilyBase,
    fontSizeBase: f.fontSizeBase300,
    lineHeightBase: f.lineHeightBase300,
    fontWeightRegular: f.fontWeightRegular,
    fontWeightSemibold: f.fontWeightSemibold,
    borderRadiusSmall: f.borderRadiusSmall,
    borderRadiusMedium: f.borderRadiusMedium,
    borderRadiusLarge: f.borderRadiusLarge,
    borderRadiusXLarge: f.borderRadiusXLarge,
    shadow2: f.shadow2,
    shadow4: f.shadow4,
    shadow8: f.shadow8,
    shadow16: f.shadow16,
    spacingHorizontalM: f.spacingHorizontalM,
    spacingVerticalM: f.spacingVerticalM,
  } as const
}

/** 默认微软风格 token（Fluent 2 / Windows 11） */
export const omesThemeTokens = buildOmesThemeTokens(microsoftLightTheme)

function toCssVarName(key: string): string {
  return key.replace(/[A-Z]/g, (m) => `-${m.toLowerCase()}`)
}

export function themeVar(key: OmesThemeTokenKey): string {
  return `var(--omes-${toCssVarName(key)})`
}

export function applyOmesThemeCssVars(
  tokens: OmesThemeTokens,
  el: HTMLElement = document.documentElement,
): void {
  for (const [key, value] of Object.entries(tokens)) {
    el.style.setProperty(`--omes-${toCssVarName(key)}`, String(value))
  }
}

export function buildAntdThemeConfig(f: Theme) {
  return {
    token: {
      colorPrimary: f.colorBrandBackground,
      colorPrimaryHover: f.colorBrandBackgroundHover,
      colorPrimaryActive: f.colorBrandBackgroundPressed,
      colorSuccess: f.colorStatusSuccessForeground1,
      colorWarning: f.colorStatusWarningForeground1,
      colorError: f.colorStatusDangerForeground1,
      colorInfo: f.colorBrandBackground,
      colorTextBase: f.colorNeutralForeground1,
      colorBgBase: f.colorNeutralBackground1,
      colorBgLayout: f.colorNeutralBackground3,
      colorBgContainer: f.colorNeutralBackground1,
      colorBgElevated: f.colorNeutralBackground2,
      colorText: f.colorNeutralForeground1,
      colorTextSecondary: f.colorNeutralForeground2,
      colorTextTertiary: f.colorNeutralForeground4,
      colorTextQuaternary: f.colorNeutralForeground4,
      colorBorder: f.colorNeutralStroke3,
      colorBorderSecondary: f.colorNeutralStroke2,
      lineWidth: 1,
      lineWidthBold: 1,
      borderRadius: 4,
      borderRadiusLG: 8,
      borderRadiusSM: 4,
      fontFamily: f.fontFamilyBase,
      fontSize: 14,
      lineHeight: 1.4286,
      controlHeight: 32,
      wireframe: false,
      boxShadow: f.shadow2,
      boxShadowSecondary: f.shadow4,
    },
    components: {
      Layout: {
        colorBgHeader: f.colorNeutralBackground1,
        colorBgBody: f.colorNeutralBackground3,
        colorBgTrigger: f.colorNeutralBackground4,
      },
      Menu: {
        colorItemBg: 'transparent',
        colorItemText: f.colorNeutralForeground2,
        colorItemBgHover: f.colorNeutralBackground4,
        colorItemTextHover: f.colorNeutralForeground1,
        colorItemBgSelected: f.colorBrandBackground2,
        colorItemTextSelected: f.colorBrandForeground1,
        colorSubItemBg: 'transparent',
        itemMarginInline: 8,
        radiusItem: 4,
        darkItemBg: 'transparent',
        darkSubMenuItemBg: 'transparent',
        darkItemColor: f.colorNeutralForeground2,
        darkItemHoverBg: f.colorNeutralBackground4,
        darkItemSelectedBg: f.colorBrandBackground2,
        darkItemSelectedColor: f.colorBrandForeground1,
      },
      Table: {
        borderColor: f.colorNeutralStroke2,
        headerSplitColor: f.colorNeutralStroke2,
      },
      Card: {
        headerBg: 'transparent',
        borderRadiusLG: 8,
      },
      Button: {
        borderRadius: 4,
        controlHeight: 32,
      },
      Input: {
        borderRadius: 4,
        controlHeight: 32,
      },
      Select: {
        borderRadius: 4,
        controlHeight: 32,
      },
    },
  }
}

export const antdThemeConfig = buildAntdThemeConfig(microsoftLightTheme)

export function buildChartPalette(tokens: OmesThemeTokens) {
  return [
    tokens.colorChartPalette0,
    tokens.colorChartPalette1,
    tokens.colorChartPalette2,
    tokens.colorChartPalette3,
    tokens.colorChartPalette4,
    tokens.colorChartPalette5,
  ] as const
}

export const chartPalette = buildChartPalette(omesThemeTokens)
