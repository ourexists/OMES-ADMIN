import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import enUS from 'ant-design-vue/es/locale/en_US'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import type { AppLocale } from '@/i18n/locale'
import { LOCALE_OPTIONS } from '@/i18n/locale'
import { switchAppLocale } from '@/i18n'

export function useLocale() {
  const { locale, t } = useI18n()

  const antdLocale = computed(() => (locale.value === 'zh-CN' ? zhCN : enUS))

  async function setLocale(next: AppLocale) {
    await switchAppLocale(next)
  }

  return {
    locale,
    t,
    antdLocale,
    localeOptions: LOCALE_OPTIONS,
    setLocale,
  }
}
