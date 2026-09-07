<script setup lang="ts">
import { computed } from 'vue'
import { BellOutlined, CheckOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import { useMessageNotify } from '@/composables/useMessageNotify'
import type { MessageRecord } from '@/types/message'

const { t } = useI18n()
const { unreadCount, recentMessages, connected, refreshUnreadCount, refreshRecentMessages, readMessage } = useMessageNotify()

const badgeCount = computed(() => (unreadCount.value > 99 ? '99+' : unreadCount.value))

const dropdownItems = computed(() => {
  if (!recentMessages.value.length) {
    return []
  }
  return recentMessages.value.map((item) => ({
    key: item.id,
    label: item,
  }))
})

function typeLabel(type?: number) {
  if (type === 1) {
    return t('messageNotify.typeAlarm')
  }
  return t('messageNotify.typeCommon')
}

function formatTime(value?: string) {
  if (!value) {
    return ''
  }
  return dayjs(value).format('MM-DD HH:mm')
}

async function onVisibleChange(open: boolean) {
  if (open) {
    await Promise.all([refreshUnreadCount(), refreshRecentMessages()])
  }
}

async function onRead(item: MessageRecord, event: Event) {
  event.stopPropagation()
  await readMessage(item)
}
</script>

<template>
  <a-dropdown trigger="click" @open-change="onVisibleChange">
    <a-badge :count="badgeCount" :overflow-count="99" :offset="[-2, 2]">
      <a-button type="text" class="notify-bell" :class="{ 'notify-bell--live': connected }">
        <BellOutlined />
      </a-button>
    </a-badge>
    <template #overlay>
      <div class="notify-panel">
        <div class="notify-panel__header">
          <span>{{ t('messageNotify.title') }}</span>
          <span class="notify-panel__status" :class="{ online: connected }">
            {{ connected ? t('messageNotify.connected') : t('messageNotify.disconnected') }}
          </span>
        </div>
        <a-empty v-if="!dropdownItems.length" :description="t('messageNotify.empty')" />
        <a-list v-else :data-source="recentMessages" size="small" class="notify-panel__list">
          <template #renderItem="{ item }">
            <a-list-item class="notify-item" :class="{ unread: item.readStatus !== 1 }">
              <div class="notify-item__main">
                <div class="notify-item__title-row">
                  <a-tag :color="item.type === 1 ? 'error' : 'blue'" class="notify-item__tag">
                    {{ typeLabel(item.type) }}
                  </a-tag>
                  <span class="notify-item__title">{{ item.title || t('messageNotify.untitled') }}</span>
                  <span class="notify-item__time">{{ formatTime(item.createdTime) }}</span>
                </div>
                <div class="notify-item__content">{{ item.context }}</div>
              </div>
              <a-button
                v-if="item.readStatus !== 1"
                type="link"
                size="small"
                class="notify-item__read"
                @click="onRead(item, $event)"
              >
                <CheckOutlined />
                {{ t('messageNotify.markRead') }}
              </a-button>
            </a-list-item>
          </template>
        </a-list>
      </div>
    </template>
  </a-dropdown>
</template>

<style scoped>
.notify-bell {
  font-size: 18px;
  color: var(--omes-color-text);
}

.notify-bell--live {
  color: var(--omes-color-primary);
}

.notify-panel {
  width: 360px;
  max-height: 420px;
  overflow: hidden;
  background: var(--omes-color-bg-container);
  border-radius: var(--omes-border-radius-large);
  box-shadow: var(--omes-fluent-surface-shadow, 0 8px 24px rgba(0, 0, 0, 0.12));
}

.notify-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--omes-color-border);
  font-weight: var(--omes-font-weight-semibold);
}

.notify-panel__status {
  font-size: 12px;
  color: var(--omes-color-text-secondary);
}

.notify-panel__status.online {
  color: var(--omes-color-success);
}

.notify-panel__list {
  max-height: 340px;
  overflow-y: auto;
}

.notify-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 16px !important;
}

.notify-item.unread {
  background: color-mix(in srgb, var(--omes-color-primary) 6%, transparent);
}

.notify-item__main {
  flex: 1;
  min-width: 0;
}

.notify-item__title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.notify-item__tag {
  margin: 0;
  line-height: 18px;
}

.notify-item__title {
  flex: 1;
  font-weight: var(--omes-font-weight-semibold);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notify-item__time {
  font-size: 12px;
  color: var(--omes-color-text-secondary);
  flex-shrink: 0;
}

.notify-item__content {
  font-size: 13px;
  color: var(--omes-color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notify-item__read {
  flex-shrink: 0;
  padding-inline: 0;
}
</style>
