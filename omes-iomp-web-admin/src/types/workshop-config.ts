export interface WorkshopCollectAttr {
  name?: string
  map?: string
  gwId?: string
  value?: string
  unit?: string
  needCollect?: boolean
}

export interface WorkshopConfigCollectDto {
  id?: string
  workshopId: string
  config?: {
    attrs?: WorkshopCollectAttr[]
  }
}

export interface WorkshopScadaConfig {
  server?: string
  privateKey?: string
  privateSecret?: string
  mapCode?: string
  interval?: number
}

export interface WorkshopConfigScadaDto {
  id?: string
  workshopId: string
  scadaConfig?: WorkshopScadaConfig
}

export interface ScadaUrlDto {
  url?: string
  /** Session refresh interval in minutes */
  interval?: number
}

export interface WorkshopMeta2dBinding {
  penId?: string
  prop?: string
  sourceType?: string
  sourceKey?: string
  options?: Record<string, unknown>
}

export interface WorkshopMeta2dConfig {
  canvas?: Record<string, unknown> | null
  bindings?: WorkshopMeta2dBinding[]
  refreshIntervalSec?: number
}

export interface WorkshopConfigMeta2dDto {
  id?: string
  workshopId: string
  meta2dConfig?: WorkshopMeta2dConfig
}

export interface WorkshopRealtimeCollectItem {
  name?: string
  map?: string
  unit?: string
  value?: unknown
}

declare global {
  interface Window {
    Meta2d?: new (id: string) => Meta2dInstance
  }
}

export interface Meta2dInstance {
  open: (data: unknown) => void
  on: (event: string, cb: (pens: { id?: string }[]) => void) => void
  setValue?: (patch: Record<string, unknown>) => void
  data?: () => Record<string, unknown>
  canvas?: { data?: () => Record<string, unknown> }
}
