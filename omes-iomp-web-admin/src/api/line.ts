import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'
import type { LineRecord, TfEdgeRecord, TfRecord } from '@/types/line'

export interface MapOption {
  id: string
  name: string
}

export function fetchLinePage(params: PageQuery & { name?: string; selfCode?: string }) {
  return postPage<LineRecord>('/line/selectByPage', params)
}

export function fetchLineById(id: string) {
  return get<LineRecord>('/line/selectById', { id })
}

export function saveLine(data: Partial<LineRecord>) {
  return post<boolean>('/line/addOrUpdate', data)
}

export function deleteLines(ids: string[]) {
  return post<boolean>('/line/delete', { ids } satisfies IdsPayload)
}

export function fetchLineTypes() {
  return get<MapOption[]>('/line/type')
}

export function downloadLineS7(lineId: string, serverName: string) {
  return get<boolean>('/line/downloadS7', { lineId, serverName })
}

export function fetchPlcServers() {
  return get<MapOption[]>('/gateway/getAll')
}

export function fetchTfByLineId(lineId: string) {
  return get<TfRecord[]>('/tf/selectByLineId', { lineId })
}

export function fetchTfById(id: string) {
  return get<TfRecord>('/tf/selectById', { id })
}

export function saveTf(data: Partial<TfRecord>) {
  return post<boolean>('/tf/addOrUpdate', data)
}

export function deleteTfs(ids: string[]) {
  return post<boolean>('/tf/delete', { ids } satisfies IdsPayload)
}

export function fetchTfEdgesByLineId(lineId: string) {
  return get<TfEdgeRecord[]>('/tfEdge/selectByLineId', { lineId })
}

export function saveTfEdgesByLineId(lineId: string, edges: TfEdgeRecord[]) {
  return post<boolean>('/tfEdge/saveByLineId', { lineId, edges })
}
