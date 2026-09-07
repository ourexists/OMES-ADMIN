import { applyFluentCssVars, clearFluentCssVars, activeMicrosoftTheme } from './fluent'
import { classicOmesTokens } from './classic'
import {
  applyOmesThemeCssVars,
  buildAntdThemeConfig,
  buildOmesThemeTokens,
} from './tokens'
import { DEFAULT_THEME_VARIANT, isThemeVariantId, type ThemeVariantId } from './variants'
import { getItem } from '@/utils/storage'
import { STORAGE_KEYS } from '@/config'

export function resolveThemeVariant(stored?: string | null): ThemeVariantId {
  if (stored && isThemeVariantId(stored)) {
    return stored
  }
  return DEFAULT_THEME_VARIANT
}

export function readStoredThemeVariant(): ThemeVariantId {
  return resolveThemeVariant(getItem(STORAGE_KEYS.themeVariant))
}

/** 应用指定主题变体（微软风格 / 原始 OMES） */
export function applyThemeVariant(
  variant: ThemeVariantId,
  el: HTMLElement = document.documentElement,
): void {
  if (variant === 'fluent') {
    const omes = buildOmesThemeTokens(activeMicrosoftTheme)
    applyFluentCssVars(el, activeMicrosoftTheme)
    applyOmesThemeCssVars(omes, el)
    el.dataset.themeVariant = 'fluent'
    el.classList.add('theme-fluent')
    el.classList.remove('theme-classic')
    return
  }

  clearFluentCssVars(el)
  applyOmesThemeCssVars(classicOmesTokens, el)
  el.dataset.themeVariant = 'classic'
  el.classList.add('theme-classic')
  el.classList.remove('theme-fluent')
}

/** @deprecated 使用 applyThemeVariant */
export function applyTheme(el: HTMLElement = document.documentElement): void {
  applyThemeVariant(DEFAULT_THEME_VARIANT, el)
}

export { buildAntdThemeConfig }
