import { createI18n } from 'vue-i18n'
import { gatewayApiPath } from '@/config/gateway'
import type { AppLocale } from '@/i18n/locale'
import { persistLocale, resolveInitialLocale, toBackendLang } from '@/i18n/locale'
import { deepMergeMessages, nestPropertyMessages, parseProperties } from '@/i18n/properties'
import enUS from '@/locales/en-US'
import zhCN from '@/locales/zh-CN'

const REMOTE_I18N_PREFIX = '/static/i18n/i18n_'

/** 旧 Thymeleaf 页用的 properties 覆盖层；Vue 管理端文案已在 locales/*.ts，默认不请求后端 */
const REMOTE_I18N_ENABLED = import.meta.env.VITE_ENABLE_REMOTE_I18N === 'true'

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: resolveInitialLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
})

function remoteI18nUrl(locale: AppLocale): string {
  return gatewayApiPath(`${REMOTE_I18N_PREFIX}${toBackendLang(locale)}.properties`)
}

export async function loadRemoteMessages(locale: AppLocale): Promise<void> {
  if (!REMOTE_I18N_ENABLED) {
    return
  }
  try {
    const response = await fetch(remoteI18nUrl(locale), { cache: 'no-cache' })
    if (!response.ok) {
      return
    }
    const text = await response.text()
    const nested = nestPropertyMessages(parseProperties(text))
    const current = (i18n.global.getLocaleMessage(locale) || {}) as Record<string, unknown>
    i18n.global.setLocaleMessage(
      locale,
      deepMergeMessages(current, nested) as typeof zhCN,
    )
  } catch {
    // ignore remote i18n load failures and keep local messages
  }
}

export async function switchAppLocale(locale: AppLocale): Promise<void> {
  if (i18n.global.locale.value === locale) {
    return
  }

  await loadRemoteMessages(locale)
  i18n.global.locale.value = locale
  persistLocale(locale)
  document.documentElement.lang = locale
}

export async function setupI18n(): Promise<void> {
  const locale = i18n.global.locale.value as AppLocale
  persistLocale(locale)
  document.documentElement.lang = locale
  await loadRemoteMessages(locale)
}

export function translateText(key?: string, fallback = ''): string {
  if (!key) {
    return fallback
  }
  const { t, te } = i18n.global
  return te(key) ? t(key) : fallback
}
