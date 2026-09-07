import { API_ROUTES, PUBLIC_API_PATHS } from '@/config'

/** 去掉 baseUrl、query，得到用于规则匹配的 pathname */
export function normalizeApiPath(url: string): string {
  try {
    if (/^https?:\/\//i.test(url)) {
      return new URL(url).pathname
    }
  } catch {
    // ignore invalid url
  }
  const path = url.split('?')[0]
  return path.startsWith('/') ? path : `/${path}`
}

/**
 * 认证/开放接口：不走 MVC LocaleChangeInterceptor，且不应携带 Authorization。
 * 与旧版 global.js 中 auth() 行为一致（仅 oauth 参数，不混用业务 lang 规则）。
 */
export function isPublicApiPath(url: string): boolean {
  const path = normalizeApiPath(url)
  return PUBLIC_API_PATHS.some((item) => {
    if (item === API_ROUTES.captcha) {
      return path.startsWith('/open/captcha')
    }
    if (item === API_ROUTES.frontendConfig) {
      return path.startsWith('/open/frontend-config')
    }
    return path === item
  })
}

/** 业务 API 才附加 lang 查询参数（后端 LocaleChangeInterceptor） */
export function shouldAppendLang(url: string): boolean {
  return !isPublicApiPath(url)
}
