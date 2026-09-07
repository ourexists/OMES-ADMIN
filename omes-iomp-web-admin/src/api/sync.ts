import { get, postPage } from '@/api/request'
import type { PageQuery } from '@/types/api'
import type { MapOption, SyncRecord } from '@/types/sync'

export function fetchSyncPage(
  params: PageQuery & {
    syncTx?: string
    status?: string
    createStartDate?: string
    createEndDate?: string
  },
) {
  return postPage<SyncRecord>('/sync/selectByPage', params)
}

export function fetchSyncById(id: string) {
  return get<SyncRecord>('/sync/selectById', { id })
}

export function fetchSyncTxOptions() {
  return get<MapOption[]>('/sync/syncTx')
}

export function fetchSyncStatusOptions() {
  return get<MapOption[]>('/sync/status')
}

export function breakpointSyncProcess(id: string) {
  return get<boolean>('/sync/breakpointProcess', { id })
}
