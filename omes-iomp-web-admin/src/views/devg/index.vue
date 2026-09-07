<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  CheckOutlined,
  ClusterOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import { deleteDevgs, fetchDevgPage } from '@/api/devg'
import type { DevgRecord } from '@/types/devg'
import DevgFormModal from './components/DevgFormModal.vue'
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
const dataSource = ref<DevgRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ name: '', selfCode: '' })

const formOpen = ref(false)
const editingRecord = ref<DevgRecord | null>(null)

const columns = computed(() => [
  { title: t('devgPage.name'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('devgPage.code'), dataIndex: 'selfCode', key: 'selfCode', width: 160, align: 'center' as const },
  {
    title: t('devgPage.colAction'),
    key: 'action',
    width: isPicker.value ? 88 : 200,
    align: 'center' as const,
    fixed: 'right' as const,
  },
])

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

function onPickerSelect(record: DevgRecord) {
  if (callParentFn('selectDevg', record)) {
    closePickerWindow()
  } else {
    message.warning(t('devgPage.pickerParentMissing'))
  }
}

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchDevgPage({
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

function openEdit(record: DevgRecord) {
  editingRecord.value = record
  formOpen.value = true
}

function openFlow(record: DevgRecord) {
  router.push({
    path: '/view/devg_flow',
    query: { id: record.id, title: record.name || record.selfCode || '' },
  })
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('devgPage.deleteConfirm'),
    content: t('devgPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteDevgs(ids)
      message.success(t('devgPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

onMounted(loadTable)
</script>

<template>
  <div class="admin-page process-module-page process-module-page--devg">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="!isPicker ? t('devgPage.subtitle') : undefined">
          <template #icon><ClusterOutlined /></template>
          {{ t('devgPage.title') }}
        </AdminPanelTitle>
      </template>
      <template v-if="!isPicker" #extra>
        <a-space :size="8" class="extra-tags">
          <a-tag v-if="hasSelection" color="blue">
            {{ t('devgPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag v-if="pagination.total" color="processing">
            {{ t('devgPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item :label="t('devgPage.name')" name="name">
              <a-input size="small"
                v-model:value="searchForm.name"
                allow-clear
                class="search-input"
                :placeholder="t('devgPage.name')"
              >
                <template #prefix>
                  <SearchOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item :label="t('devgPage.code')" name="selfCode">
              <a-input size="small"
                v-model:value="searchForm.selfCode"
                allow-clear
                class="search-input"
                :placeholder="t('devgPage.code')"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('devgPage.query')"
              :reset-title="t('devgPage.reset')"
              @reset="onReset"
            >
              <a-tooltip :title="t('devgPage.refresh')">
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
              {{ t('devgPage.add') }}
            </a-button>
            <a-button danger :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
              <template #icon><DeleteOutlined /></template>
              {{ t('devgPage.batchDelete') }}
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
                showTotal: (total: number) => t('devgPage.paginationTotal', { total }),
              }"
              :scroll="{ x: isPicker ? 560 : 640, y: scrollY }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space v-if="isPicker" size="small" class="action-group">
                    <a-button type="link" size="small" @click="onPickerSelect(record as DevgRecord)">
                      <CheckOutlined />
                      {{ t('devgPage.pickerSelect') }}
                    </a-button>
                  </a-space>
                  <a-space v-else wrap size="small" class="action-group">
                    <a-button
                      type="link"
                      size="small"
                      class="action-link--flow"
                      @click="openFlow(record as DevgRecord)"
                    >
                      <ClusterOutlined />
                      {{ t('devgPage.flow') }}
                    </a-button>
                    <a-button type="link" size="small" @click="openEdit(record as DevgRecord)">
                      <EditOutlined />
                      {{ t('devgPage.edit') }}
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      danger
                      @click="confirmDelete([(record as DevgRecord).id])"
                    >
                      <DeleteOutlined />
                      {{ t('devgPage.delete') }}
                    </a-button>
                  </a-space>
                </template>
              </template>
              <template v-if="!isPicker" #emptyText>
                <a-empty :description="t('devgPage.empty')">
                  <a-button type="primary" @click="openCreate">
                    <PlusOutlined />
                    {{ t('devgPage.add') }}
                  </a-button>
                </a-empty>
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <DevgFormModal v-model:open="formOpen" :record="editingRecord" @success="loadTable" />
  </div>
</template>
