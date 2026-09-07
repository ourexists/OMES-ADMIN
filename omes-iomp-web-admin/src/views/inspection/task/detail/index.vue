<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import {
  DeleteOutlined,
  EyeOutlined,
  FileTextOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { InspectRecordRecord } from '@/api/inspect-record'
import { deleteInspectRecords, fetchInspectRecordPage } from '@/api/inspect-record'
import type { InspectTaskRecord } from '@/api/inspect-task'
import {
  fetchInspectTaskById,
  inspectTaskStatusLabel,
  restartOverdueInspectTasks,
} from '@/api/inspect-task'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import InspectRecordDetailModal from '@/views/inspection/record/components/InspectRecordDetailModal.vue'
import InspectTaskAssignModal from '../components/InspectTaskAssignModal.vue'
import DetailHeader from './components/DetailHeader.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const taskId = computed(() => String(route.query.id || ''))

const loading = ref(false)
const recordsLoading = ref(false)
const task = ref<InspectTaskRecord | null>(null)
const dataSource = ref<InspectRecordRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  equipName: '',
})

const assignOpen = ref(false)
const recordDetailOpen = ref(false)
const recordDetailId = ref<string | null>(null)
const recordColumns = computed(() => [
  { title: t('inspectRecordPage.colEquip'), dataIndex: 'equipName', key: 'equipName', minWidth: 160, ellipsis: true },
  { title: t('inspectRecordPage.colScore'), key: 'score', width: 88, align: 'center' as const },
  { title: t('inspectRecordPage.colItemCount'), key: 'itemCount', width: 88, align: 'center' as const },
  { title: t('inspectRecordPage.colRecordTime'), dataIndex: 'recordTime', key: 'recordTime', width: 168 },
  { title: t('inspectRecordPage.colCreateTime'), dataIndex: 'createTime', key: 'createTime', width: 168 },
  { title: t('inspectRecordPage.colAction'), key: 'action', width: 120, fixed: 'right' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

const showRecordTableScrollY = computed(
  () => recordsLoading.value || dataSource.value.length > 0,
)

function recordTableScroll(scrollY: number) {
  return {
    x: 900,
    ...(showRecordTableScrollY.value ? { y: scrollY } : {}),
  }
}

const recordPagination = computed(() => {
  if (!pagination.total && !recordsLoading.value) {
    return false
  }
  return {
    current: pagination.current,
    pageSize: pagination.pageSize,
    total: pagination.total,
    showSizeChanger: true,
    showTotal: (total: number) => t('inspectRecordPage.paginationTotal', { total }),
  }
})

const infoItems = computed(() => {
  const data = task.value
  if (!data) {
    return []
  }
  const statusText = data.statusDesc
    ? t(`inspectTaskPage.status.${data.statusDesc}`, data.statusDesc)
    : inspectTaskStatusLabel(data.status)
  return [
    { label: t('inspectTaskPage.colPlanName'), value: data.planName || '-' },
    { label: t('inspectTaskPage.colTemplate'), value: data.templateName || '-' },
    { label: t('inspectTaskPage.colScheduledTime'), value: data.scheduledTime || '-' },
    { label: t('inspectTaskPage.colWorkshop'), value: data.workshopName || data.workshopCode || '-' },
    { label: t('inspectTaskPage.colStatus'), value: statusText },
    { label: t('inspectTaskPage.colExecutor'), value: data.executorName || t('inspectTaskPage.unassigned') },
    { label: t('inspectTaskPage.colActualStart'), value: data.actualStartTime || '-' },
    { label: t('inspectTaskPage.colActualEnd'), value: data.actualEndTime || '-' },
    { label: t('inspectTaskPage.colRemark'), value: data.remark || '-', span: 3 },
  ]
})

function itemCount(record: InspectRecordRecord): number {
  return record.items?.length ?? 0
}

async function loadTask() {
  if (!taskId.value) {
    message.error(t('inspectTaskDetailPage.missingId'))
    return
  }
  loading.value = true
  try {
    task.value = await fetchInspectTaskById(taskId.value)
  } finally {
    loading.value = false
  }
}

async function loadRecords() {
  if (!taskId.value) {
    dataSource.value = []
    pagination.total = 0
    return
  }
  recordsLoading.value = true
  try {
    const result = await fetchInspectRecordPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      taskId: taskId.value,
      equipName: searchForm.equipName.trim() || undefined,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    recordsLoading.value = false
  }
}

async function loadAll() {
  await loadTask()
  pagination.current = 1
  await loadRecords()
}

function onSearchRecords() {
  pagination.current = 1
  loadRecords()
}

function onResetRecords() {
  searchForm.equipName = ''
  onSearchRecords()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadRecords()
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/view/inspect_task_tables')
}

function openAssign() {
  if (!task.value?.id) {
    return
  }
  assignOpen.value = true
}

async function onRestart() {
  if (!task.value?.id) {
    return
  }
  await restartOverdueInspectTasks([task.value.id])
  message.success(t('inspectTaskPage.restartSuccess'))
  await loadAll()
}

function openRecordDetail(record: InspectRecordRecord) {
  recordDetailId.value = record.id
  recordDetailOpen.value = true
}

function confirmDeleteRecords(ids: string[]) {
  Modal.confirm({
    title: t('inspectRecordPage.deleteConfirm'),
    content: t('inspectRecordPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteInspectRecords(ids)
      message.success(t('inspectRecordPage.deleteSuccess'))
      selectedRowKeys.value = []
      await loadRecords()
      await loadTask()
    },
  })
}

watch(
  taskId,
  () => {
    selectedRowKeys.value = []
    searchForm.equipName = ''
    void loadAll()
  },
  { immediate: true },
)
</script>

<template>
  <div class="admin-page inspect-task-detail-page">
    <DetailHeader
      compact
      :task="task"
      :record-count="pagination.total"
      @refresh="loadAll"
      @back="goBack"
      @assign="openAssign"
      @restart="onRestart"
    />

    <div class="detail-body">
      <section v-if="task" class="detail-section detail-section--info">
        <a-descriptions bordered size="small" :column="3" class="task-descriptions">
          <a-descriptions-item
            v-for="item in infoItems"
            :key="item.label"
            :label="item.label"
            :span="item.span || 1"
          >
            {{ item.value }}
          </a-descriptions-item>
        </a-descriptions>
      </section>

      <section class="detail-section detail-section--records">
        <div class="section-head">
          <span class="section-title">
            <FileTextOutlined />
            {{ t('inspectTaskDetailPage.sectionRecords') }}
          </span>
          <span class="section-hint">{{ t('inspectTaskDetailPage.recordsHint') }}</span>
        </div>

        <div class="records-panel">
          <div class="records-search search-toolbar--compact">
            <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearchRecords">
              <a-form-item name="equipName">
                <a-input
                  v-model:value="searchForm.equipName"
                  allow-clear
                  size="small"
                  class="search-input"
                  :placeholder="t('inspectRecordPage.searchEquip')"
                >
                  <template #prefix>
                    <SearchOutlined class="input-prefix-icon" />
                  </template>
                </a-input>
              </a-form-item>
              <CompactSearchActions
                :query-title="t('inspectRecordPage.query')"
                :reset-title="t('inspectRecordPage.reset')"
                @reset="onResetRecords"
              />
            </a-form>
            <a-space v-if="pagination.total || hasSelection" :size="8">
              <a-tag v-if="hasSelection" color="blue">
                {{ t('inspectRecordPage.selectedCount', { count: selectedRowKeys.length }) }}
              </a-tag>
              <a-tag v-if="pagination.total" color="processing">
                {{ t('inspectRecordPage.total', { count: pagination.total }) }}
              </a-tag>
            </a-space>
          </div>

          <div class="records-toolbar">
            <a-space wrap>
              <a-button danger :disabled="!hasSelection" @click="confirmDeleteRecords(selectedRowKeys)">
                <template #icon><DeleteOutlined /></template>
                {{ t('inspectRecordPage.batchDelete') }}
              </a-button>
              <a-button :loading="recordsLoading" @click="loadRecords">
                <template #icon><ReloadOutlined /></template>
                {{ t('inspectRecordPage.refresh') }}
              </a-button>
            </a-space>
          </div>

          <TableScrollWrap
            class="records-table-wrap"
            :min-height="160"
            :refresh-keys="[dataSource, pagination, showRecordTableScrollY]"
          >
            <template #default="{ scrollY }">
              <a-table
                row-key="id"
                size="middle"
                bordered
                class="inspect-module-table scroll-table records-table"
                :class="{ 'records-table--empty': !showRecordTableScrollY }"
                :columns="recordColumns"
                :data-source="dataSource"
                :row-selection="rowSelection"
                :scroll="recordTableScroll(scrollY)"
                :pagination="recordPagination"
                @change="onTableChange"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'equipName'">
                    <span class="name-cell">{{ record.equipName || '—' }}</span>
                  </template>
                  <template v-else-if="column.key === 'score'">
                    {{ record.score != null ? record.score : '-' }}
                  </template>
                  <template v-else-if="column.key === 'itemCount'">
                    {{ itemCount(record) }}
                  </template>
                  <template v-else-if="column.key === 'action'">
                    <a-space wrap size="small" class="action-group">
                      <a-button type="link" size="small" @click="openRecordDetail(record)">
                        <EyeOutlined />
                        {{ t('inspectRecordPage.detail') }}
                      </a-button>
                      <a-button type="link" size="small" danger @click="confirmDeleteRecords([record.id])">
                        <DeleteOutlined />
                        {{ t('inspectRecordPage.delete') }}
                      </a-button>
                    </a-space>
                  </template>
                </template>

                <template #emptyText>
                  <div class="records-empty">
                    <a-empty :description="t('inspectTaskDetailPage.recordsEmpty')" />
                  </div>
                </template>
              </a-table>
            </template>
          </TableScrollWrap>
        </div>
      </section>
    </div>

    <InspectTaskAssignModal
      v-if="task?.id"
      v-model:open="assignOpen"
      :task-ids="[task.id]"
      @success="loadAll"
    />

    <InspectRecordDetailModal v-model:open="recordDetailOpen" :record-id="recordDetailId" />
  </div>
</template>

<style scoped>
.inspect-task-detail-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  height: 100%;
}

.inspect-task-detail-page > :first-child {
  flex-shrink: 0;
}

.detail-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.detail-section {
  flex-shrink: 0;
  background: var(--omes-color-bg-container);
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
}

.detail-section--info {
  padding: 8px 12px 10px;
}

.detail-section--records {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 12px 14px 14px;
  overflow: hidden;
}

.task-descriptions :deep(.ant-descriptions-item-label) {
  width: 96px;
  padding: 6px 10px !important;
  font-size: 12px;
  background: var(--omes-color-bg-muted);
}

.task-descriptions :deep(.ant-descriptions-item-content) {
  padding: 6px 10px !important;
  font-size: 12px;
}

.task-descriptions :deep(.ant-descriptions-row > th),
.task-descriptions :deep(.ant-descriptions-row > td) {
  padding-bottom: 0;
}

.section-head {
  flex-shrink: 0;
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 6px 12px;
  margin-bottom: 10px;
}

.section-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.section-hint {
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
}

.records-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.records-table-wrap {
  flex: 1;
  min-height: 160px;
  padding-bottom: 10px;
}

.records-table-wrap :deep(.scroll-table.ant-table-wrapper) {
  min-height: 0;
}

.records-search {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
  padding: 12px 14px;
  background: var(--omes-color-bg-toolbar-from);
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
}

.search-form {
  margin-bottom: 0;
}

.search-input {
  width: 220px;
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.records-toolbar {
  flex-shrink: 0;
  padding-bottom: 4px;
}

.records-empty {
  padding: 20px 0 8px;
}

.records-table--empty :deep(.ant-table-body) {
  overflow: visible !important;
  max-height: none !important;
}

.records-table--empty :deep(.ant-table-placeholder .ant-table-cell) {
  padding: 0;
  border-bottom: none;
}

.records-table--empty :deep(.ant-table-tbody > tr.ant-table-placeholder:hover > td) {
  background: transparent;
}

.records-table :deep(.ant-table-placeholder .ant-table-cell) {
  padding: 16px 8px;
}

.records-table :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-table-head);
  font-weight: 600;
}

.name-cell {
  font-weight: 500;
}

.action-group :deep(.ant-btn-link) {
  padding-inline: 4px;
}

.action-group :deep(.ant-btn-link .anticon) {
  margin-inline-end: 4px;
  font-size: 13px;
}
</style>
