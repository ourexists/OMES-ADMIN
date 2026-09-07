<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import {
  CalendarOutlined,
  CheckCircleOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  StopOutlined,
  ThunderboltOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons-vue'
import type { InspectPlanRecord } from '@/api/inspect-plan'
import {
  deleteInspectPlans,
  disableInspectPlan,
  enableInspectPlan,
  fetchInspectPlanPage,
  generateInspectPlanTasks,
  inspectPlanCycleLabel,
} from '@/api/inspect-plan'
import { fetchInspectTemplateList } from '@/api/inspect-template'
import type { InspectTemplateRecord } from '@/api/inspect-template'
import InspectPlanFormModal from './components/InspectPlanFormModal.vue'
import InspectPlanTasksModal from './components/InspectPlanTasksModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue/es/table'

const { t } = useI18n()
const route = useRoute()

const loading = ref(false)
const dataSource = ref<InspectPlanRecord[]>([])
const selectedRowKeys = ref<string[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })
const templateOptions = ref<InspectTemplateRecord[]>([])

const presetTemplateId = computed(() => {
  const value = route.query.templateId
  return typeof value === 'string' && value ? value : undefined
})

const searchForm = reactive({
  name: '',
  templateId: undefined as string | undefined,
  cycleType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const formOpen = ref(false)
const formViewOnly = ref(false)
const editingRecord = ref<InspectPlanRecord | null>(null)
const tasksModalOpen = ref(false)
const tasksPlan = ref<InspectPlanRecord | null>(null)

const columns = computed(() => [
  { title: t('inspectPlanPage.colName'), dataIndex: 'name', key: 'name', minWidth: 160, ellipsis: true },
  { title: t('inspectPlanPage.colTemplate'), dataIndex: 'templateName', key: 'templateName', width: 140, ellipsis: true },
  { title: t('inspectPlanPage.colCycleType'), key: 'cycleType', width: 90 },
  { title: t('inspectPlanPage.colCycleConfig'), dataIndex: 'cycleConfig', key: 'cycleConfig', width: 120, ellipsis: true },
  { title: t('inspectPlanPage.colStatus'), key: 'status', width: 88, align: 'center' as const },
  { title: t('inspectPlanPage.colRemark'), dataIndex: 'remark', key: 'remark', minWidth: 120, ellipsis: true },
  { title: t('inspectPlanPage.colCreatedTime'), dataIndex: 'createdTime', key: 'createdTime', width: 170 },
  { title: t('inspectPlanPage.colAction'), key: 'action', width: 300, fixed: 'right' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

const hasSelection = computed(() => selectedRowKeys.value.length > 0)

const cycleFilterOptions = computed(() => [
  { value: 1, label: t('inspectPlanPage.cycleType.每日') },
  { value: 2, label: t('inspectPlanPage.cycleType.每周') },
  { value: 3, label: t('inspectPlanPage.cycleType.每月') },
])

const statusFilterOptions = computed(() => [
  { value: 0, label: t('inspectPlanPage.statusDisabled') },
  { value: 1, label: t('inspectPlanPage.statusEnabled') },
])

function cycleLabel(record: InspectPlanRecord): string {
  if (record.cycleTypeDesc) {
    return t(`inspectPlanPage.cycleType.${record.cycleTypeDesc}`, record.cycleTypeDesc)
  }
  return inspectPlanCycleLabel(record.cycleType)
}

async function loadTemplates() {
  const list = await fetchInspectTemplateList()
  templateOptions.value = Array.isArray(list) ? list : []
}

async function loadTable() {
  loading.value = true
  try {
    const result = await fetchInspectPlanPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.name.trim() || undefined,
      templateId: searchForm.templateId,
      cycleType: searchForm.cycleType,
      status: searchForm.status,
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
  searchForm.templateId = presetTemplateId.value
  searchForm.cycleType = undefined
  searchForm.status = undefined
  onSearch()
}

function onTableChange(page: TablePaginationConfig) {
  pagination.current = page.current || 1
  pagination.pageSize = page.pageSize || 10
  loadTable()
}

function openCreate() {
  editingRecord.value = null
  formViewOnly.value = false
  formOpen.value = true
}

function openEdit(record: InspectPlanRecord) {
  editingRecord.value = record
  formViewOnly.value = false
  formOpen.value = true
}

function openView(record: InspectPlanRecord) {
  editingRecord.value = record
  formViewOnly.value = true
  formOpen.value = true
}

function confirmDelete(ids: string[]) {
  Modal.confirm({
    title: t('inspectPlanPage.deleteConfirm'),
    content: t('inspectPlanPage.deleteContent', { count: ids.length }),
    onOk: async () => {
      await deleteInspectPlans(ids)
      message.success(t('inspectPlanPage.deleteSuccess'))
      selectedRowKeys.value = []
      loadTable()
    },
  })
}

async function onEnable(record: InspectPlanRecord) {
  await enableInspectPlan(record.id)
  message.success(t('inspectPlanPage.enableSuccess'))
  loadTable()
}

async function onDisable(record: InspectPlanRecord) {
  await disableInspectPlan(record.id)
  message.success(t('inspectPlanPage.disableSuccess'))
  loadTable()
}

async function onGenerateTasks(record: InspectPlanRecord) {
  await generateInspectPlanTasks(record.id)
  message.success(t('inspectPlanPage.generateSuccess'))
}

function openTasks(record: InspectPlanRecord) {
  tasksPlan.value = record
  tasksModalOpen.value = true
}

function openFromRouteQuery() {
  const id = route.query.id
  if (typeof id === 'string' && id) {
    editingRecord.value = { id } as InspectPlanRecord
    formViewOnly.value = route.query.view === '1' || route.query.view === 'true'
    formOpen.value = true
    return
  }
  if (route.path.includes('inspect_plan_form_edit')) {
    editingRecord.value = null
    formViewOnly.value = false
    formOpen.value = true
  }
}

onMounted(async () => {
  searchForm.templateId = presetTemplateId.value
  await loadTemplates()
  await loadTable()
  openFromRouteQuery()
})
</script>

<template>
  <div class="admin-page inspect-module-page inspect-module-page--plan">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="t('inspectPlanPage.subtitle')">
          <template #icon><CalendarOutlined /></template>
          {{ t('inspectPlanPage.title') }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8" class="extra-tags">
          <a-tag v-if="presetTemplateId" color="blue">
            {{ t('inspectPlanPage.templateFilter', { id: presetTemplateId }) }}
          </a-tag>
          <a-tag v-if="hasSelection" color="blue">
            {{ t('inspectPlanPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-tag v-if="pagination.total" color="processing">
            {{ t('inspectPlanPage.total', { count: pagination.total }) }}
          </a-tag>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="search-toolbar search-toolbar--compact">
          <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
            <a-form-item :label="t('inspectPlanPage.colName')" name="name">
              <a-input size="small"
                v-model:value="searchForm.name"
                allow-clear
                :placeholder="t('inspectPlanPage.searchName')"
                class="search-input"
              >
                <template #prefix>
                  <SearchOutlined class="input-prefix-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item :label="t('inspectPlanPage.colTemplate')" name="templateId">
              <a-select size="small"
                v-model:value="searchForm.templateId"
                allow-clear
                show-search
                option-filter-prop="label"
                class="search-select"
                :placeholder="t('inspectPlanPage.searchTemplate')"
                :options="templateOptions.map((item) => ({ value: item.id, label: item.name || item.id }))"
              />
            </a-form-item>
            <a-form-item :label="t('inspectPlanPage.colCycleType')" name="cycleType">
              <a-select size="small"
                v-model:value="searchForm.cycleType"
                allow-clear
                class="search-select-sm"
                :placeholder="t('inspectPlanPage.searchCycleType')"
                :options="cycleFilterOptions"
              />
            </a-form-item>
            <a-form-item :label="t('inspectPlanPage.colStatus')" name="status">
              <a-select size="small"
                v-model:value="searchForm.status"
                allow-clear
                class="search-select-sm"
                :placeholder="t('inspectPlanPage.searchStatus')"
                :options="statusFilterOptions"
              />
            </a-form-item>
            <CompactSearchActions
              :query-title="t('inspectPlanPage.query')"
              :reset-title="t('inspectPlanPage.reset')"
              @reset="onReset"
            />
          </a-form>
        </div>

        <div class="table-toolbar">
          <a-space wrap>
            <a-button type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              {{ t('inspectPlanPage.add') }}
            </a-button>
            <a-button danger :disabled="!hasSelection" @click="confirmDelete(selectedRowKeys)">
              <template #icon><DeleteOutlined /></template>
              {{ t('inspectPlanPage.batchDelete') }}
            </a-button>
            <a-button :loading="loading" @click="loadTable">
              <template #icon><ReloadOutlined /></template>
              {{ t('inspectPlanPage.refresh') }}
            </a-button>
          </a-space>
        </div>

        <TableScrollWrap :refresh-keys="[dataSource.length, pagination.total]">
          <template #default="{ scrollY }">
            <a-table
              row-key="id"
              size="middle"
              bordered
              class="scroll-table inspect-module-table"
              :columns="columns"
              :data-source="dataSource"
              :row-selection="rowSelection"
              :scroll="{ x: 1200, y: scrollY }"
              :pagination="{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                showSizeChanger: true,
                showTotal: (total: number) => t('inspectPlanPage.paginationTotal', { total }),
              }"
              @change="onTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'name'">
                  <span class="name-cell">{{ record.name || '—' }}</span>
                </template>
                <template v-else-if="column.key === 'templateName'">
                  <span class="muted-cell">{{ record.templateName || '—' }}</span>
                </template>
                <template v-else-if="column.key === 'cycleType'">
                  <a-tag class="cycle-tag" color="blue">{{ cycleLabel(record) }}</a-tag>
                </template>
                <template v-else-if="column.key === 'cycleConfig'">
                  <span class="time-cell">{{ record.cycleConfig || '—' }}</span>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag :color="record.status === 1 ? 'success' : 'default'">
                    {{ record.status === 1 ? t('inspectPlanPage.statusEnabled') : t('inspectPlanPage.statusDisabled') }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space wrap size="small" class="action-group">
                    <template v-if="record.status === 0">
                      <a-button type="link" size="small" class="action-link--accent" @click="onEnable(record)">
                        <CheckCircleOutlined />
                        {{ t('inspectPlanPage.enable') }}
                      </a-button>
                      <a-button type="link" size="small" @click="openEdit(record)">
                        <EditOutlined />
                        {{ t('inspectPlanPage.edit') }}
                      </a-button>
                    </template>
                    <template v-else>
                      <a-button type="link" size="small" @click="onDisable(record)">
                        <StopOutlined />
                        {{ t('inspectPlanPage.disable') }}
                      </a-button>
                      <a-button type="link" size="small" @click="openView(record)">
                        <EyeOutlined />
                        {{ t('inspectPlanPage.view') }}
                      </a-button>
                    </template>
                    <a-button type="link" size="small" class="action-link--warn" @click="onGenerateTasks(record)">
                      <ThunderboltOutlined />
                      {{ t('inspectPlanPage.generateTasks') }}
                    </a-button>
                    <a-button type="link" size="small" @click="openTasks(record)">
                      <UnorderedListOutlined />
                      {{ t('inspectPlanPage.tasks') }}
                    </a-button>
                    <a-button type="link" size="small" danger @click="confirmDelete([record.id])">
                      <DeleteOutlined />
                      {{ t('inspectPlanPage.delete') }}
                    </a-button>
                  </a-space>
                </template>
              </template>

              <template #emptyText>
                <a-empty :description="t('inspectPlanPage.empty')">
                  <a-button type="primary" @click="openCreate">
                    <PlusOutlined />
                    {{ t('inspectPlanPage.add') }}
                  </a-button>
                </a-empty>
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <InspectPlanFormModal
      v-model:open="formOpen"
      :record="editingRecord"
      :view-only="formViewOnly"
      :preset-template-id="presetTemplateId"
      @success="loadTable"
    />

    <InspectPlanTasksModal v-model:open="tasksModalOpen" :plan="tasksPlan" />
  </div>
</template>

