import { STORAGE_KEYS } from '@/config'
import { getItem, setItem } from '@/utils/storage'

export type AppLocale = 'zh-CN' | 'en-US'

export const LOCALE_OPTIONS: { value: AppLocale; label: string }[] = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' },
]

const LOCALE_TO_BACKEND: Record<AppLocale, string> = {
  'zh-CN': 'zh',
  'en-US': 'en',
}

const BACKEND_TO_LOCALE: Record<string, AppLocale> = {
  zh: 'zh-CN',
  en: 'en-US',
}

export function toBackendLang(locale: AppLocale): string {
  return LOCALE_TO_BACKEND[locale]
}

export function resolveInitialLocale(): AppLocale {
  const stored = getItem(STORAGE_KEYS.language)
  if (stored && BACKEND_TO_LOCALE[stored]) {
    return BACKEND_TO_LOCALE[stored]
  }

  const browser = navigator.language.toLowerCase()
  if (browser.startsWith('en')) {
    return 'en-US'
  }
  return 'zh-CN'
}

export function persistLocale(locale: AppLocale): void {
  setItem(STORAGE_KEYS.language, toBackendLang(locale))
}
