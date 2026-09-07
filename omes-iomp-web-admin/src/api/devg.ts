import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'
import type { DevgEquipRecord, DevgRecord } from '@/types/devg'

export function fetchDevgPage(params: PageQuery & { name?: string; selfCode?: string }) {
  return postPage<DevgRecord>('/devg/selectByPage', params)
}

export function saveDevg(data: Partial<DevgRecord>) {
  return post<boolean>('/devg/addOrUpdate', data)
}

export function deleteDevgs(ids: string[]) {
  return post<boolean>('/devg/delete', { ids } satisfies IdsPayload)
}

export function fetchDevgEquips(dgId: string) {
  return get<DevgEquipRecord[]>('/devg/equips', { dgId })
}

export function bindDevgEquips(dgId: string, equipIds: string[]) {
  return post<boolean>('/devg/bindEquips', { dgId, equipIds })
}

export function unbindDevgEquips(dgId: string, equipIds: string[]) {
  return post<boolean>('/devg/unbindEquips', { dgId, equipIds })
}

export function saveDevgEquipProcess(data: {
  dgId: string
  equipId: string
  processMaterials?: { matCode?: string; matName?: string; maxCapacity?: number }[]
}) {
  return post<boolean>('/devg/saveEquipProcess', data)
}
