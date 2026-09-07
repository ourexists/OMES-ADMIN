/**
 * Fluent UI 主题集成（微软风格 → Fluent 2 / Windows 11）。
 */
import { webLightTheme } from '@fluentui/react-theme'
import type { Theme } from '@fluentui/react-theme'
import { fluent2LightTheme, microsoftLightTheme } from './fluent2'

export { webLightTheme, fluent2LightTheme, microsoftLightTheme }
export type FluentTheme = Theme

/** 微软风格当前使用的 Fluent 2 主题 */
export const activeMicrosoftTheme = microsoftLightTheme

/** 将 Fluent Theme 注入为 --colorXxx CSS 变量（与 FluentProvider 行为一致） */
export function applyFluentCssVars(
  el: HTMLElement = document.documentElement,
  theme: Theme = activeMicrosoftTheme,
): void {
  for (const [key, value] of Object.entries(theme)) {
    if (typeof value === 'string') {
      el.style.setProperty(`--${key}`, value)
    }
  }
}

/** 清除已注入的 Fluent --color* 变量 */
export function clearFluentCssVars(
  el: HTMLElement = document.documentElement,
  theme: Theme = activeMicrosoftTheme,
): void {
  for (const key of Object.keys(theme)) {
    el.style.removeProperty(`--${key}`)
  }
}

/** Fluent CSS 变量引用，供样式文件使用 */
export function fluentVar(token: keyof Theme): string {
  return `var(--${String(token)})`
}
