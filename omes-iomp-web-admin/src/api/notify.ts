import { get, post, postPage } from '@/api/request'
import type { IdsPayload } from '@/types/api'
import type { NotifyFormPayload, NotifyPageQuery, NotifyRecord } from '@/types/notify'

export function fetchNotifyPage(params: NotifyPageQuery) {
  return postPage<NotifyRecord>('/notify/selectByPage', params)
}

export function saveNotify(data: NotifyFormPayload) {
  return post<boolean>('/notify/addOrUpdate', data)
}

export function deleteNotifies(ids: string[]) {
  return post<boolean>('/notify/delete', { ids } satisfies IdsPayload)
}

export function startNotify(id: string) {
  return get<boolean>('/notify/start', { id })
}

export function completeNotify(id: string) {
  return get<boolean>('/notify/complete', { id })
}

export function fetchNotifyStatusMap() {
  return get<Record<string, string>>('/notify/notifyStatus')
}

export function fetchMessageTypeMap() {
  return get<Record<string, string>>('/notify/messageTypes')
}

export function fetchMessageSourceMap() {
  return get<Record<string, string>>('/notify/messageSources')
}
