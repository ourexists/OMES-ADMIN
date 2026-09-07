<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  AppstoreOutlined,
  BarcodeOutlined,
  CheckOutlined,
  DeleteOutlined,
  EditOutlined,
  FolderOutlined,
  PlusOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { MaterialClassifyRecord, MaterialRecord } from '@/api/material'
import { deleteMaterialClassifies, deleteMaterials, fetchMaterialPage } from '@/api/material'
import MaterialClassifyPanel, {
  type MaterialClassifySelection,
} from './components/MaterialClassifyPanel.vue'
import MaterialClassifyFormModal from './components/MaterialClassifyFormModal.vue'
import MaterialFormModal from './components/MaterialFormModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()
const route = useRoute()

/** 2=单选、3=多选（供 BOM/设备等 Thymeleaf 老页回调用） */
const pickerMode = computed(() => {
  const raw = route.query.page_type
  const value = Array.isArray(raw) ? raw[0] : raw
  if (value === '2') {
    return 'single' as const
  }
  if (value === '3') {
    return 'multi' as const
  }
  return null
})

const isPicker = computed(() => pickerMode.value != null)

const selectedClassify = ref<MaterialClassifySelection>('all')
const classifyList = ref<MaterialClassifyRecord[]>([])
const classifyPanelRef = ref<InstanceType<typeof MaterialClassifyPanel> | null>(null)

const loading = ref(false)
const dataSource = ref<MaterialRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  name: '',
  selfCode: '',
})

const materialFormOpen = ref(false)
const editingMaterial = ref<MaterialRecord | null>(null)

const classifyFormOpen = ref(false)
const editingClassify = ref<MaterialClassifyRecord | null>(null)

const classifyNameMap = computed(() => {
  const map = new Map<string, string>()
  for (const item of classifyList.value) {
    if (item.selfCode) {
      map.set(item.selfCode, item.name || item.selfCode)
    }
  }
  return map
})

const activeClassifyCode = computed(() => {
  const value = selectedClassify.value
  if (value === 'all' || value == null) {
    return undefined
  }
  return value.selfCode
})

const activeClassifyLabel = computed(() => {
  const value = selectedClassify.value
  if (value === 'all' || value == null) {
    return t('materialPage.classifyAll')
  }
  return value.name || value.selfCode || '-'
})

const selectedClassifyRecord = computed(() => {
  const value = selectedClassify.value
  return value && value !== 'all' ? value : null
})

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
    {
      title: t('materialPage.name'),
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
      width: 200,
    },
    {
      title: t('materialPage.code'),
      dataIndex: 'selfCode',
      key: 'selfCode',
      width: 140,
      align: 'center',
    },
  ]
  if (!isPicker.value) {
    cols.push({
      title: t('materialPage.classify'),
      key: 'classify',
      width: 130,
      align: 'center',
    })
  }
  if (pickerMode.value === 'single' || !isPicker.value) {
    cols.push({
      title: t('materialPage.colAction'),
      key: 'action',
      width: pickerMode.value === 'single' ? 88 : 132,
      align: 'center',
      fixed: 'right',
    })
  }
  return cols
})

const rowSelection = computed(() => {
  if (pickerMode.value !== 'multi' && !isPicker.value) {
    return {
      selectedRowKeys: selectedRowKeys.value,
      onChange: (keys: string[]) => {
        selectedRowKeys.value = keys
      },
    }
  }
  if (pickerMode.value === 'multi') {
    return {
      selectedRowKeys: selectedRowKeys.value,
      onChange: (keys: string[]) => {
        selectedRowKeys.value = keys
      },
    }
  }
  return undefined
})

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

const classifyCount = computed(() => classifyList.value.length)

const defaultMaterialClassifyCode = computed(() => activeClassifyCode.value)

function classifyLabel(code?: string): string {
  if (!code) {
    return '-'
  }
  return classifyNameMap.value.get(code) || code
}

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

function onPickerSelect(record: MaterialRecord) {
  if (callParentFn('selectMat', record)) {
    closePickerWindow()
  } else {
    message.warning(t('materialPage.pickerParentMissing'))
  }
}

function onPickerSelectMulti() {
  const rows = dataSource.value.filter((row) => selectedRowKeys.value.includes(row.id))
  if (!rows.length) {
    message.warning(t('materialPage.pickerSelectOne'))
    return
  }
  if (callParentFn('addTbData', rows)) {
    closePickerWindow()
  } else {
    message.warning(t('materialPage.pickerParentMissing'))
  }
}

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchMaterialPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
      classifyCode: activeClassifyCode.value,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function onClassifyChange() {
  pagination.current = 1
  selectedRowKeys.value = []
  loadTable()
}

function onClassifyLoaded(list: MaterialClassifyRecord[]) {
  classifyList.value = list
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

function openCreateMaterial() {
  editingMaterial.value = null
  materialFormOpen.value = true
}

function openEditMaterial(record: MaterialRecord) {
  editingMaterial.value = record
  materialFormOpen.value = true
}

function confirmDeleteMaterials(ids: string[]) {
  Modal.confirm({
    title: t('materialPage.deleteConfirm'),
    content: t('materialPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteMaterials(ids)
      message.success(t('materialPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

function openCreateClassify() {
  editingClassify.value = null
  classifyFormOpen.value = true
}

function openEditClassify() {
  if (!selectedClassifyRecord.value) {
    message.warning(t('materialPage.selectClassifyFirst'))
    return
  }
  editingClassify.value = selectedClassifyRecord.value
  classifyFormOpen.value = true
}

function confirmDeleteClassify() {
  const record = selectedClassifyRecord.value
  if (!record?.id) {
    message.warning(t('materialPage.selectClassifyFirst'))
    return
  }
  Modal.confirm({
    title: t('materialPage.classifyDeleteConfirm'),
    content: t('materialPage.classifyDeleteContent', { name: record.name || record.selfCode }),
    onOk: async () => {
      await deleteMaterialClassifies([record.id])
      message.success(t('materialPage.deleteSuccess'))
      selectedClassify.value = 'all'
      await classifyPanelRef.value?.reload()
      loadTable()
    },
  })
}

async function onClassifySaved() {
  await classifyPanelRef.value?.reload()
  loadTable()
}

watch(selectedClassify, onClassifyChange, { immediate: true })
</script>

<template>
  <div class="admin-page material-page" :class="{ 'material-page--picker': isPicker }">
    <a-row :gutter="16" class="admin-page-row page-body">
      <a-col v-if="!isPicker" :xs="24" :lg="6" class="admin-page-col">
        <a-card size="small" class="admin-panel-card panel-card classify-card">
          <template #title>
            <AdminPanelTitle icon-class="card-title__icon--classify">
              <template #icon><FolderOutlined /></template>
              {{ t('materialPage.classifyTitle') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="6" wrap class="classify-card__extra">
              <a-tag v-if="classifyCount" color="processing" class="count-tag">
                {{ classifyCount }}
              </a-tag>
              <a-button type="primary" size="small" @click="openCreateClassify">
                <PlusOutlined />
                {{ t('materialPage.classifyAdd') }}
              </a-button>
            </a-space>
          </template>
          <div class="admin-panel-body classify-card__body">
            <MaterialClassifyPanel
              ref="classifyPanelRef"
              v-model="selectedClassify"
              @loaded="onClassifyLoaded"
              @edit="openEditClassify"
              @delete="confirmDeleteClassify"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="isPicker ? 24 : 18" class="admin-page-col">
        <a-card size="small" class="admin-panel-card panel-card material-card">
          <template #title>
            <AdminPanelTitle>
              <template #icon><AppstoreOutlined /></template>
              {{ t('materialPage.title') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="8" wrap>
              <a-tag v-if="!isPicker && selectedClassify !== 'all'" color="blue">
                {{ activeClassifyLabel }}
              </a-tag>
              <a-tag v-if="hasSelection && !isPicker" color="blue">
                {{ t('materialPage.selectedCount', { count: selectedRowKeys.length }) }}
              </a-tag>
              <a-tag v-if="pagination.total" color="processing">
                {{ t('materialPage.total', { count: pagination.total }) }}
              </a-tag>
            </a-space>
          </template>

          <div class="admin-panel-body">
            <div class="search-toolbar search-toolbar--compact">
              <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
                <a-form-item :label="t('materialPage.name')" name="name">
                  <a-input size="small"
                    v-model:value="searchForm.name"
                    allow-clear
                    :placeholder="t('materialPage.searchName')"
                    class="search-input"
                  >
                    <template #prefix>
                      <SearchOutlined class="input-prefix-icon" />
                    </template>
                  </a-input>
                </a-form-item>
                <a-form-item :label="t('materialPage.code')" name="selfCode">
                  <a-input size="small"
                    v-model:value="searchForm.selfCode"
                    allow-clear
                    :placeholder="t('materialPage.searchCode')"
                    class="search-input"
                  >
                    <template #prefix>
                      <BarcodeOutlined class="input-prefix-icon" />
                    </template>
                  </a-input>
                </a-form-item>
                <CompactSearchActions
              :query-title="t('materialPage.query')"
              :reset-title="t('materialPage.reset')"
              @reset="onReset"
            />
              </a-form>
            </div>

            <div v-if="!isPicker || pickerMode === 'multi'" class="table-toolbar">
              <a-space wrap>
                <template v-if="!isPicker">
                  <a-button type="primary" @click="openCreateMaterial">
                    <template #icon><PlusOutlined /></template>
                    {{ t('materialPage.add') }}
                  </a-button>
                  <a-button danger :disabled="!hasSelection" @click="confirmDeleteMaterials(selectedRowKeys)">
                    <template #icon><DeleteOutlined /></template>
                    {{ t('materialPage.batchDelete') }}
                  </a-button>
                </template>
                <a-button v-if="pickerMode === 'multi'" type="primary" @click="onPickerSelectMulti">
                  <CheckOutlined />
                  {{ t('materialPage.pickerConfirm') }}
                </a-button>
              </a-space>
            </div>

            <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total, activeClassifyCode || '']">
              <template #default="{ scrollY }">
                <a-table
                  row-key="id"
                  size="small"
                  table-layout="fixed"
                  bordered
                  class="scroll-table material-table"
                  :columns="columns"
                  :data-source="dataSource"
                  :loading="loading"
                  :row-selection="rowSelection"
                  :scroll="{ x: isPicker ? 480 : 640, y: scrollY }"
                  :pagination="{
                    current: pagination.current,
                    pageSize: pagination.pageSize,
                    total: pagination.total,
                    showSizeChanger: true,
                    showTotal: (total: number) => t('materialPage.paginationTotal', { total }),
                  }"
                  @change="onTableChange"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'name'">
                      <div class="mat-cell-name">
                        <span class="mat-cell-name__icon">
                          <AppstoreOutlined />
                        </span>
                        <span class="mat-cell-name__text">{{ record.name || '—' }}</span>
                      </div>
                    </template>
                    <template v-else-if="column.key === 'selfCode'">
                      <span v-if="record.selfCode" class="code-cell">{{ record.selfCode }}</span>
                      <span v-else class="empty-cell">—</span>
                    </template>
                    <template v-else-if="column.key === 'classify'">
                      <a-tag v-if="record.classifyCode" color="processing" class="classify-tag" bordered>
                        {{ classifyLabel(record.classifyCode) }}
                      </a-tag>
                      <span v-else class="empty-cell">—</span>
                    </template>
                    <template v-else-if="column.key === 'action'">
                      <div class="mat-cell-actions">
                        <a-button
                          v-if="pickerMode === 'single'"
                          type="link"
                          size="small"
                          class="mat-action-btn"
                          @click="onPickerSelect(record)"
                        >
                          <CheckOutlined />
                          {{ t('materialPage.pickerSelect') }}
                        </a-button>
                        <template v-else>
                          <a-button
                            type="link"
                            size="small"
                            class="mat-action-btn mat-action-btn--edit"
                            @click="openEditMaterial(record)"
                          >
                            <EditOutlined />
                            {{ t('materialPage.edit') }}
                          </a-button>
                          <span class="mat-cell-actions__divider" />
                          <a-button
                            type="link"
                            size="small"
                            danger
                            class="mat-action-btn mat-action-btn--danger"
                            @click="confirmDeleteMaterials([record.id])"
                          >
                            <DeleteOutlined />
                            {{ t('materialPage.delete') }}
                          </a-button>
                        </template>
                      </div>
                    </template>
                  </template>

                  <template #emptyText>
                    <a-empty :description="t('materialPage.empty')">
                      <a-button v-if="!isPicker" type="primary" @click="openCreateMaterial">
                        <PlusOutlined />
                        {{ t('materialPage.add') }}
                      </a-button>
                    </a-empty>
                  </template>
                </a-table>
              </template>
            </TableScrollWrap>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <MaterialFormModal
      v-model:open="materialFormOpen"
      :record="editingMaterial"
      :classifies="classifyList"
      :default-classify-code="defaultMaterialClassifyCode"
      @success="loadTable"
    />
    <MaterialClassifyFormModal v-model:open="classifyFormOpen" :record="editingClassify" @success="onClassifySaved" />
  </div>
</template>

<style scoped>
.page-body {
  flex: 1;
  min-height: 0;
}

.panel-card {
  height: 100%;
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
}

.panel-card :deep(.ant-card-head) {
  min-height: 48px;
  border-bottom: 1px solid var(--omes-color-border);
}

.panel-card :deep(.ant-card-body) {
  padding: 16px;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.count-tag {
  margin-inline-end: 0;
  font-variant-numeric: tabular-nums;
}

.classify-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 280px);
}

.classify-card__body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.material-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 280px);
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

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.table-toolbar {
  margin-bottom: 16px;
}

.material-table :deep(.ant-table) {
  border-radius: var(--omes-radius-md);
  overflow: hidden;
}

.material-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(180deg, var(--omes-color-bg-elevated) 0%, var(--omes-color-bg-layout) 100%);
  font-weight: 600;
  font-size: 13px;
  padding: 10px 12px !important;
}

.material-table :deep(.ant-table-tbody > tr > td) {
  padding: 8px 12px !important;
  vertical-align: middle;
  font-size: 13px;
}

.material-table :deep(.ant-table-tbody > tr:nth-child(even) > td) {
  background: var(--omes-color-bg-muted);
}

.material-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f0f7ff !important;
}

.material-table :deep(.ant-table-cell-fix-right) {
  background: inherit;
}

.mat-cell-name {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.mat-cell-name__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: var(--omes-radius-md);
  font-size: 15px;
  color: var(--omes-color-primary);
  background: var(--omes-color-primary-bg);
}

.mat-cell-name__text {
  flex: 1;
  min-width: 0;
  font-weight: 500;
  color: var(--omes-color-text);
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.code-cell {
  display: inline-block;
  max-width: 100%;
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--omes-color-primary);
  background: #f0f5ff;
  border: 1px solid var(--omes-color-primary-border);
  padding: 2px 10px;
  border-radius: var(--omes-radius-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.classify-tag {
  margin: 0;
  max-width: 100%;
  font-size: 12px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-cell {
  display: inline-block;
  min-width: 28px;
  padding: 2px 10px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.25);
  background: var(--omes-color-bg-layout);
  border-radius: var(--omes-radius-sm);
  line-height: 20px;
}

.mat-cell-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  gap: 2px;
  white-space: nowrap;
}

.mat-cell-actions__divider {
  display: inline-block;
  width: 1px;
  height: 14px;
  margin: 0 2px;
  background: var(--omes-color-border-hover);
  flex-shrink: 0;
}

.mat-action-btn {
  display: inline-flex !important;
  align-items: center;
  gap: 4px;
  height: 26px !important;
  padding: 0 6px !important;
  font-size: 13px !important;
  line-height: 1 !important;
}

.mat-action-btn :deep(.anticon) {
  font-size: 13px;
}

.mat-action-btn--edit:hover {
  color: var(--omes-color-primary-active) !important;
  background: var(--omes-color-primary-bg);
  border-radius: 4px;
}

.mat-action-btn--danger:hover {
  background: #fff1f0;
  border-radius: 4px;
}

.material-table :deep(.ant-empty) {
  margin: 32px 0;
}

.material-page--picker {
  padding: 8px;
}

.material-page--picker .material-card :deep(.ant-card-body) {
  min-height: auto;
}

@media (max-width: 992px) {
  .classify-card {
    margin-bottom: 16px;
  }

  .classify-card :deep(.ant-card-body),
  .material-card :deep(.ant-card-body) {
    min-height: auto;
  }

  .classify-card__body {
    min-height: 240px;
    max-height: 320px;
  }

  .search-input {
    width: 100%;
  }
}
</style>
