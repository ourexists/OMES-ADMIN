import { API_ROUTES, OAUTH_CLIENT } from '@/config'
import { gatewayApiPath } from '@/config/gateway'
import { get } from '@/api/request'
import type { ApiResult, LoginPayload, OAuthTokenResult } from '@/types/api'
import type { PermissionNode } from '@/types/permission'
import type { UserInfo } from '@/types/user'

const PUBLIC_AUTH_HEADERS = {
  'x-era-platform': OAUTH_CLIENT.platform,
  'x-route-tenant': String(OAUTH_CLIENT.tenantId),
} as const

async function parsePublicApiResult<T>(response: Response): Promise<T> {
  const data = (await response.json()) as ApiResult<T> & { message?: string }
  if (!response.ok || data.code !== 200) {
    throw new Error(data.msg || data.message || '请求失败')
  }
  return data.data
}

export function fetchCaptchaUrl(uuid: string): string {
  return `${gatewayApiPath(API_ROUTES.captcha)}?uuid=${uuid}`
}

/** 滑块验证码初始化（服务端写入校验码，登录时携带 uuid-code） */
export async function initSliderCaptcha(uuid: string): Promise<void> {
  const url = `${gatewayApiPath(API_ROUTES.captchaSlider)}?uuid=${encodeURIComponent(uuid)}`
  const response = await fetch(url, {
    method: 'GET',
    credentials: 'omit',
    headers: PUBLIC_AUTH_HEADERS,
  })
  await parsePublicApiResult<boolean>(response)
}

/** 滑块拖动完成后校验，返回登录用 captcha code */
export async function verifySliderCaptcha(
  uuid: string,
  offset: number,
  trackWidth: number,
): Promise<string> {
  const body = new URLSearchParams()
  body.set('uuid', uuid)
  body.set('offset', String(offset))
  body.set('trackWidth', String(trackWidth))

  const response = await fetch(gatewayApiPath(API_ROUTES.captchaSliderVerify), {
    method: 'POST',
    credentials: 'omit',
    headers: {
      ...PUBLIC_AUTH_HEADERS,
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
    },
    body,
  })
  return parsePublicApiResult<string>(response)
}

/** 独立 fetch，不携带 Cookie / Authorization，与旧版 global.js auth() 一致 */
export async function login(payload: Omit<LoginPayload, 'client_id' | 'grant_type'>) {
  const body = new URLSearchParams()
  body.set('client_id', OAUTH_CLIENT.id)
  body.set('grant_type', OAUTH_CLIENT.grantType)
  body.set('username', payload.username)
  body.set('password', payload.password)
  body.set('captcha', payload.captcha)

  const response = await fetch(gatewayApiPath(API_ROUTES.authToken), {
    method: 'POST',
    credentials: 'omit',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      'x-era-platform': OAUTH_CLIENT.platform,
      'x-route-tenant': String(OAUTH_CLIENT.tenantId),
    },
    body,
  })

  const data = (await response.json()) as OAuthTokenResult & {
    code?: number
    error?: string
    error_description?: string
    msg?: string
  }

  if (data.access_token) {
    return data
  }

  const message = data.error_description || data.msg || data.error || '登录失败'
  throw new Error(message)
}

export function fetchCurrentUser() {
  return get<UserInfo>(API_ROUTES.currentUser)
}

export function fetchPermissionTree() {
  return get<PermissionNode[]>(API_ROUTES.permissionTree)
}
