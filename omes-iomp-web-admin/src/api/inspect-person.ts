import { get, post, postPage } from '@/api/request'
import type { IdsPayload, PageQuery } from '@/types/api'

export interface InspectPersonRecord {
  id: string
  name?: string
  accountId?: string | null
  accountName?: string
  mobile?: string
  jobNumber?: string
  remark?: string
  createdTime?: string
  updatedTime?: string
}

export interface InspectPersonSavePayload {
  person: Partial<InspectPersonRecord>
  syncAccount?: boolean
  accountId?: string | null
  accountInfo?: {
    accName: string
    password: string
    nickName?: string
    accRole?: string
  }
}

export function fetchInspectPersonPage(params: PageQuery & { name?: string; jobNumber?: string }) {
  return postPage<InspectPersonRecord>('/inspection/person/selectByPage', params)
}

export function fetchInspectPersonById(id: string) {
  return get<InspectPersonRecord>('/inspection/person/selectById', { id })
}

export function saveInspectPerson(data: InspectPersonSavePayload) {
  return post<boolean>('/inspection/person/addOrUpdate', data)
}

export function deleteInspectPersons(ids: string[]) {
  return post<boolean>('/inspection/person/delete', { ids } satisfies IdsPayload)
}
