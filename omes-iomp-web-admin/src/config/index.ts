export const APP_CONFIG = {
  systemName: 'OMES ADMIN',
  desc: '面向工业设备全生命周期管理的数字化平台，支持多场景建模、实时数据采集与设备运维管理',
} as const

export const OAUTH_CLIENT = {
  id: 'mes',
  grantType: 'captcha',
  tenantId: 0,
  platform: 'mes-edge',
} as const

export const STORAGE_KEYS = {
  token: 'mes-token',
  userInfo: 'user_info',
  language: 'mes-lang',
  menu: 'menu',
  themeVariant: 'mes-theme-variant',
} as const

export const API_ROUTES = {
  captcha: '/open/captcha',
  captchaSlider: '/open/captchaSlider',
  captchaSliderVerify: '/open/captchaSlider/verify',
  authToken: '/oauth2/token',
  currentUser: '/acc/currentUser',
  permissionTree: '/permission/currentAccPermissionTree',
  frontendConfig: '/open/frontend-config',
} as const

/**
 * 认证/开放接口：不携带 Authorization，不附加 lang 查询参数。
 * OAuth2 走 Security 过滤器链；/open/** 为网关本地开放端点。
 */
export const PUBLIC_API_PATHS = [
  API_ROUTES.authToken,
  API_ROUTES.frontendConfig,
  API_ROUTES.captcha,
] as const
