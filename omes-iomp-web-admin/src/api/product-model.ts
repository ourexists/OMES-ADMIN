import { get, post } from '@/api/request'
import type { EquipConfigDetail } from '@/types/equip-config'
import type { IdsPayload } from '@/types/api'

export interface ProductModelRecord {
  id: string
  productCode?: string
  name?: string
  code?: string
  attrConfig?: EquipConfigDetail
}

export function fetchProductModels(productCode: string) {
  return get<ProductModelRecord[]>('/productModel/listByProductCode', { productCode })
}

export function fetchProductModelById(id: string) {
  return get<ProductModelRecord>('/productModel/selectById', { id })
}

export function saveProductModel(data: Partial<ProductModelRecord>) {
  return post<boolean>('/productModel/addOrUpdate', data)
}

export function deleteProductModels(ids: string[]) {
  return post<boolean>('/productModel/delete', { ids } satisfies IdsPayload)
}
