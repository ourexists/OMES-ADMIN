export interface MessageRecord {
  id: string
  title?: string
  context?: string
  type?: number
  platform?: string
  source?: string
  readStatus?: number
  createdTime?: string
}

export interface MessagePageQuery {
  page?: number
  pageSize?: number
  platform?: string
  type?: number
  limitCurrentUser?: boolean
  readStatus?: number
}

export interface MessagePushEvent {
  event: 'message'
  data: MessageRecord
}
