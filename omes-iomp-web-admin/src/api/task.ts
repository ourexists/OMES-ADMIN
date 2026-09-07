import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'
import type { MapOption } from '@/types/sync'
import type { TaskFormPayload, TaskRecord } from '@/types/task'

export function fetchTaskPage(params: PageQuery) {
  return postPage<TaskRecord>('/task/selectByPage', params)
}

export function fetchTaskById(id: string) {
  return get<TaskRecord>('/task/selectById', { id })
}

export function fetchTimerTaskTypes() {
  return get<MapOption[]>('/task/timerTask')
}

export function saveTask(data: TaskFormPayload) {
  return post<boolean>('/task/addOrUpdate', data)
}

export function deleteTasks(ids: string[]) {
  return post<boolean>('/task/delete', { ids } satisfies IdsPayload)
}

export function startTask(id: string) {
  return get<boolean>('/task/start', { id })
}

export function stopTask(id: string) {
  return get<boolean>('/task/stop', { id })
}
