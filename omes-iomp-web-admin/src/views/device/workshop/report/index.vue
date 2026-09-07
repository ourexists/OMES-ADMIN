<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { type Dayjs } from 'dayjs'
import {
  ApartmentOutlined,
  BarChartOutlined,
  ReloadOutlined } from '@ant-design/icons-vue'
import type { WorkshopNode } from '@/api/device'
import { fetchWorkshopCollectList } from '@/api/workshop-collect'
import { fetchWorkshopCollectConfig } from '@/api/workshop-config'
import type { WorkshopCollectAttr } from '@/types/workshop-config'
import WorkshopTree from '@/components/WorkshopTree.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import { message } from 'ant-design-vue'
import type { TableColumnType } from 'ant-design-vue'

const { t } = useI18n()

const selectedWorkshop = ref<WorkshopNode | null>(null)
const collectAttrs = ref<WorkshopCollectAttr[]>([])
const tableRows = ref<Record<string, unknown>[]>([])
const tableLoading = ref(false)
const configLoading = ref(false)
const searched = ref(false)

const range = ref<[Dayjs, Dayjs] | null>(null)

const workshopLabel = computed(() => selectedWorkshop.value?.name?.trim() || '')
const hasWorkshop = computed(() => Boolean(selectedWorkshop.value?.id))
const hasAttrs = computed(() => collectAttrs.value.some((item) => item.name?.trim()))
const hasRange = computed(() => range.value != null && range.value.length === 2)

const tableColumns = computed<TableColumnType[]>(() => {
  const cols: TableColumnType[] = [
    {
      title: t('workshopReportPage.colTime'),
      dataIndex: 'time',
      key: 'time',
      width: 180,
      fixed: 'left',
    },
  ]
  for (const attr of collectAttrs.value) {
    const name = attr.name?.trim()
    if (!name) {
      continue
    }
    cols.push({
      title: name,
      dataIndex: name,
      key: name,
      ellipsis: true,
      minWidth: 120,
    })
  }
  return cols
})

const tableSummary = computed(() => {
  if (!searched.value || !hasWorkshop.value || !hasRange.value) {
    return ''
  }
  const start = range.value![0].format('YYYY-MM-DD HH:mm:ss')
  const end = range.value![1].format('YYYY-MM-DD HH:mm:ss')
  return t('workshopReportPage.summary', {
    workshop: workshopLabel.value,
    start,
    end,
    count: tableRows.value.length,
  })
})

function formatCollectTime(value: unknown): string {
  if (value == null || value === '') {
    return '-'
  }
  const parsed = dayjs(value as string | number | Date)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : String(value)
}

function mapCollectRows(list: Awaited<ReturnType<typeof fetchWorkshopCollectList>>) {
  return list.map((row, index) => {
    const data = row.data || {}
    const flat: Record<string, unknown> = {
      key: `${row.time ?? index}-${index}`,
      time: formatCollectTime(row.time),
    }
    for (const attr of collectAttrs.value) {
      const name = attr.name?.trim()
      if (!name) {
        continue
      }
      const raw = data[name]
      flat[name] = raw == null || raw === '' ? '-' : String(raw)
    }
    return flat
  })
}

async function loadCollectConfig() {
  const workshopId = selectedWorkshop.value?.id
  if (!workshopId) {
    collectAttrs.value = []
    tableRows.value = []
    searched.value = false
    return
  }
  configLoading.value = true
  try {
    const dto = await fetchWorkshopCollectConfig(workshopId)
    const attrs = dto?.config?.attrs || []
    collectAttrs.value = attrs.filter((item) => item.name?.trim())
    tableRows.value = []
    searched.value = false
    if (!collectAttrs.value.length) {
      message.warning(t('workshopReportPage.noConfig'))
    }
  } finally {
    configLoading.value = false
  }
}

async function loadTable() {
  const workshopId = selectedWorkshop.value?.id
  if (!workshopId) {
    message.warning(t('workshopReportPage.selectWorkshop'))
    return
  }
  if (!hasRange.value) {
    message.warning(t('workshopReportPage.selectTime'))
    return
  }
  if (!hasAttrs.value) {
    message.warning(t('workshopReportPage.noConfig'))
    return
  }
  const [start, end] = range.value!
  if (!end.isAfter(start)) {
    message.warning(t('workshopReportPage.invalidRange'))
    return
  }

  tableLoading.value = true
  searched.value = true
  try {
    const list = await fetchWorkshopCollectList({
      workshopId,
      startDate: start.format('YYYY-MM-DD HH:mm:ss'),
      endDate: end.format('YYYY-MM-DD HH:mm:ss'),
    })
    tableRows.value = mapCollectRows(list)
    if (!list.length) {
      message.info(t('workshopReportPage.noData'))
    }
  } finally {
    tableLoading.value = false
  }
}

function onSearch() {
  void loadTable()
}

function onReset() {
  range.value = null
  tableRows.value = []
  searched.value = false
}

function onWorkshopChange() {
  void loadCollectConfig()
}

watch(selectedWorkshop, () => {
  void loadCollectConfig()
})
</script>

<template>
  <div class="workshop-report-page">
    <a-row :gutter="16" class="report-layout">
      <a-col :xs="24" :lg="5" class="sidebar-col">
        <a-card size="small" class="panel-card sidebar-card">
          <template #title>
            <AdminPanelTitle>
              <template #icon><ApartmentOutlined /></template>
              {{ t('workshopReportPage.workshop') }}
            </AdminPanelTitle>
          </template>
          <div class="sidebar-tree-body">
            <WorkshopTree
              v-model="selectedWorkshop"
              fill
              :auto-select-first="false"
              @change="onWorkshopChange"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="19" class="main-col">
        <a-card size="small" class="panel-card main-card">
          <template #title>
            <AdminPanelTitle icon-class="card-title__icon--cyan">
              <template #icon><BarChartOutlined /></template>
              {{ t('workshopReportPage.title') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="8" wrap>
              <a-tag v-if="workshopLabel" color="processing">{{ workshopLabel }}</a-tag>
              <a-tag v-if="searched && tableRows.length" color="success">
                {{ t('workshopReportPage.rowCount', { count: tableRows.length }) }}
              </a-tag>
            </a-space>
          </template>

          <div class="main-card-inner">
            <p class="page-desc">{{ t('workshopReportPage.subtitle') }}</p>

            <div class="toolbar-strip search-toolbar--compact">
              <a-form layout="inline" class="search-form" @submit.prevent="onSearch">
                <a-form-item name="timeRange">
                  <a-range-picker
                    v-model:value="range"
                    size="small"
                    show-time
                    format="YYYY-MM-DD HH:mm:ss"
                    class="search-range-picker"
                    :placeholder="[
                      t('workshopReportPage.startTime'),
                      t('workshopReportPage.endTime'),
                    ]"
                  />
                </a-form-item>
                <CompactSearchActions
                  :query-title="t('workshopReportPage.query')"
                  :reset-title="t('workshopReportPage.reset')"
                  :loading="tableLoading"
                  :disabled="!hasWorkshop || configLoading"
                  @reset="onReset"
                >
                  <a-tooltip :title="t('workshopReportPage.reload')">
                    <a-button
                      size="small"
                      :disabled="!hasWorkshop || !hasRange || !hasAttrs"
                      @click="onSearch"
                    >
                      <ReloadOutlined />
                    </a-button>
                  </a-tooltip>
                </CompactSearchActions>
              </a-form>
            </div>

            <p v-if="tableSummary" class="table-summary">{{ tableSummary }}</p>

            <TableScrollWrap
              :refresh-keys="[tableRows.length, collectAttrs.length, searched]"
            >
              <template #default="{ scrollY }">
                <a-table
                  row-key="key"
                  size="middle"
                  bordered
                  class="scroll-table report-table"
                  :columns="tableColumns"
                  :data-source="hasWorkshop && hasAttrs ? tableRows : []"
                  :scroll="{ x: 'max-content', y: scrollY }"
                  :pagination="false"
                  :locale="{
                    emptyText: !hasWorkshop
                      ? t('workshopReportPage.selectWorkshop')
                      : !hasAttrs
                        ? t('workshopReportPage.noConfig')
                        : searched
                          ? t('workshopReportPage.noData')
                          : t('workshopReportPage.selectTime'),
                  }"
                />
              </template>
            </TableScrollWrap>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped>
.workshop-report-page {
  height: calc(100vh - 64px - 32px - 48px);
  max-height: calc(100vh - 64px - 32px - 48px);
  overflow: hidden;
}

.report-layout {
  height: 100%;
  min-height: 0;
}

.report-layout :deep(> .ant-col) {
  height: 100%;
  min-width: 0;
}

.sidebar-col,
.main-col {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 12px;
  border: 1px solid var(--omes-color-border);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.panel-card :deep(.ant-card-head) {
  flex-shrink: 0;
  min-height: 52px;
  border-bottom: 1px solid var(--omes-color-border);
  background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
}

.panel-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 14px 16px 16px;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 15px;
}

.title-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--omes-radius-md);
  background: var(--omes-color-primary-bg);
  color: var(--omes-color-primary);
  font-size: 15px;
}

.title-icon--report {
  background: #f6ffed;
  color: var(--omes-color-success);
}

.sidebar-tree-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.main-card-inner {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-desc {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
  flex-shrink: 0;
}

.toolbar-strip {
  flex-shrink: 0;
}

.search-form :deep(.ant-form-item) {
  margin-bottom: 8px;
}

.table-summary {
  margin: 0;
  font-size: 13px;
  color: #475569;
  flex-shrink: 0;
}

.report-table {
  flex: 1;
  min-height: 0;
}
</style>
