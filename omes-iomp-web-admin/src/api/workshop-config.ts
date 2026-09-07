import { get, post } from '@/api/request'
import type {
  ScadaUrlDto,
  WorkshopConfigCollectDto,
  WorkshopConfigMeta2dDto,
  WorkshopConfigScadaDto,
  WorkshopRealtimeCollectItem,
} from '@/types/workshop-config'

export function fetchWorkshopCollectConfig(workshopId: string) {
  return get<WorkshopConfigCollectDto>('/workshop/queryConfigCollect', { workshopId })
}

export function saveWorkshopCollectConfig(dto: WorkshopConfigCollectDto) {
  return post<boolean>('/workshop/setConfigCollect', dto)
}

export function fetchWorkshopScadaConfig(workshopId: string) {
  return get<WorkshopConfigScadaDto>('/workshop/queryScadaConfig', { workshopId })
}

export function saveWorkshopScadaConfig(dto: WorkshopConfigScadaDto) {
  return post<boolean>('/workshop/setScadaConfig', dto)
}

export function fetchWorkshopScadaServers() {
  return get<string[]>('/workshop/scadaServer')
}

export function fetchWorkshopScadaUrl(workshopId: string) {
  return get<ScadaUrlDto | null>('/workshop/getScadaUrl', { workshopId })
}

export function fetchWorkshopMeta2dConfig(workshopId: string) {
  return get<WorkshopConfigMeta2dDto>('/workshop/queryMeta2dConfig', { workshopId })
}

export function saveWorkshopMeta2dConfig(dto: WorkshopConfigMeta2dDto) {
  return post<boolean>('/workshop/setMeta2dConfig', dto)
}

export function fetchWorkshopRealtimeCollect(workshopCode: string) {
  return get<WorkshopRealtimeCollectItem[]>('/workshop/getWorkshopRealtimeCollect', { workshopCode })
}
