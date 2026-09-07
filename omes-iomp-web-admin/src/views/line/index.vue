<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  CheckOutlined,
  CloudDownloadOutlined,
  DeleteOutlined,
  EditOutlined,
  NodeIndexOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { deleteLines, fetchLinePage } from '@/api/line'
import type { LineRecord } from '@/types/line'
import LineDownloadModal from './components/LineDownloadModal.vue'
import LineFormModal from './components/LineFormModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()

const isPicker = computed(() => {
  const raw = route.query.page_type
  const value = Array.isArray(raw) ? raw[0] : raw
  return value === '2'
})

const loading = ref(false)
const dataSource = ref<LineRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', selfCode: '' })

const formOpen = ref(false)
const editingRecord = ref<LineRecord | null>(null)
const downloadOpen = ref(false)
const downloadLineId = ref('')

const columns = computed(() => {
  const cols: {
    title: string
    dataIndex?: string
    key: string
    ellipsis?: boolean
    width?: number
    align?: 'left' | 'center' | 'right'
    fixed?: 'right'
  }[] = [
    { title: t('linePage.name'), dataIndex: 'name', key: 'name', ellipsis: true, width: 140 },
    { title: t('linePage.code'), dataIndex: 'selfCode', key: 'selfCode', width: 120, align: 'center' },
    { title: t('linePage.productCode'), dataIndex: 'productCode', key: 'productCode', ellipsis: true, width: 130 },
    { title: t('linePage.productName'), dataIndex: 'productName', key: 'productName', ellipsis: true, width: 140 },
    { title: t('linePage.materialCode'), dataIndex: 'materialCode', key: 'materialCode', ellipsis: true, width: 120 },
    { title: t('linePage.materialName'), dataIndex: 'materialName', key: 'materialName', ellipsis: true, width: 130 },
    { title: t('linePage.versionNo'), dataIndex: 'versionNo', key: 'versionNo', width: 90, align: 'center' },
  ]
  if (!isPicker.value) {
    cols.push(
      { title: t('linePage.throughput'), dataIndex: 'throughput', key: 'throughput', width: 110, align: 'center' },
      {
        title: t('linePage.stepInterval'),
        dataIndex: 'stepInterval',
        key: 'stepInterval',
        width: 110,
        align: 'center',
      },
      { title: t('linePage.syncTime'), dataIndex: 'syncTime', key: 'syncTime', width: 170 },
      { title: t('linePage.plcTime'), dataIndex: 'plcTime', key: 'plcTime', width: 170 },
    )
  }
  cols.push({
    title: t('linePage.colAction'),
    key: 'action',
    width: isPicker.value ? 88 : 240,
    align: 'center',
    fixed: 'right',
  })
  return cols
})

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

const rowSelection = computed(() => {
  if (isPicker.value) {
    return undefined
  }
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: string[]) => {
      selectedRowKeys.value = keys
    },
  }
})

function callParentFn(name: string, payload: unknown) {
  const parent = window.parent as Window & Record<string, unknown>
  const fn = parent[name]
  if (typeof fn === 'function') {
    ;(fn as (data: unknown) => void)(payload)
    return true
  }
  return false
}

function closePickerWindow() {
  try {
    window.close()
  } catch {
    /* ignore */
  }
}

function onPickerSelect(record: LineRecord) {
  if (callParentFn('selectLine', record)) {
    closePickerWindow()
  } else {
    message.warning(t('linePage.pickerParentMissing'))
  }
}

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchLinePage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
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
  searchForm.name = ''
  searchForm.selfCode = ''
  onSearch()
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

function openEdit(record: LineRecord) {
  editingRecord.value = record
  formOpen.value = true
}

function openFlow(record: LineRecord) {
  router.push({
    path: '/view/line_flow',
    query: { line_id: record.id, title: record.name || record.selfCode || '' },
  })
}

function openDownload(record: LineRecord) {
  downloadLineId.value = record.id
  downloadOpen.value = true
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('linePage.deleteConfirm'),
    content: t('linePage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteLines(ids)
      message.success(t('linePage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

onMounted(loadTable)
</script>

<template>
  <div class="admin-page process-module-page process-module-page--line">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="!isPicker ? t('linePage.subtitle') : undefined">
          <template #icon><NodeIndexOutlined /></template>
          {{ t('linePage.title') }}
        </AdminPanelTitle>
      </template>
      <template v-if="!isPicker" #extra>
        <a-space :size="8" class="extra-tags">
          <a-tag v-if="hasSelection" color="blue">
            {{ t('linePage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag v-if="pagination.total" color="processing">
            {{ t('linePage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item :label="t('linePage.name')" name="name">
              <a-input size="small"
                v-model:value="searchForm.name"
                allow-clear
                class="search-input"
                :placeholder="t('linePage.name')"
              >
                <template #prefix>
                  <SearchOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item :label="t('linePage.code')" name="selfCode">
              <a-input size="small"
                v-model:value="searchForm.selfCode"
                allow-clear
                class="search-input"
                :placeholder="t('linePage.code')"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('linePage.query')"
              :reset-title="t('linePage.reset')"
              @reset="onReset"
            >
              <a-tooltip :title="t('linePage.refresh')">
                <a-button size="small" :loading="loading" @click="loadTable">
                  <ReloadOutlined />
                </a-button>
              </a-tooltip>
            </CompactSearchActions>
          </a-form>
        </div>

        <div v-if="!isPicker" class="table-toolbar">
          <a-space wrap>
            <a-button type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              {{ t('linePage.add') }}
            </a-button>
            <a-button danger :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
              <template #icon><DeleteOutlined /></template>
              {{ t('linePage.batchDelete') }}
            </a-button>
          </a-space>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
          <template #default="{ scrollY }">
            <a-table
              row-key="id"
              size="middle"
              bordered
              class="scroll-table process-module-table"
              :loading="loading"
              :columns="columns"
              :data-source="dataSource"
              :row-selection="rowSelection"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => t('linePage.paginationTotal', { total }),
              }"
              :scroll="{ x: isPicker ? 980 : 1560, y: scrollY }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'syncTime' || column.key === 'plcTime'">
                  <span class="time-cell">{{ record[column.key as keyof LineRecord] || '-' }}</span>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space v-if="isPicker" size="small" class="action-group">
                    <a-button type="link" size="small" @click="onPickerSelect(record as LineRecord)">
                      <CheckOutlined />
                      {{ t('linePage.pickerSelect') }}
                    </a-button>
                  </a-space>
                  <a-space v-else wrap size="small" class="action-group">
                    <a-button
                      type="link"
                      size="small"
                      class="action-link--flow"
                      @click="openFlow(record as LineRecord)"
                    >
                      <NodeIndexOutlined />
                      {{ t('linePage.flow') }}
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      class="action-link--download"
                      @click="openDownload(record as LineRecord)"
                    >
                      <CloudDownloadOutlined />
                      {{ t('linePage.download') }}
                    </a-button>
                    <a-button type="link" size="small" @click="openEdit(record as LineRecord)">
                      <EditOutlined />
                      {{ t('linePage.edit') }}
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      danger
                      @click="confirmDelete([(record as LineRecord).id])"
                    >
                      <DeleteOutlined />
                      {{ t('linePage.delete') }}
                    </a-button>
                  </a-space>
                </template>
              </template>
              <template v-if="!isPicker" #emptyText>
                <a-empty :description="t('linePage.empty')">
                  <a-button type="primary" @click="openCreate">
                    <PlusOutlined />
                    {{ t('linePage.add') }}
                  </a-button>
                </a-empty>
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <LineFormModal v-model:open="formOpen" :record="editingRecord" @success="loadTable" />
    <LineDownloadModal
      v-model:open="downloadOpen"
      :line-id="downloadLineId"
      @success="loadTable"
    />
  </div>
</template>
