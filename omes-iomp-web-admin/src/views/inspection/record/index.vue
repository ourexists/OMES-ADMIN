<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import {
  DeleteOutlined,
  EyeOutlined,
  FileTextOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { InspectRecordRecord } from '@/api/inspect-record'
import { deleteInspectRecords, fetchInspectRecordPage } from '@/api/inspect-record'
import InspectRecordDetailModal from './components/InspectRecordDetailModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()
const route = useRoute()

const loading = ref(false)
const dataSource = ref<InspectRecordRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const taskId = computed(() => {
  const value = route.query.taskId
  return typeof value === 'string' && value ? value : undefined
})

const searchForm = reactive({
  equipName: '',
})

const detailOpen = ref(false)
const detailRecordId = ref<string | null>(null)

const columns = computed(() => [
  { title: t('inspectRecordPage.colEquip'), dataIndex: 'equipName', key: 'equipName', minWidth: 160, ellipsis: true },
  { title: t('inspectRecordPage.colScore'), dataIndex: 'score', key: 'score', width: 100, align: 'center' as const },
  { title: t('inspectRecordPage.colItemCount'), key: 'itemCount', width: 100, align: 'center' as const },
  { title: t('inspectRecordPage.colRecordTime'), dataIndex: 'recordTime', key: 'recordTime', width: 170 },
  { title: t('inspectRecordPage.colCreateTime'), dataIndex: 'createTime', key: 'createTime', width: 170 },
  { title: t('inspectRecordPage.colAction'), key: 'action', width: 140, fixed: 'right' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

function itemCount(record: InspectRecordRecord): number {
  return record.items?.length ?? 0
}

async function loadTable() {
  loading.value = true
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
    loading.value = false
  }
}

function onSearch() {
  pagination.current = 1
  loadTable()
}

function onReset() {
  searchForm.equipName = ''
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function openDetail(record: InspectRecordRecord) {
  detailRecordId.value = record.id
  detailOpen.value = true
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('inspectRecordPage.deleteConfirm'),
    content: t('inspectRecordPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteInspectRecords(ids)
      message.success(t('inspectRecordPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

onMounted(loadTable)
</script>

<template>
  <div class="admin-page inspect-record-page">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle>
          <template #icon><FileTextOutlined /></template>
          {{ t('inspectRecordPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8">
          <a-tag v-if="taskId" color="blue">
            {{ t('inspectRecordPage.taskFilter', { id: taskId }) }}
          </a-tag>
          <a-tag v-if="hasSelection" color="blue">
            {{ t('inspectRecordPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag v-if="pagination.total" color="processing">
            {{ t('inspectRecordPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item :label="t('inspectRecordPage.colEquip')" name="equipName">
              <a-input size="small"
                v-model:value="searchForm.equipName"
                allow-clear
                :placeholder="t('inspectRecordPage.searchEquip')"
                class="search-input"
              >
                <template #prefix>
                  <SearchOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>
            <CompactSearchActions
              :query-title="t('inspectRecordPage.query')"
              :reset-title="t('inspectRecordPage.reset')"
              @reset="onReset"
            />
          </a-form>
        </div>

        <div class="table-toolbar">
          <a-space wrap>
            <a-button danger :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
              <template #icon><DeleteOutlined /></template>
              {{ t('inspectRecordPage.batchDelete') }}
            </a-button>
            <a-button :loading="loading" @click="loadTable">
              <template #icon><ReloadOutlined /></template>
              {{ t('inspectRecordPage.refresh') }}
            </a-button>
          </a-space>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
          <template #default="{ scrollY }">
            <a-table
              row-key="id"
              size="middle"
              bordered
              class="scroll-table inspect-record-table"
              :columns="columns"
              :data-source="dataSource"
              :row-selection="rowSelection"
              :scroll="{ x: 900, y: scrollY }"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => t('inspectRecordPage.paginationTotal', { total }),
              }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'score'">
                  {{ record.score != null ? record.score : '-' }}
                </template>
                <template v-else-if="column.key === 'itemCount'">
                  {{ itemCount(record) }}
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space wrap size="small" class="action-group">
                    <a-button type="link" size="small" @click="openDetail(record)">
                      <EyeOutlined />
                      {{ t('inspectRecordPage.detail') }}
                    </a-button>
                    <a-button type="link" size="small" danger @click="confirmDelete([record.id])">
                      <DeleteOutlined />
                      {{ t('inspectRecordPage.delete') }}
                    </a-button>
                  </a-space>
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="t('inspectRecordPage.empty')" />
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <InspectRecordDetailModal v-model:open="detailOpen" :record-id="detailRecordId" />
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
  width: 220px;
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.table-toolbar {
  margin-bottom: 16px;
}

.inspect-record-table :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-elevated);
  font-weight: 600;
}

.action-group :deep(.ant-btn-link) {
  padding-inline: 4px;
}

.action-group :deep(.ant-btn-link .anticon) {
  margin-inline-end: 4px;
  font-size: 13px;
}
</style>
