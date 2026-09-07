import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { STORAGE_KEYS } from '@/config'
import { applyThemeVariant, readStoredThemeVariant } from '@/theme/apply'
import { classicAntdThemeConfig, classicOmesTokens } from '@/theme/classic'
import {
  buildAntdThemeConfig,
  buildChartPalette,
  buildOmesThemeTokens,
} from '@/theme/tokens'
import { activeMicrosoftTheme } from '@/theme/fluent'
import { THEME_VARIANT_IDS, type ThemeVariantId } from '@/theme/variants'
import { setItem } from '@/utils/storage'

export const useThemeStore = defineStore('theme', () => {
  const variant = ref<ThemeVariantId>(readStoredThemeVariant())

  const isFluent = computed(() => variant.value === 'fluent')
  const menuTheme = computed(() => (isFluent.value ? 'light' : 'dark'))

  const omesTokens = computed(() =>
    isFluent.value
      ? buildOmesThemeTokens(activeMicrosoftTheme)
      : classicOmesTokens,
  )

  const antdTheme = computed(() =>
    isFluent.value
      ? buildAntdThemeConfig(activeMicrosoftTheme)
      : classicAntdThemeConfig,
  )

  const chartPalette = computed(() => buildChartPalette(omesTokens.value))

  const variantOptions = computed(() =>
    THEME_VARIANT_IDS.map((id) => ({
      value: id,
      i18nKey: id === 'fluent' ? 'layout.themeMicrosoft' : 'layout.themeClassic',
    })),
  )

  function setVariant(id: ThemeVariantId) {
    if (variant.value === id) {
      return
    }
    variant.value = id
    setItem(STORAGE_KEYS.themeVariant, id)
    applyThemeVariant(id)
  }

  function toggleVariant() {
    setVariant(isFluent.value ? 'classic' : 'fluent')
  }

  return {
    variant,
    isFluent,
    menuTheme,
    omesTokens,
    antdTheme,
    chartPalette,
    variantOptions,
    setVariant,
    toggleVariant,
  }
})
