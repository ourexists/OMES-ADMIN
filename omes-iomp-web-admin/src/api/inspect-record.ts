import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface InspectRecordItemRecord {
  id?: string
  recordId?: string
  itemId?: string
  itemName?: string
  content?: string
  result?: number
  resultDesc?: string
  ruleScore?: number | null
  score?: number | null
  remark?: string
  photoUrls?: string
}

export interface InspectRecordRecord {
  id: string
  taskId?: string
  equipId?: string
  equipSelfCode?: string
  equipName?: string
  score?: number | null
  recordTime?: string
  createTime?: string
  items?: InspectRecordItemRecord[]
}

export function inspectRecordResultLabel(result?: number): string {
  if (result === 0) {
    return '正常'
  }
  if (result === 1) {
    return '异常'
  }
  return '-'
}

export function fetchInspectRecordPage(
  params: PageQuery & { taskId?: string; equipName?: string; equipId?: string },
) {
  return postPage<InspectRecordRecord>('/inspection/record/selectByPage', params)
}

export function fetchInspectRecordById(id: string) {
  return get<InspectRecordRecord>('/inspection/record/selectById', { id })
}

export function deleteInspectRecords(ids: string[]) {
  return post<boolean>('/inspection/record/delete', { ids } satisfies IdsPayload)
}
