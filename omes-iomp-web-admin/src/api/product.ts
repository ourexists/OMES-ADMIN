import request, { get, post, postPage } from '@/api/request'
import { gatewayApiPath } from '@/config/gateway'
import type { ProductAttrConfig } from '@/types/equip-config'
import type { IdsPayload } from '@/types/api'
import type { PageQuery } from '@/types/api'

export interface ProductRecord {
  id: string
  name?: string
  code?: string
  imageUrl?: string
  attrConfig?: ProductAttrConfig
}

const FILE_URL_PREFIX = '/files/'

export function resolveProductImageUrl(path?: string): string {
  if (!path?.trim()) {
    return ''
  }
  const trimmed = path.trim()
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    return trimmed
  }
  if (trimmed.startsWith('/')) {
    return gatewayApiPath(trimmed)
  }
  return gatewayApiPath(`${FILE_URL_PREFIX}${trimmed.replace(/^\/+/, '')}`)
}

export function fetchProductPage(params: PageQuery & { name?: string; code?: string }) {
  return postPage<ProductRecord>('/product/selectByPage', params)
}

export function fetchProductById(id: string) {
  return get<ProductRecord>('/product/selectById', { id })
}

export function fetchProductByCode(code: string) {
  return get<ProductRecord>('/product/selectByCode', { code })
}

export function saveProduct(data: Partial<ProductRecord>) {
  return post<boolean>('/product/addOrUpdate', data)
}

export function deleteProducts(ids: string[]) {
  return post<boolean>('/product/delete', { ids } satisfies IdsPayload)
}

export async function uploadProductImage(file: File, dir = 'product') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('dir', dir)
  const { data } = await request.post<string>('/localFile/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return resolveProductImageUrl(typeof data === 'string' ? data : '')
}
