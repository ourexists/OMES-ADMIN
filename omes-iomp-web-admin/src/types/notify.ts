import type { PageQuery } from '@/types/api'

export const NOTIFY_STATUS = {
  READY: 0,
  PROGRESS: 1,
  COMPLETED: 2,
} as const

export const MESSAGE_TYPE = {
  COMMON: 0,
  ALARM: 1,
} as const

export interface NotifyRecord {
  id: string
  title?: string
  context?: string
  type?: number
  typeDesc?: string
  status?: number
  statusDesc?: string
  step?: number
  source?: string
  sourceId?: string
  platforms?: string[]
  createdTime?: string
}

export interface NotifyPageQuery extends PageQuery {
  type?: number
  status?: number
  platform?: string
}

export interface NotifyFormPayload {
  id?: string
  title: string
  context?: string
  type: number
  step: number
  source?: string
  sourceId?: string
  platforms: string[]
}
