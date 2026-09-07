import axios, { type AxiosRequestConfig } from 'axios'
import { message, Modal } from 'ant-design-vue'
import { isPublicApiPath, shouldAppendLang } from '@/config/api-path'
import { OAUTH_CLIENT, STORAGE_KEYS } from '@/config'
import { gatewayApiPath } from '@/config/gateway'
import { i18n } from '@/i18n'
import type { ApiResult, PageQuery } from '@/types/api'
import { getItem, removeItem } from '@/utils/storage'

function t(key: string, fallback?: string) {
  const { te } = i18n.global
  return te(key) ? i18n.global.t(key) : fallback || key
}

const request = axios.create({
  timeout: 60000,
})

/** 仅真正未登录 / 会话失效时弹重新登录，避免 403/405 权限或内部密钥错误误伤 */
const SESSION_EXPIRED_CODES = new Set([401, 403])
const SESSION_EXPIRED_HTTP = new Set([401])

let unauthorizedPromptOpen = false

function appendLang(url: string): string {
  const lang = getItem(STORAGE_KEYS.language) || 'zh'
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}lang=${encodeURIComponent(lang)}`
}

function resolveRequestUrl(originalUrl: string): string {
  const resolved = gatewayApiPath(originalUrl)
  return shouldAppendLang(originalUrl) ? appendLang(resolved) : resolved
}

function normalizeAuthorization(token: string): string {
  const raw = token.trim()
  if (!raw) {
    return ''
  }
  if (/^bearer\s+/i.test(raw)) {
    return raw.replace(/^bearer\s+/i, 'Bearer ')
  }
  return `Bearer ${raw}`
}

function buildHeaders(contentType?: string, withAuth = true): Record<string, string> {
  const headers: Record<string, string> = {
    'x-era-platform': OAUTH_CLIENT.platform,
    'x-route-tenant': String(OAUTH_CLIENT.tenantId),
  }
  if (withAuth) {
    const token = getItem(STORAGE_KEYS.token)
    if (token) {
      headers.Authorization = normalizeAuthorization(token)
    }
  }
  if (contentType) {
    headers['Content-Type'] = contentType
  }
  return headers
}

function normalizeIds(data: unknown): void {
  if (Array.isArray(data)) {
    data.forEach((item) => {
      if (item && typeof item === 'object' && 'id' in item && typeof item.id !== 'string') {
        item.id = item.id == null ? '' : String(item.id)
      }
    })
  }
}

function isLoginPage(): boolean {
  return window.location.pathname.startsWith('/login')
}

function isSessionExpiredMessage(msg?: string): boolean {
  if (!msg) {
    return false
  }
  const text = msg.toLowerCase()
  return (
    text.includes('未登录') ||
    text.includes('unauthorized') ||
    text.includes('invalid bearer') ||
    (text.includes('token') && (text.includes('expired') || text.includes('invalid')))
  )
}

function handleUnauthorized(): void {
  removeItem(STORAGE_KEYS.token)
  if (isLoginPage() || unauthorizedPromptOpen) {
    return
  }
  unauthorizedPromptOpen = true
  Modal.confirm({
    title: t('request.expiredTitle'),
    content: t('request.expiredContent'),
    okText: t('request.relogin'),
    cancelText: t('request.cancel'),
    onOk: () => {
      unauthorizedPromptOpen = false
      const redirect = `${window.location.pathname}${window.location.search}`
      window.location.href = `/login?redirect=${encodeURIComponent(redirect)}`
    },
    onCancel: () => {
      unauthorizedPromptOpen = false
    },
  })
}

function extractErrorMessage(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return t('request.networkError')
  }
  const data = error.response?.data
  if (typeof data === 'string' && data.trim()) {
    // Tomcat sendError 可能返回 HTML，尽量抽出短文案
    const plain = data.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
    if (plain.length > 0 && plain.length < 180) {
      return plain
    }
    const statusText = error.response?.statusText
    if (statusText) {
      return statusText
    }
  }
  if (data && typeof data === 'object') {
    const payload = data as Record<string, unknown>
    if (typeof payload.msg === 'string' && payload.msg) {
      return payload.msg
    }
    if (typeof payload.error_description === 'string' && payload.error_description) {
      return payload.error_description
    }
    if (typeof payload.message === 'string' && payload.message) {
      return payload.message
    }
  }
  return error.message || t('request.networkError')
}

request.interceptors.request.use((config) => {
  const originalUrl = config.url || ''
  const isPublic = isPublicApiPath(originalUrl)
  config.url = resolveRequestUrl(originalUrl)
  Object.assign(config.headers, buildHeaders(config.headers?.['Content-Type'] as string | undefined, !isPublic))
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload === 'object' && 'code' in payload) {
      const result = payload as ApiResult
      if (result.code === 200) {
        normalizeIds(result.data)
        if (result.pagination) {
          return {
            ...response,
            data: {
              records: result.data,
              total: result.pagination.total,
              current: result.pagination.page,
              pageSize: result.pagination.pageSize,
            },
          }
        }
        return { ...response, data: result.data }
      }
      // 401 / 业务未登录(403) 才视为会话失效；405 权限拒绝、内部密钥错误只提示
      if (SESSION_EXPIRED_CODES.has(result.code) && isSessionExpiredMessage(result.msg)) {
        handleUnauthorized()
        return Promise.reject(result)
      }
      if (result.code === 401) {
        handleUnauthorized()
        return Promise.reject(result)
      }
      message.error(result.msg || t('request.failed'))
      return Promise.reject(result)
    }
    return response
  },
  (error) => {
    const originalUrl = error.config?.url || ''
    const isPublic = isPublicApiPath(originalUrl)
    const status = error.response?.status
    const errMsg = extractErrorMessage(error)
    if (!isPublic && SESSION_EXPIRED_HTTP.has(status)) {
      handleUnauthorized()
    } else if (!isPublic && status === 403 && isSessionExpiredMessage(errMsg)) {
      handleUnauthorized()
    }
    message.error(errMsg)
    return Promise.reject(error)
  },
)

export async function get<T = unknown>(
  url: string,
  params?: Record<string, unknown>,
  config?: AxiosRequestConfig,
) {
  const { data } = await request.get<T>(url, { params, ...config })
  return data
}

export async function post<T = unknown>(url: string, body?: unknown, config?: AxiosRequestConfig) {
  const { data } = await request.post<T>(url, body, {
    headers: { 'Content-Type': 'application/json' },
    ...config,
  })
  return data
}

export async function postForm<T = unknown>(url: string, payload: Record<string, string | number | boolean>) {
  const body = new URLSearchParams()
  Object.entries(payload).forEach(([key, value]) => {
    body.append(key, String(value))
  })
  const { data } = await request.post<T>(url, body, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
  })
  return data
}

export async function postPage<T>(url: string, body?: PageQuery) {
  const { data } = await request.post<{ records: T[]; total: number; current?: number; pageSize?: number }>(
    url,
    { page: 1, pageSize: 10, ...body },
    { headers: { 'Content-Type': 'application/json' } },
  )
  return data
}

export default request
