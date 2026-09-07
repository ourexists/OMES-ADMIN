<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import {
  CarryOutOutlined,
  DeleteOutlined,
  EyeOutlined,
  RedoOutlined,
  ReloadOutlined,
  UserAddOutlined,
} from '@ant-design/icons-vue'
import type { InspectPlanRecord } from '@/api/inspect-plan'
import type { InspectTaskRecord } from '@/api/inspect-task'
import {
  deleteInspectTasks,
  fetchInspectTaskPage,
  fetchInspectTaskStatusTypes,
  inspectTaskStatusLabel,
  restartOverdueInspectTasks,
} from '@/api/inspect-task'
import InspectTaskAssignModal from '@/views/inspection/task/components/InspectTaskAssignModal.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const TASK_STATUS_PENDING = 0
const TASK_STATUS_OVERDUE = 3

const props = defineProps<{
  open: boolean
  plan: InspectPlanRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const dataSource = ref<InspectTaskRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const statusOptions = ref<{ value: number; label: string }[]>([])

const searchForm = reactive({
  status: undefined as number | undefined,
  unassigned: undefined as boolean | undefined,
})

const assignOpen = ref(false)
const assignTaskIds = ref<string[]>([])

const planId = computed(() => props.plan?.id)
const modalTitle = computed(() =>
  t('inspectPlanPage.planTasksTitle', { name: props.plan?.name || props.plan?.id || '-' }),
)

const columns = computed(() => [
  { title: t('inspectTaskPage.colScheduledTime'), dataIndex: 'scheduledTime', key: 'scheduledTime', width: 168 },
  { title: t('inspectTaskPage.colWorkshop'), key: 'workshop', width: 120, ellipsis: true },
  { title: t('inspectTaskPage.colStatus'), key: 'status', width: 88, align: 'center' as const },
  { title: t('inspectTaskPage.colExecutor'), key: 'executorName', width: 100, ellipsis: true },
  { title: t('inspectTaskPage.colActualStart'), dataIndex: 'actualStartTime', key: 'actualStartTime', width: 168 },
  { title: t('inspectTaskPage.colActualEnd'), dataIndex: 'actualEndTime', key: 'actualEndTime', width: 168 },
  { title: t('inspectTaskPage.colRemark'), dataIndex: 'remark', key: 'remark', minWidth: 100, ellipsis: true },
  { title: t('inspectTaskPage.colAction'), key: 'action', width: 200, fixed: 'right' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

const unassignedFilterOptions = computed(() => [
  { value: true, label: t('inspectTaskPage.unassignedOnly') },
])

function statusLabel(record: InspectTaskRecord): string {
  if (record.statusDesc) {
    return t(`inspectTaskPage.status.${record.statusDesc}`, record.statusDesc)
  }
  return inspectTaskStatusLabel(record.status)
}

function statusColor(status?: number): string {
  if (status === 1) {
    return 'processing'
  }
  if (status === 2) {
    return 'success'
  }
  if (status === 3) {
    return 'error'
  }
  return 'default'
}

function workshopLabel(record: InspectTaskRecord): string {
  if (record.workshopName?.trim()) {
    return record.workshopName
  }
  return record.workshopCode || '-'
}

async function loadStatusOptions() {
  const map = await fetchInspectTaskStatusTypes()
  if (map && typeof map === 'object') {
    statusOptions.value = Object.entries(map)
      .map(([key, label]) => ({
        value: Number(key),
        label: t(`inspectTaskPage.status.${label}`, String(label)),
      }))
      .filter((item) => !Number.isNaN(item.value))
      .sort((a, b) => a.value - b.value)
  }
}

async function loadTable() {
  if (!planId.value) {
    dataSource.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const result = await fetchInspectTaskPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      planId: planId.value,
      status: searchForm.status,
      unassigned: searchForm.unassigned,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.current = 1
  loadTable()
}

function onReset() {
  searchForm.status = undefined
  searchForm.unassigned = undefined
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function getSelectedRecords(): InspectTaskRecord[] {
  const idSet = new Set(selectedRowKeys.value)
  return dataSource.value.filter((row) => idSet.has(row.id))
}

function closeModal() {
  emit('update:open', false)
}

function openAssign(taskIds: string[]) {
  if (!taskIds.length) {
    message.warning(t('inspectTaskPage.assignSelectRequired'))
    return
  }
  assignTaskIds.value = taskIds
  assignOpen.value = true
}

function batchAssign() {
  const pending = getSelectedRecords().filter((row) => row.status === TASK_STATUS_PENDING)
  if (!pending.length) {
    message.warning(t('inspectTaskPage.assignPendingOnly'))
    return
  }
  openAssign(pending.map((row) => row.id))
}

function assignOne(record: InspectTaskRecord) {
  if (record.status !== TASK_STATUS_PENDING) {
    message.warning(t('inspectTaskPage.assignPendingOnly'))
    return
  }
  openAssign([record.id])
}

async function restartTasks(ids: string[]) {
  if (!ids.length) {
    message.warning(t('inspectTaskPage.restartSelectRequired'))
    return
  }
  await restartOverdueInspectTasks(ids)
  message.success(t('inspectTaskPage.restartSuccess'))
  selectedRowKeys.value = []
  loadTable()
}

function batchRestart() {
  const overdue = getSelectedRecords().filter((row) => row.status === TASK_STATUS_OVERDUE)
  if (!overdue.length) {
    message.warning(t('inspectTaskPage.restartOverdueOnly'))
    return
  }
  restartTasks(overdue.map((row) => row.id))
}

function restartOne(record: InspectTaskRecord) {
  if (record.status !== TASK_STATUS_OVERDUE) {
    message.warning(t('inspectTaskPage.restartOverdueOnly'))
    return
  }
  restartTasks([record.id])
}

function openDetail(record: InspectTaskRecord) {
  router.push({ path: '/view/inspect_task_detail', query: { id: record.id } })
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('inspectTaskPage.deleteConfirm'),
    content: t('inspectTaskPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteInspectTasks(ids)
      message.success(t('inspectTaskPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

watch(
  () => props.open,
  async (visible) => {
    if (!visible) {
      selectedRowKeys.value = []
      searchForm.status = undefined
      searchForm.unassigned = undefined
      pagination.current = 1
      return
    }
    if (!statusOptions.value.length) {
      await loadStatusOptions()
    }
    await loadTable()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    width="1080px"
    destroy-on-close
    class="inspect-plan-tasks-modal"
    :footer="null"
    @cancel="closeModal"
  >
    <template #title>
      <span class="modal-title">
        <CarryOutOutlined style="margin-right: 8px; color: var(--omes-color-accent-cyan-from)" />
        {{ modalTitle }}
      </span>
    </template>

    <div class="plan-tasks-body">
      <div class="plan-tasks-search search-toolbar--compact">
        <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
          <a-form-item name="status">
            <a-select
              v-model:value="searchForm.status"
              allow-clear
              size="small"
              class="search-select-sm"
              :placeholder="t('inspectTaskPage.searchStatus')"
              :options="statusOptions"
            />
          </a-form-item>
          <a-form-item name="unassigned">
            <a-select
              v-model:value="searchForm.unassigned"
              allow-clear
              size="small"
              class="search-select-sm"
              :placeholder="t('inspectTaskPage.searchAssign')"
              :options="unassignedFilterOptions"
            />
          </a-form-item>
          <CompactSearchActions
            :query-title="t('inspectTaskPage.query')"
            :reset-title="t('inspectTaskPage.reset')"
            @reset="onReset"
          />
        </a-form>
        <a-space v-if="pagination.total || hasSelection" class="plan-tasks-meta" :size="8">
          <a-tag v-if="hasSelection" color="blue">
            {{ t('inspectTaskPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag v-if="pagination.total" color="processing">
            {{ t('inspectTaskPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </div>

      <div class="plan-tasks-toolbar">
        <a-space wrap>
          <a-button type="primary" size="small" @click="batchAssign">
            <template #icon><UserAddOutlined /></template>
            {{ t('inspectTaskPage.batchAssign') }}
          </a-button>
          <a-button size="small" @click="batchRestart">
            <template #icon><RedoOutlined /></template>
            {{ t('inspectTaskPage.batchRestart') }}
          </a-button>
          <a-button danger size="small" :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
            <template #icon><DeleteOutlined /></template>
            {{ t('inspectTaskPage.batchDelete') }}
          </a-button>
          <a-button size="small" :loading="loading" @click="loadTable">
            <template #icon><ReloadOutlined /></template>
            {{ t('inspectTaskPage.refresh') }}
          </a-button>
        </a-space>
      </div>

      <div class="plan-tasks-table-wrap">
        <a-table
          row-key="id"
          size="small"
          bordered
          class="inspect-module-table"
          :columns="columns"
          :data-source="dataSource"
          :row-selection="rowSelection"
          :scroll="{ x: 980, y: 420 }"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            size: 'small',
            showTotal: (total: number) => t('inspectTaskPage.paginationTotal', { total }),
          }"
          @change="onTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'scheduledTime'">
              <span class="time-cell">{{ record.scheduledTime || '—' }}</span>
            </template>
            <template v-else-if="column.key === 'workshop'">
              <span class="muted-cell">{{ workshopLabel(record) }}</span>
            </template>
            <template v-else-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">
                {{ statusLabel(record) }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'executorName'">
              <span :class="record.executorName ? 'name-cell' : 'muted-cell'">
                {{ record.executorName || t('inspectTaskPage.unassigned') }}
              </span>
            </template>
            <template v-else-if="column.key === 'actualStartTime'">
              <span class="time-cell">{{ record.actualStartTime || '—' }}</span>
            </template>
            <template v-else-if="column.key === 'actualEndTime'">
              <span class="time-cell">{{ record.actualEndTime || '—' }}</span>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space wrap size="small" class="action-group">
                <a-button
                  v-if="record.status === TASK_STATUS_PENDING"
                  type="link"
                  size="small"
                  class="action-link--accent"
                  @click="assignOne(record)"
                >
                  <UserAddOutlined />
                  {{ t('inspectTaskPage.assign') }}
                </a-button>
                <a-button
                  v-if="record.status === TASK_STATUS_OVERDUE"
                  type="link"
                  size="small"
                  class="action-link--warn"
                  @click="restartOne(record)"
                >
                  <RedoOutlined />
                  {{ t('inspectTaskPage.restart') }}
                </a-button>
                <a-button type="link" size="small" @click="openDetail(record)">
                  <EyeOutlined />
                  {{ t('inspectTaskPage.detail') }}
                </a-button>
                <a-button type="link" size="small" danger @click="confirmDelete([record.id])">
                  <DeleteOutlined />
                  {{ t('inspectTaskPage.delete') }}
                </a-button>
              </a-space>
            </template>
          </template>

          <template #emptyText>
            <a-empty :description="t('inspectPlanPage.planTasksEmpty')" />
          </template>
        </a-table>
      </div>
    </div>

    <InspectTaskAssignModal v-model:open="assignOpen" :task-ids="assignTaskIds" @success="loadTable" />

  </a-modal>
</template>

<style scoped>
.inspect-plan-tasks-modal :deep(.ant-modal-body) {
  padding: 12px 20px 20px;
}

.plan-tasks-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.plan-tasks-search {
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

.search-form :deep(.ant-form-item) {
  margin-bottom: 0;
}

.search-select-sm {
  width: 132px;
}

.plan-tasks-meta {
  flex-shrink: 0;
}

.plan-tasks-toolbar {
  padding-bottom: 4px;
}

.plan-tasks-table-wrap {
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
  overflow: hidden;
}

.plan-tasks-table-wrap :deep(.ant-table) {
  border-radius: 0;
}

.plan-tasks-table-wrap :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-table-head);
  font-weight: 600;
}

.time-cell {
  font-variant-numeric: tabular-nums;
  font-size: 13px;
}

.name-cell {
  font-weight: 500;
}

.muted-cell {
  color: var(--omes-color-text-tertiary);
  font-size: 13px;
}

.action-group :deep(.ant-btn-link) {
  padding-inline: 4px;
}

.action-group :deep(.ant-btn-link .anticon) {
  margin-inline-end: 4px;
  font-size: 13px;
}
</style>
