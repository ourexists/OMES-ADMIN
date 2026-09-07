/**
 * Fluent 2 / Windows 11 视觉规范主题。
 * 基于 createLightTheme + Win11 强调色 #0078D4 与 Mica 层次表面。
 */
import { createLightTheme } from '@fluentui/react-theme'
import type { BrandVariants, Theme } from '@fluentui/react-theme'

/** Windows 11 默认强调色品牌色阶 */
export const brandWindows11: BrandVariants = {
  10: '#001520',
  20: '#002639',
  30: '#003653',
  40: '#004578',
  50: '#005494',
  60: '#0063b1',
  70: '#0072ce',
  80: '#0078d4',
  90: '#429ce3',
  100: '#68aee9',
  110: '#83bfef',
  120: '#9fd0f5',
  130: '#b8def9',
  140: '#d0ecfc',
  150: '#e4f5fd',
  160: '#f0f9ff',
}

const FLUENT2_FONT =
  '"Segoe UI Variable", "Segoe UI Variable Text", "Segoe UI", -apple-system, BlinkMacSystemFont, system-ui, sans-serif'

const base = createLightTheme(brandWindows11)

/** Fluent 2 浅色主题（Windows 11 壳层 + 控件圆角） */
export const fluent2LightTheme: Theme = {
  ...base,
  fontFamilyBase: FLUENT2_FONT,
  borderRadiusSmall: '4px',
  borderRadiusMedium: '4px',
  borderRadiusLarge: '8px',
  borderRadiusXLarge: '8px',
  colorNeutralBackground2: '#fafafa',
  colorNeutralBackground3: '#f3f3f3',
  colorNeutralBackground4: '#eeeeee',
  shadow2: '0 1px 2px rgba(0, 0, 0, 0.04)',
  shadow4: '0 2px 4px rgba(0, 0, 0, 0.06)',
  shadow8: '0 4px 12px rgba(0, 0, 0, 0.08)',
  shadow16: '0 8px 24px rgba(0, 0, 0, 0.1)',
}

/** 微软风格默认使用的 Fluent 主题 */
export const microsoftLightTheme = fluent2LightTheme
