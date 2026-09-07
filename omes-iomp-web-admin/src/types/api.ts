export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T
  pagination?: Pagination
}

export interface Pagination {
  total: number
  page?: number
  pageSize?: number
  pages?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  current?: number
  pageSize?: number
}

export interface PageQuery {
  page?: number
  pageSize?: number
  requirePage?: boolean
  [key: string]: unknown
}

export interface OAuthTokenResult {
  access_token: string
  token_type: string
  expires_in: number
  refresh_token?: string
}

export interface LoginPayload {
  client_id: string
  username: string
  password: string
  captcha: string
  grant_type: string
}

export interface IdsPayload {
  ids: string[]
}

export interface IdPayload {
  id: string
}

export interface AssignPayload {
  id: string
  permissionIds: string[]
}

export interface BindAccPayload {
  accId: string
  roleIds: string[]
}
