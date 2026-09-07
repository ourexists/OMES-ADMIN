import { get, post } from '@/api/request'
import type { SystemConfigRecord } from '@/types/system-config'

export function fetchSystemConfig() {
  return get<SystemConfigRecord>('/systemConfig/get')
}

export function saveSystemConfig(data: SystemConfigRecord) {
  return post<boolean>('/systemConfig/save', data)
}
