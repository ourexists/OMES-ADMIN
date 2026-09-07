import { post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface MaterialClassifyRecord {
  id: string
  name?: string
  selfCode?: string
  code?: string
  pcode?: string
}

export interface MaterialRecord {
  id: string
  name?: string
  selfCode?: string
  classifyCode?: string
}

export function fetchMaterialClassifyList(params?: PageQuery & { name?: string; selfCode?: string }) {
  return postPage<MaterialClassifyRecord>('/mc/selectByPage', {
    page: 1,
    pageSize: 500,
    requirePage: false,
    ...params,
  }).then((res) => res.records || [])
}

export function fetchMaterialPage(
  params: PageQuery & { name?: string; selfCode?: string; classifyCode?: string },
) {
  return postPage<MaterialRecord>('/mat/selectByPage', params)
}

export function saveMaterialClassify(data: Partial<MaterialClassifyRecord>) {
  return post<boolean>('/mc/addOrUpdate', data)
}

export function saveMaterial(data: Partial<MaterialRecord>) {
  return post<boolean>('/mat/addOrUpdate', data)
}

export function deleteMaterialClassifies(ids: string[]) {
  return post<boolean>('/mc/delete', { ids } satisfies IdsPayload)
}

export function deleteMaterials(ids: string[]) {
  return post<boolean>('/mat/delete', { ids } satisfies IdsPayload)
}
