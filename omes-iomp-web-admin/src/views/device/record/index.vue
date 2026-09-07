<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { type Dayjs } from 'dayjs'
import {
  HistoryOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { TableColumnType, TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import type { EquipStateSegment } from '@/api/equip-detail'
import {
  fetchEquipRecordAlarmPage,
  fetchEquipRecordOnlinePage,
  fetchEquipRecordRunPage,
} from '@/api/equip-detail'
import { fetchEquipAlarmLevels } from '@/api/device'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'

type RecordTab = 'run' | 'online' | 'alarm'

const { t } = useI18n()

const recordTab = ref<RecordTab>('run')
const loading = ref(false)
const queried = ref(false)
const dataSource = ref<EquipStateSegment[]>([])
const alarmLevelMap = ref<Record<number, string>>({})

const searchForm = reactive({
  sn: '',
  state: undefined as string | undefined,
})

const range = ref<[Dayjs, Dayjs]>([
  dayjs().subtract(6, 'day').startOf('day'),
  dayjs().endOf('day'),
])

const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
})

const stateOptions = computed(() => {
  if (recordTab.value === 'run') {
    return [
      { value: '-1', label: t('realtimePage.unknown') },
      { value: '0', label: t('realtimePage.stop') },
      { value: '1', label: t('realtimePage.run') },
    ]
  }
  if (recordTab.value === 'online') {
    return [
      { value: '0', label: t('realtimePage.offline') },
      { value: '1', label: t('realtimePage.online') },
    ]
  }
  return [
    { value: '-1', label: t('realtimePage.unknown') },
    { value: '0', label: t('realtimePage.normal') },
    { value: '1', label: t('realtimePage.alarm') },
  ]
})

const columns = computed<TableColumnType[]>(() => {
  const base: TableColumnType[] = [
    { title: t('equipRecordPage.colSn'), dataIndex: 'sn', key: 'sn', width: 140, ellipsis: true },
    { title: t('equipDetailPage.state'), dataIndex: 'state', key: 'state', width: 96 },
    { title: t('equipDetailPage.startTime'), dataIndex: 'startTime', key: 'startTime', width: 170 },
    { title: t('equipDetailPage.endTime'), dataIndex: 'endTime', key: 'endTime', width: 170 },
  ]

  if (recordTab.value === 'run') {
    return [
      ...base,
      { title: t('equipDetailPage.powerStart'), dataIndex: 'powerStart', key: 'powerStart', width: 100 },
      { title: t('equipDetailPage.powerEnd'), dataIndex: 'powerEnd', key: 'powerEnd', width: 100 },
      { title: t('equipDetailPage.duration'), dataIndex: 'duration', key: 'duration', width: 110 },
      { title: t('equipDetailPage.powerUse'), dataIndex: 'powerUse', key: 'powerUse', width: 100 },
    ]
  }

  if (recordTab.value === 'online') {
    return [
      ...base,
      { title: t('equipDetailPage.duration'), dataIndex: 'duration', key: 'duration', width: 120 },
    ]
  }

  return [
    ...base,
    { title: t('equipDetailPage.duration'), dataIndex: 'duration', key: 'duration', width: 110 },
    { title: t('equipDetailPage.alarmLevel'), dataIndex: 'level', key: 'level', width: 96 },
    { title: t('equipDetailPage.alarmReason'), dataIndex: 'reason', key: 'reason', ellipsis: true, minWidth: 160 },
  ]
})

const tableSummary = computed(() => {
  if (!queried.value) {
    return ''
  }
  const start = range.value[0].format('YYYY-MM-DD HH:mm:ss')
  const end = range.value[1].format('YYYY-MM-DD HH:mm:ss')
  return t('equipRecordPage.summary', { start, end, count: pagination.total })
})

const emptyDescription = computed(() =>
  queried.value ? t('equipRecordPage.noData') : t('equipRecordPage.selectTime'),
)

function formatState(value: unknown): string {
  const state = Number(value)
  if (recordTab.value === 'run') {
    if (state === -1) return t('realtimePage.unknown')
    return state === 1 ? t('realtimePage.run') : t('realtimePage.stop')
  }
  if (recordTab.value === 'online') {
    return state === 1 ? t('realtimePage.online') : t('realtimePage.offline')
  }
  if (state === -1) return t('realtimePage.unknown')
  return state === 1 ? t('realtimePage.alarm') : t('realtimePage.normal')
}

function formatAlarmLevel(value: unknown): string {
  if (value == null || value === '') {
    return '-'
  }
  const level = Number(value)
  if (Number.isNaN(level)) {
    return String(value)
  }
  return alarmLevelMap.value[level] || String(level)
}

function equipStateSegmentRowKey(record: EquipStateSegment): string {
  return [record.sn, record.startTime, record.endTime, record.state, record.level, record.reason]
    .map((v) => (v == null ? '' : String(v)))
    .join('|')
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

async function loadTable() {
  if (!validateRange()) {
    return
  }
  loading.value = true
  queried.value = true
  try {
    const params = {
      sn: searchForm.sn.trim() || undefined,
      state: searchForm.state !== undefined && searchForm.state !== '' ? searchForm.state : undefined,
      startDate: range.value[0].format('YYYY-MM-DD HH:mm:ss'),
      endDate: range.value[1].format('YYYY-MM-DD HH:mm:ss'),
      page: pagination.current,
      pageSize: pagination.pageSize,
      requirePage: true,
    }
    const fetcher =
      recordTab.value === 'run'
        ? fetchEquipRecordRunPage
        : recordTab.value === 'online'
          ? fetchEquipRecordOnlinePage
          : fetchEquipRecordAlarmPage
    const result = await fetcher(params)
    dataSource.value = result?.records || []
    pagination.total = result?.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.current = 1
  void loadTable()
}

function onReset() {
  searchForm.sn = ''
  searchForm.state = undefined
  range.value = [dayjs().subtract(6, 'day').startOf('day'), dayjs().endOf('day')]
  pagination.current = 1
  dataSource.value = []
  pagination.total = 0
  queried.value = false
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 15
  void loadTable()
}

function onTabChange() {
  searchForm.state = undefined
  onSearch()
}

async function loadAlarmLevels() {
  try {
    const list = await fetchEquipAlarmLevels()
    const map: Record<number, string> = {}
    for (const item of list || []) {
      const code = Number(item.id)
      if (!Number.isNaN(code)) {
        map[code] = item.name
      }
    }
    alarmLevelMap.value = map
  } catch {
    alarmLevelMap.value = {}
  }
}

onMounted(() => {
  void loadAlarmLevels()
})
</script>

<template>
  <div class="admin-page equip-record-page">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="t('equipRecordPage.subtitle')">
          <template #icon><HistoryOutlined /></template>
          {{ t('equipRecordPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8" wrap>
          <a-tag v-if="queried && pagination.total" color="processing">
            {{ t('equipRecordPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">

        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" @submit.prevent="onSearch">
            <a-form-item :label="t('equipRecordPage.colSn')">
              <a-input size="small"
                v-model:value="searchForm.sn"
                allow-clear
                :placeholder="t('equipRecordPage.snPlaceholder')"
                class="search-input"
              >
                <template #prefix>
                  <SearchOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item :label="t('equipDetailPage.state')">
              <a-select size="small"
                v-model:value="searchForm.state"
                allow-clear
                :placeholder="t('equipRecordPage.stateAll')"
                class="state-select"
                :options="stateOptions"
              />
            </a-form-item>
            <a-form-item :label="t('equipRecordPage.timeRange')">
              <a-range-picker size="small"
                v-model:value="range"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                :placeholder="[t('equipDetailPage.startTime'), t('equipDetailPage.endTime')]"
                style="width: 360px"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('equipRecordPage.query')"
              :reset-title="t('equipRecordPage.reset')"
              :loading="loading"
              @reset="onReset"
            >
              <a-tooltip :title="t('equipRecordPage.reload')">
                <a-button size="small" :disabled="!queried" :loading="loading" @click="loadTable">
                  <ReloadOutlined />
                </a-button>
              </a-tooltip>
            </CompactSearchActions>
          </a-form>
          <p class="range-hint">{{ t('equipDetailPage.rangeHint') }}</p>
        </div>

        <a-tabs v-model:active-key="recordTab" size="small" class="record-tabs" @change="onTabChange">
          <a-tab-pane key="run" :tab="t('equipRecordPage.tabRun')" />
          <a-tab-pane key="online" :tab="t('equipRecordPage.tabOnline')" />
          <a-tab-pane key="alarm" :tab="t('equipRecordPage.tabAlarm')" />
        </a-tabs>

        <p v-if="tableSummary" class="table-summary">{{ tableSummary }}</p>

        <TableScrollWrap
          :refresh-keys="[dataSource.length, pagination.total, recordTab, queried]"
        >
          <template #default="{ scrollY }">
            <a-table
              size="middle"
              bordered
              class="scroll-table equip-record-table"
              :row-key="equipStateSegmentRowKey"
              :columns="columns"
              :data-source="queried ? dataSource : []"
              :scroll="{ x: 980, y: scrollY }"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
              }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'state'">
                  {{ formatState(record.state) }}
                </template>
                <template v-else-if="column.key === 'level'">
                  {{ formatAlarmLevel(record.level) }}
                </template>
                <template v-else-if="column.key === 'reason'">
                  {{ record.reason?.toString().trim() || '—' }}
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="emptyDescription" />
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.panel-card {
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
}

.panel-card :deep(.ant-card-head) {
  min-height: 48px;
  border-bottom: 1px solid var(--omes-color-border);
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.page-desc {
  margin: 0 0 12px;
  color: var(--omes-color-text-quaternary);
  font-size: 13px;
}

.search-toolbar {
  margin-bottom: 12px;
  padding: 12px 16px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
}

.search-form {
  margin-bottom: 0;
}

.search-input {
  width: 200px;
}

.state-select {
  width: 140px;
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.range-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.record-tabs {
  margin-bottom: 12px;
}

.table-summary {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--omes-color-text-tertiary);
}

.equip-record-table :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-elevated);
  font-weight: 600;
}
</style>
