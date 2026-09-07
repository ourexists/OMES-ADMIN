<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  ClockCircleOutlined,
  DeleteOutlined,
  EditOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import {
  deleteTasks,
  fetchTaskPage,
  startTask,
  stopTask,
} from '@/api/task'
import { TASK_STATUS_RUNNING, TASK_STATUS_STOPPING, type TaskRecord } from '@/types/task'
import TaskFormModal from './components/TaskFormModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()

const loading = ref(false)
const dataSource = ref<TaskRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const formOpen = ref(false)
const editingRecord = ref<TaskRecord | null>(null)

const columns = computed(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 180, ellipsis: true },
  { title: t('taskPage.colName'), dataIndex: 'name', key: 'name', minWidth: 160, ellipsis: true },
  { title: t('taskPage.colType'), dataIndex: 'type', key: 'type', width: 160, ellipsis: true },
  { title: t('taskPage.colCron'), dataIndex: 'cron', key: 'cron', width: 180, ellipsis: true },
  { title: t('taskPage.colStatus'), key: 'status', width: 110, align: 'center' as const },
  { title: t('taskPage.colAction'), key: 'action', width: 200, fixed: 'right' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

function statusLabel(record: TaskRecord): string {
  if (record.status === TASK_STATUS_RUNNING) {
    return t('taskPage.status.running')
  }
  if (record.status === TASK_STATUS_STOPPING) {
    return t('taskPage.status.stopping')
  }
  return record.statusDesc || '-'
}

function statusColor(status?: number): string {
  if (status === TASK_STATUS_RUNNING) {
    return 'processing'
  }
  return 'default'
}

function isStopped(record: TaskRecord): boolean {
  return record.status === TASK_STATUS_STOPPING
}

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchTaskPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function openCreate() {
  editingRecord.value = null
  formOpen.value = true
}

function openEdit(record: TaskRecord) {
  editingRecord.value = record
  formOpen.value = true
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('taskPage.deleteConfirm'),
    content: t('taskPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteTasks(ids)
      message.success(t('taskPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

async function handleStart(id: string) {
  await startTask(id)
  message.success(t('taskPage.startSuccess'))
  loadTable()
}

async function handleStop(id: string) {
  await stopTask(id)
  message.success(t('taskPage.stopSuccess'))
  loadTable()
}

onMounted(loadTable)
</script>

<template>
  <div class="admin-page system-module-page system-module-page--task">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="t('taskPage.subtitle')">
          <template #icon><ClockCircleOutlined /></template>
          {{ t('taskPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8" class="extra-tags">
          <a-tag v-if="hasSelection" color="blue">
            {{ t('taskPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag color="processing">
            {{ t('taskPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="table-toolbar">
          <a-space wrap>
            <a-button type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              {{ t('taskPage.add') }}
            </a-button>
            <a-button danger :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
              <template #icon><DeleteOutlined /></template>
              {{ t('taskPage.batchDelete') }}
            </a-button>
            <a-button :loading="loading" @click="loadTable">
              <template #icon><ReloadOutlined /></template>
              {{ t('taskPage.refresh') }}
            </a-button>
          </a-space>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
          <template #default="{ scrollY, scrollReady }">
            <a-table
              row-key="id"
              size="middle"
              bordered
              class="scroll-table system-module-table"
              :class="{ 'scroll-table--pending': !scrollReady }"
              :columns="columns"
              :data-source="dataSource"
              :row-selection="rowSelection"
              :scroll="{ x: 960, ...(scrollReady ? { y: scrollY } : {}) }"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => t('taskPage.paginationTotal', { total }),
              }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'name'">
                  <span class="name-cell">{{ record.name || '—' }}</span>
                </template>
                <template v-else-if="column.key === 'cron'">
                  <code class="cron-cell">{{ record.cron || '—' }}</code>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="statusColor(record.status)">{{ statusLabel(record) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space wrap size="small" class="action-group">
                    <a-button
                      v-if="isStopped(record)"
                      type="link"
                      size="small"
                      class="action-link--accent"
                      @click="handleStart(record.id)"
                    >
                      <PlayCircleOutlined />
                      {{ t('taskPage.start') }}
                    </a-button>
                    <a-button
                      v-else
                      type="link"
                      size="small"
                      class="action-link--warn"
                      @click="handleStop(record.id)"
                    >
                      <PauseCircleOutlined />
                      {{ t('taskPage.stop') }}
                    </a-button>
                    <a-button
                      v-if="isStopped(record)"
                      type="link"
                      size="small"
                      @click="openEdit(record)"
                    >
                      <EditOutlined />
                      {{ t('taskPage.edit') }}
                    </a-button>
                    <a-button type="link" size="small" danger @click="confirmDelete([record.id])">
                      <DeleteOutlined />
                      {{ t('taskPage.delete') }}
                    </a-button>
                  </a-space>
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="t('taskPage.empty')">
                  <a-button type="primary" @click="openCreate">
                    <PlusOutlined />
                    {{ t('taskPage.add') }}
                  </a-button>
                </a-empty>
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <TaskFormModal v-model:open="formOpen" :record="editingRecord" @success="loadTable" />
  </div>
</template>

<style scoped>
.cron-cell {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.72);
  background: var(--omes-color-bg-table-head);
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
