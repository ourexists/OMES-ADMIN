import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface InspectTaskRecord {
  id: string
  planId?: string
  planName?: string
  templateId?: string
  templateName?: string
  scheduledTime?: string
  workshopCode?: string
  workshopName?: string
  status?: number
  statusDesc?: string
  executorPersonId?: string
  executorId?: string
  executorName?: string
  actualStartTime?: string
  actualEndTime?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export const INSPECT_TASK_STATUS: Record<number, string> = {
  0: '待执行',
  1: '执行中',
  2: '已完成',
  3: '已逾期',
}

export function inspectTaskStatusLabel(status?: number): string {
  if (status == null) {
    return '-'
  }
  return INSPECT_TASK_STATUS[status] || String(status)
}

export function fetchInspectTaskPage(
  params: PageQuery & {
    planId?: string
    planName?: string
    status?: number
    unassigned?: boolean
  },
) {
  return postPage<InspectTaskRecord>('/inspection/task/selectByPage', params)
}

export function fetchInspectTaskById(id: string) {
  return get<InspectTaskRecord>('/inspection/task/selectById', { id })
}

export function deleteInspectTasks(ids: string[]) {
  return post<boolean>('/inspection/task/delete', { ids } satisfies IdsPayload)
}

export function assignInspectTasks(taskIds: string[], personId: string) {
  return post<boolean>('/inspection/task/assign', { taskIds, personId })
}

export function restartOverdueInspectTasks(ids: string[]) {
  return post<boolean>('/inspection/task/restartOverdue', { ids } satisfies IdsPayload)
}

export function fetchInspectTaskStatusTypes() {
  return get<Record<number, string>>('/inspection/task/statusTypes')
}
