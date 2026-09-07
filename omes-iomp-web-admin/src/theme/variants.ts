export type ThemeVariantId = 'fluent' | 'classic'

export const DEFAULT_THEME_VARIANT: ThemeVariantId = 'fluent'

export const THEME_VARIANT_IDS: ThemeVariantId[] = ['fluent', 'classic']

export function isThemeVariantId(value: string): value is ThemeVariantId {
  return (THEME_VARIANT_IDS as string[]).includes(value)
}
