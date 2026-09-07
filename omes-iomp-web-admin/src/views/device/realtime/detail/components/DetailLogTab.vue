<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import type { TableColumnType } from 'ant-design-vue'
import type { EquipRecord } from '@/api/device'
import {
  fetchEquipRecordAlarmPage,
  fetchEquipRecordOnlinePage,
  fetchEquipRecordRunPage,
} from '@/api/equip-detail'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'

const props = defineProps<{
  equip: EquipRecord | null
  active: boolean
}>()

const { t } = useI18n()

const logTab = ref<'run' | 'online' | 'alarm'>('run')
const loading = ref(false)
const records = ref<Record<string, unknown>[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(15)

const range = ref<[Dayjs, Dayjs]>([
  dayjs().subtract(6, 'day').startOf('day'),
  dayjs().endOf('day'),
])

const defaultRange = (): [Dayjs, Dayjs] => [
  dayjs().subtract(6, 'day').startOf('day'),
  dayjs().endOf('day'),
]

const columns = computed<TableColumnType[]>(() => {
  if (logTab.value === 'run') {
    return [
      { title: t('equipDetailPage.startTime'), dataIndex: 'startTime', key: 'startTime', width: 170 },
      { title: t('equipDetailPage.endTime'), dataIndex: 'endTime', key: 'endTime', width: 170 },
      { title: t('equipDetailPage.state'), dataIndex: 'state', key: 'state', width: 90 },
      { title: t('equipDetailPage.powerStart'), dataIndex: 'powerStart', key: 'powerStart', width: 100 },
      { title: t('equipDetailPage.powerEnd'), dataIndex: 'powerEnd', key: 'powerEnd', width: 100 },
      { title: t('equipDetailPage.duration'), dataIndex: 'duration', key: 'duration', width: 110 },
      { title: t('equipDetailPage.powerUse'), dataIndex: 'powerUse', key: 'powerUse', width: 100 },
    ]
  }
  if (logTab.value === 'online') {
    return [
      { title: t('equipDetailPage.startTime'), dataIndex: 'startTime', key: 'startTime', width: 170 },
      { title: t('equipDetailPage.endTime'), dataIndex: 'endTime', key: 'endTime', width: 170 },
      { title: t('equipDetailPage.state'), dataIndex: 'state', key: 'state', width: 90 },
      { title: t('equipDetailPage.duration'), dataIndex: 'duration', key: 'duration', width: 120 },
    ]
  }
  return [
    { title: t('equipDetailPage.startTime'), dataIndex: 'startTime', key: 'startTime', width: 160 },
    { title: t('equipDetailPage.endTime'), dataIndex: 'endTime', key: 'endTime', width: 160 },
    { title: t('equipDetailPage.state'), dataIndex: 'state', key: 'state', width: 88 },
    { title: t('equipDetailPage.duration'), dataIndex: 'duration', key: 'duration', width: 110 },
    { title: t('equipDetailPage.alarmLevel'), dataIndex: 'level', key: 'level', width: 90 },
    { title: t('equipDetailPage.alarmReason'), dataIndex: 'reason', key: 'reason', ellipsis: true },
  ]
})

function formatState(value: unknown): string {
  const state = Number(value)
  if (logTab.value === 'run') {
    if (state === -1) return t('realtimePage.unknown')
    return state === 1 ? t('realtimePage.run') : t('realtimePage.stop')
  }
  if (logTab.value === 'online') {
    return state === 1 ? t('realtimePage.online') : t('realtimePage.offline')
  }
  if (state === -1) return t('realtimePage.unknown')
  return state === 1 ? t('realtimePage.alarm') : t('realtimePage.normal')
}

function validateRange(): boolean {
  const span = range.value[1].valueOf() - range.value[0].valueOf()
  if (span <= 0) {
    message.warning(t('equipDetailPage.invalidRange'))
    return false
  }
  if (span > 30 * 24 * 60 * 60 * 1000) {
    message.warning(t('equipDetailPage.rangeLimit'))
    return false
  }
  return true
}

async function loadLogs() {
  if (!props.active || !props.equip?.selfCode) {
    records.value = []
    total.value = 0
    return
  }
  if (!validateRange()) {
    return
  }
  loading.value = true
  try {
    const params = {
      sn: props.equip.selfCode,
      startDate: range.value[0].format('YYYY-MM-DD HH:mm:ss'),
      endDate: range.value[1].format('YYYY-MM-DD HH:mm:ss'),
      page: page.value,
      pageSize: pageSize.value,
      requirePage: true,
    }
    const fetcher =
      logTab.value === 'run'
        ? fetchEquipRecordRunPage
        : logTab.value === 'online'
          ? fetchEquipRecordOnlinePage
          : fetchEquipRecordAlarmPage
    const result = await fetcher(params)
    records.value = (result?.records || []) as Record<string, unknown>[]
    total.value = result?.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  void loadLogs()
}

function onResetRange() {
  range.value = defaultRange()
  page.value = 1
  void loadLogs()
}

function onTableChange(pagination: { current?: number; pageSize?: number }) {
  page.value = pagination.current || 1
  pageSize.value = pagination.pageSize || 15
  void loadLogs()
}

function logRecordRowKey(record: Record<string, unknown>): string {
  if (record.id != null) return String(record.id)
  return [record.startTime, record.endTime, record.state, record.level, record.reason]
    .map((v) => (v == null ? '' : String(v)))
    .join('|')
}

watch(
  () => [props.active, props.equip?.selfCode, logTab.value] as const,
  ([active]) => {
    if (active) {
      page.value = 1
      void loadLogs()
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="log-tab">
    <div class="toolbar search-toolbar--compact">
      <a-form layout="inline" class="search-form" @finish="onSearch">
        <a-form-item>
          <a-range-picker
            v-model:value="range"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            size="small"
            class="search-range-picker"
          />
        </a-form-item>
        <CompactSearchActions
          :query-title="t('equipDetailPage.query')"
          :reset-title="t('equipRecordPage.reset')"
          :loading="loading"
          @reset="onResetRange"
        />
      </a-form>
      <span class="hint">{{ t('equipDetailPage.rangeHint') }}</span>
    </div>

    <a-tabs v-model:active-key="logTab" size="small" class="log-tabs" @change="onSearch">
      <a-tab-pane key="run" :tab="t('realtimePage.run')" />
      <a-tab-pane key="online" :tab="t('realtimePage.online')" />
      <a-tab-pane key="alarm" :tab="t('realtimePage.alarm')" />
    </a-tabs>

    <TableScrollWrap :refresh-keys="[records.length, total, logTab]">
      <template #default="{ scrollY }">
        <a-table
          size="small"
          bordered
          class="scroll-table log-table"
          :row-key="logRecordRowKey"
          :columns="columns"
          :data-source="records"
          :pagination="{ current: page, pageSize, total, showSizeChanger: true }"
          :scroll="{ x: 900, y: scrollY }"
          @change="onTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'state'">
              {{ formatState(record.state) }}
            </template>
          </template>
        </a-table>
      </template>
    </TableScrollWrap>
  </div>
</template>

<style scoped>
.log-tab {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
}

.log-tabs {
  flex-shrink: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 12px 16px;
  background: var(--omes-color-bg-container);
  border: 1px solid #e8eef5;
  border-radius: 12px;
}

.hint {
  font-size: 12px;
  color: #94a3b8;
}
</style>
