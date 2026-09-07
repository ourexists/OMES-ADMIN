<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  AppstoreOutlined,
  BarcodeOutlined,
  CheckOutlined,
  DeleteOutlined,
  EditOutlined,
  ExperimentOutlined,
  FolderOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import type { BomClassifyNode, BomRecord, BomTypeOption } from '@/api/bom'
import { deleteBomClassifies, deleteBoms, fetchBomPage, fetchBomTypes } from '@/api/bom'
import BomClassifyPanel from './components/BomClassifyPanel.vue'
import BomClassifyFormModal from './components/BomClassifyFormModal.vue'
import BomFormModal from './components/BomFormModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()
const route = useRoute()

/** 1=单选（配方选择弹窗），Thymeleaf 老页 selectBom 回调用 */
const isPicker = computed(() => {
  const raw = route.query.page_type
  const value = Array.isArray(raw) ? raw[0] : raw
  return value === '1'
})

const classifyPanelRef = ref<InstanceType<typeof BomClassifyPanel> | null>(null)
const selectedClassify = ref<BomClassifyNode | null>(null)
const classifyCount = ref(0)

const typeOptions = ref<BomTypeOption[]>([])
const loading = ref(false)
const dataSource = ref<BomRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  name: '',
  selfCode: '',
  type: undefined as number | undefined,
  detailName: '',
})

const bomFormOpen = ref(false)
const editingBom = ref<Partial<BomRecord> | null>(null)

const classifyFormOpen = ref(false)
const editingClassify = ref<Partial<BomClassifyNode> | null>(null)

const activeClassifyCode = computed(() => selectedClassify.value?.selfCode)
const activeClassifyLabel = computed(() => selectedClassify.value?.name || selectedClassify.value?.selfCode || '-')

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
    { title: t('bomPage.name'), dataIndex: 'name', key: 'name', ellipsis: true, width: 200 },
    { title: t('bomPage.code'), dataIndex: 'selfCode', key: 'selfCode', width: 140, align: 'center' },
    { title: t('bomPage.type'), dataIndex: 'typeDesc', key: 'typeDesc', width: 120, align: 'center' },
  ]
  cols.push({
    title: t('bomPage.colAction'),
    key: 'action',
    width: isPicker.value ? 88 : 132,
    align: 'center',
    fixed: 'right',
  })
  return cols
})

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

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

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

function onPickerSelect(record: BomRecord) {
  if (callParentFn('selectBom', record)) {
    closePickerWindow()
  } else {
    message.warning(t('bomPage.pickerParentMissing'))
  }
}

function ensureClassifySelected(): boolean {
  if (!selectedClassify.value?.selfCode) {
    message.warning(t('bomPage.selectClassifyFirst'))
    return false
  }
  return true
}

async function loadTypes() {
  const list = await fetchBomTypes()
  typeOptions.value = Array.isArray(list) ? list : []
}

async function loadTable() {
  if (!activeClassifyCode.value) {
    dataSource.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const result = await fetchBomPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      selfCode: searchForm.selfCode.trim() || undefined,
      classifyCode: activeClassifyCode.value,
      type: searchForm.type,
      detailName: searchForm.detailName.trim() || undefined,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
  } finally {
    loading.value = false
  }
}

function onClassifySelect(node: BomClassifyNode | null) {
  selectedClassify.value = node
  pagination.current = 1
  selectedRowKeys.value = []
  loadTable()
}

function onClassifyLoaded(payload: { nodeCount: number }) {
  classifyCount.value = payload.nodeCount
}

async function reloadClassifyTree() {
  await classifyPanelRef.value?.reload()
}

function onSearch() {
  if (!ensureClassifySelected()) {
    return
  }
  pagination.current = 1
  loadTable()
}

function onReset() {
  searchForm.name = ''
  searchForm.selfCode = ''
  searchForm.type = undefined
  searchForm.detailName = ''
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function openCreateBom() {
  if (!ensureClassifySelected()) {
    return
  }
  editingBom.value = { classifyCode: activeClassifyCode.value }
  bomFormOpen.value = true
}

function openEditBom(record: BomRecord) {
  editingBom.value = record
  bomFormOpen.value = true
}

function confirmDeleteBoms(ids: string[]) {
  Modal.confirm({
    title: t('bomPage.deleteConfirm'),
    content: t('bomPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteBoms(ids)
      message.success(t('bomPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

function openCreateClassifyRoot() {
  editingClassify.value = {}
  classifyFormOpen.value = true
}

function openCreateClassifyChild(parent: BomClassifyNode) {
  editingClassify.value = { pcode: parent.code }
  classifyFormOpen.value = true
}

function openEditClassify(node: BomClassifyNode) {
  editingClassify.value = node
  classifyFormOpen.value = true
}

function confirmDeleteClassify(node: BomClassifyNode) {
  if (!node.id) {
    return
  }
  Modal.confirm({
    title: t('bomPage.classifyDeleteConfirm'),
    content: t('bomPage.classifyDeleteContent', { name: node.name || node.selfCode }),
    onOk: async () => {
      await deleteBomClassifies([node.id])
      message.success(t('bomPage.deleteSuccess'))
      selectedClassify.value = null
      await classifyPanelRef.value?.reload()
      loadTable()
    },
  })
}

async function onClassifySaved() {
  await classifyPanelRef.value?.reload()
}

loadTypes()
</script>

<template>
  <div class="admin-page bom-page" :class="{ 'bom-page--picker': isPicker }">
    <a-row :gutter="16" class="admin-page-row page-body">
      <a-col :xs="24" :lg="6" class="admin-page-col">
        <a-card size="small" class="admin-panel-card panel-card classify-card">
          <template #title>
            <AdminPanelTitle icon-class="card-title__icon--classify">
              <template #icon><FolderOutlined /></template>
              {{ t('bomPage.classifyTitle') }}
            </AdminPanelTitle>
          </template>
          <template v-if="!isPicker" #extra>
            <a-space :size="6" wrap class="classify-card__extra">
              <a-tag v-if="classifyCount" color="processing" class="count-tag">
                {{ classifyCount }}
              </a-tag>
              <a-tooltip :title="t('bomPage.refresh')">
                <a-button size="small" @click="reloadClassifyTree">
                  <ReloadOutlined />
                </a-button>
              </a-tooltip>
              <a-button type="primary" size="small" @click="openCreateClassifyRoot">
                <PlusOutlined />
                {{ t('bomPage.classifyAddRoot') }}
              </a-button>
            </a-space>
          </template>
          <div class="admin-panel-body classify-card__body">
            <BomClassifyPanel
              ref="classifyPanelRef"
              :readonly="isPicker"
              @select="onClassifySelect"
              @loaded="onClassifyLoaded"
              @edit="openEditClassify"
              @add-child="openCreateClassifyChild"
              @delete="confirmDeleteClassify"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="18" class="admin-page-col">
        <a-card size="small" class="admin-panel-card panel-card bom-card">
          <template #title>
            <AdminPanelTitle icon-class="card-title__icon--main">
              <template #icon><ExperimentOutlined /></template>
              {{ t('bomPage.title') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="8" wrap>
              <a-tag v-if="selectedClassify" color="blue">{{ activeClassifyLabel }}</a-tag>
              <a-tag v-if="hasSelection && !isPicker" color="blue">
                {{ t('bomPage.selectedCount', { count: selectedRowKeys.length }) }}
              </a-tag>
              <a-tag v-if="pagination.total" color="processing">
                {{ t('bomPage.total', { count: pagination.total }) }}
              </a-tag>
            </a-space>
          </template>

          <div class="admin-panel-body">
            <div class="search-toolbar search-toolbar--compact">
              <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
                <a-form-item :label="t('bomPage.name')" name="name">
                  <a-input size="small"
                    v-model:value="searchForm.name"
                    allow-clear
                    :placeholder="t('bomPage.searchName')"
                    class="search-input"
                  >
                    <template #prefix>
                      <SearchOutlined class="input-prefix-icon" />
                    </template>
                  </a-input>
                </a-form-item>
                <a-form-item :label="t('bomPage.code')" name="selfCode">
                  <a-input size="small"
                    v-model:value="searchForm.selfCode"
                    allow-clear
                    :placeholder="t('bomPage.searchCode')"
                    class="search-input"
                  >
                    <template #prefix>
                      <BarcodeOutlined class="input-prefix-icon" />
                    </template>
                  </a-input>
                </a-form-item>
                <a-form-item :label="t('bomPage.type')" name="type">
                  <a-select size="small"
                    v-model:value="searchForm.type"
                    allow-clear
                    class="search-input"
                    :placeholder="t('bomPage.typeAll')"
                    :options="typeOptions.map((item) => ({ value: Number(item.id), label: item.name }))"
                  />
                </a-form-item>
                <a-form-item :label="t('bomPage.ingredient')" name="detailName">
                  <a-input size="small"
                    v-model:value="searchForm.detailName"
                    allow-clear
                    :placeholder="t('bomPage.ingredientPlaceholder')"
                    class="search-input"
                  />
                </a-form-item>
                <CompactSearchActions
              :query-title="t('bomPage.query')"
              :reset-title="t('bomPage.reset')"
              @reset="onReset"
            />
              </a-form>
            </div>

            <div v-if="!isPicker" class="table-toolbar">
              <a-space wrap>
                <a-button type="primary" @click="openCreateBom">
                  <template #icon><PlusOutlined /></template>
                  {{ t('bomPage.add') }}
                </a-button>
                <a-button danger :disabled="!hasSelection" @click="confirmDeleteBoms(selectedRowKeys)">
                  <template #icon><DeleteOutlined /></template>
                  {{ t('bomPage.batchDelete') }}
                </a-button>
                <a-button :loading="loading" @click="loadTable">
                  <template #icon><ReloadOutlined /></template>
                  {{ t('bomPage.refresh') }}
                </a-button>
              </a-space>
            </div>

            <a-alert
              v-if="!selectedClassify && !isPicker"
              type="info"
              show-icon
              class="select-classify-hint"
              :message="t('bomPage.selectClassifyHint')"
            />

            <TableScrollWrap
              :refresh-keys="[dataSource.length, pagination.total, activeClassifyCode || '', isPicker ? '1' : '0']"
            >
              <template #default="{ scrollY }">
                <a-table
                  row-key="id"
                  size="small"
                  table-layout="fixed"
                  bordered
                  class="scroll-table bom-table"
                  :columns="columns"
                  :data-source="dataSource"
                  :loading="loading"
                  :row-selection="rowSelection"
                  :scroll="{ x: isPicker ? 520 : 680, y: scrollY }"
                  :pagination="{
                    current: pagination.current,
                    pageSize: pagination.pageSize,
                    total: pagination.total,
                    showSizeChanger: true,
                    showTotal: (total: number) => t('bomPage.paginationTotal', { total }),
                  }"
                  @change="onTableChange"
                >
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'name'">
                      <div class="bom-cell-name">
                        <span class="bom-cell-name__icon">
                          <AppstoreOutlined />
                        </span>
                        <span class="bom-cell-name__text">{{ record.name || '—' }}</span>
                      </div>
                    </template>
                    <template v-else-if="column.key === 'selfCode'">
                      <span v-if="record.selfCode" class="code-cell">{{ record.selfCode }}</span>
                      <span v-else class="empty-cell">—</span>
                    </template>
                    <template v-else-if="column.key === 'typeDesc'">
                      <a-tag v-if="record.typeDesc" color="processing">{{ record.typeDesc }}</a-tag>
                      <span v-else class="empty-cell">—</span>
                    </template>
                    <template v-else-if="column.key === 'action'">
                      <div class="bom-cell-actions">
                        <a-button
                          v-if="isPicker"
                          type="link"
                          size="small"
                          @click="onPickerSelect(record)"
                        >
                          <CheckOutlined />
                          {{ t('bomPage.pickerSelect') }}
                        </a-button>
                        <template v-else>
                          <a-button
                            type="link"
                            size="small"
                            class="bom-action-btn bom-action-btn--edit"
                            @click="openEditBom(record)"
                          >
                            <EditOutlined />
                            {{ t('bomPage.edit') }}
                          </a-button>
                          <span class="bom-cell-actions__divider" />
                          <a-button
                            type="link"
                            size="small"
                            danger
                            class="bom-action-btn bom-action-btn--danger"
                            @click="confirmDeleteBoms([record.id])"
                          >
                            <DeleteOutlined />
                            {{ t('bomPage.delete') }}
                          </a-button>
                        </template>
                      </div>
                    </template>
                  </template>

                  <template #emptyText>
                    <a-empty :description="t('bomPage.empty')">
                      <a-button v-if="!isPicker && selectedClassify" type="primary" @click="openCreateBom">
                        <PlusOutlined />
                        {{ t('bomPage.add') }}
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

    <BomFormModal
      v-model:open="bomFormOpen"
      :record="editingBom"
      :default-classify-code="activeClassifyCode"
      @success="loadTable"
    />
    <BomClassifyFormModal v-model:open="classifyFormOpen" :record="editingClassify" @success="onClassifySaved" />
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

.classify-card__body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
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

.table-toolbar {
  margin-bottom: 16px;
}

.select-classify-hint {
  margin-bottom: 12px;
  border-radius: var(--omes-radius-md);
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.bom-table :deep(.ant-table) {
  border-radius: var(--omes-radius-md);
  overflow: hidden;
}

.bom-table :deep(.ant-table-thead > tr > th) {
  background: linear-gradient(180deg, var(--omes-color-bg-elevated) 0%, var(--omes-color-bg-layout) 100%);
  font-weight: 600;
  font-size: 13px;
  padding: 10px 12px !important;
}

.bom-table :deep(.ant-table-tbody > tr > td) {
  padding: 8px 12px !important;
  vertical-align: middle;
  font-size: 13px;
}

.bom-table :deep(.ant-table-tbody > tr:nth-child(even) > td) {
  background: var(--omes-color-bg-muted);
}

.bom-table :deep(.ant-table-tbody > tr:hover > td) {
  background: #f0f7ff !important;
}

.bom-table :deep(.ant-table-cell-fix-right) {
  background: inherit;
}

.bom-cell-name {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.bom-cell-name__icon {
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

.bom-cell-name__text {
  flex: 1;
  min-width: 0;
  font-weight: 500;
  color: var(--omes-color-text);
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

.bom-cell-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  gap: 2px;
  white-space: nowrap;
}

.bom-cell-actions__divider {
  display: inline-block;
  width: 1px;
  height: 14px;
  margin: 0 2px;
  background: var(--omes-color-border-hover);
  flex-shrink: 0;
}

.bom-action-btn {
  display: inline-flex !important;
  align-items: center;
  gap: 4px;
  height: 26px !important;
  padding: 0 6px !important;
  font-size: 13px !important;
}

.bom-action-btn--edit:hover {
  color: var(--omes-color-primary-active) !important;
  background: var(--omes-color-primary-bg);
  border-radius: 4px;
}

.bom-action-btn--danger:hover {
  background: #fff1f0;
  border-radius: 4px;
}

.bom-table :deep(.ant-empty) {
  margin: 32px 0;
}

.bom-page--picker {
  padding: 8px;
}

.bom-page--picker .classify-card__body {
  min-height: 240px;
}

@media (max-width: 992px) {
  .classify-card {
    margin-bottom: 16px;
  }

  .classify-card__body {
    min-height: 240px;
    max-height: 360px;
  }

  .search-input {
    width: 100%;
  }
}
</style>
