import { get, postPage } from '@/api/request'
import { OAUTH_CLIENT } from '@/config'
import type { MessagePageQuery, MessageRecord } from '@/types/message'

export function fetchUnreadMessageCount() {
  return get<number>('/message/countUnread')
}

export function fetchMessagePage(params: MessagePageQuery) {
  return postPage<MessageRecord>('/message/selectByPage', {
    platform: OAUTH_CLIENT.platform,
    limitCurrentUser: true,
    ...params,
  })
}

export function markMessageRead(messageId: string) {
  return get<boolean>('/message/read', { messageId })
}
