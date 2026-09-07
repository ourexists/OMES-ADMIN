import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface BomClassifyNode {
  id: string
  name?: string
  selfCode: string
  code?: string
  pcode?: string
  description?: string
  children?: BomClassifyNode[]
}

export interface BomTypeOption {
  id: string
  name: string
}

export interface BomDetailRecord {
  id?: string
  tmp_id?: string
  matId?: string
  matName?: string
  matCode?: string
  matScale?: number | string
  mcode?: string
  attribute?: number
  attributeDesc?: string
}

export interface BomRecord {
  id: string
  name?: string
  selfCode?: string
  classifyCode?: string
  type?: number
  typeDesc?: string
  details?: BomDetailRecord[]
}

export function fetchBomClassifyTree() {
  return get<BomClassifyNode[]>('/BOMC/treeClassify')
}

export function fetchBomPage(
  params: PageQuery & {
    name?: string
    selfCode?: string
    classifyCode?: string
    type?: number | string
    detailName?: string
  },
) {
  const body = { ...params }
  if (body.type === '' || body.type == null) {
    delete body.type
  } else if (typeof body.type === 'string') {
    body.type = Number(body.type)
  }
  return postPage<BomRecord>('/BOM/selectByPage', body)
}

export function fetchBomById(id: string) {
  return get<BomRecord>('/BOM/selectById', { id })
}

export function fetchBomTypes() {
  return get<BomTypeOption[]>('/BOM/type')
}

export function saveBomClassify(data: Partial<BomClassifyNode>) {
  return post<boolean>('/BOMC/addOrUpdate', data)
}

export function saveBom(data: Partial<BomRecord>) {
  return post<boolean>('/BOM/addOrUpdate', data)
}

export function deleteBomClassifies(ids: string[]) {
  return post<boolean>('/BOMC/delete', { ids } satisfies IdsPayload)
}

export function deleteBoms(ids: string[]) {
  return post<boolean>('/BOM/delete', { ids } satisfies IdsPayload)
}
