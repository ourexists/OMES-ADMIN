<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowLeftOutlined,
  ClusterOutlined,
  DisconnectOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import { bindDevgEquips, fetchDevgEquips, unbindDevgEquips } from '@/api/devg'
import type { DevgEquipRecord } from '@/types/devg'
import EquipBindPickerModal from './components/EquipBindPickerModal.vue'
import EquipProcessModal from './components/EquipProcessModal.vue'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const dgId = computed(() => String(route.query.id || ''))
const pageTitle = computed(() => {
  const title = route.query.title
  const name = Array.isArray(title) ? title[0] : title
  return name ? `${t('devgFlowPage.title')} · ${name}` : t('devgFlowPage.title')
})

const loading = ref(false)
const dataSource = ref<DevgEquipRecord[]>([])
const selectedRowKeys = ref<string[]>([])

const bindOpen = ref(false)
const processOpen = ref(false)
const editingEquip = ref<DevgEquipRecord | null>(null)

const hasSelection = computed(() => selectedRowKeys.value.length > 0)
const boundIds = computed(() => dataSource.value.map((item) => item.id).filter(Boolean))

const columns = computed(() => [
  { title: t('devgFlowPage.devCode'), dataIndex: 'selfCode', key: 'selfCode', width: 140 },
  { title: t('devgFlowPage.devName'), dataIndex: 'name', key: 'name', ellipsis: true },
  { title: t('devgFlowPage.devType'), dataIndex: 'typeDesc', key: 'typeDesc', width: 120 },
  { title: t('devgFlowPage.workshop'), key: 'workshop', width: 120, ellipsis: true },
  { title: t('devgFlowPage.material'), key: 'material', minWidth: 240, ellipsis: true },
  { title: t('devgFlowPage.colAction'), key: 'action', width: 140, fixed: 'right' as const, align: 'center' as const },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys
  },
}))

function goBack() {
  router.push('/view/devg_tables')
}

function openBind() {
  if (!dgId.value) {
    return
  }
  bindOpen.value = true
}

function openProcess(record: DevgEquipRecord) {
  editingEquip.value = record
  processOpen.value = true
}

function formatCapacity(value?: number | string | null) {
  if (value == null || value === '') {
    return ''
  }
  return `${value} ${t('devgFlowPage.capacityUnit')}`
}

function formatMaterial(item: { matName?: string; matCode?: string; maxCapacity?: number | string | null }) {
  const name = item.matName || item.matCode || ''
  const capacity = formatCapacity(item.maxCapacity)
  return capacity ? `${name} ${capacity}` : name
}

async function loadList() {
  if (!dgId.value) {
    dataSource.value = []
    return
  }
  loading.value = true
  try {
    dataSource.value = (await fetchDevgEquips(dgId.value)) || []
  } finally {
    loading.value = false
  }
}

async function onBindConfirm(ids: string[]) {
  if (!dgId.value) {
    return
  }
  if (!ids.length) {
    message.warning(t('devgFlowPage.bindEmpty'))
    return
  }
  await bindDevgEquips(dgId.value, ids)
  message.success(t('devgFlowPage.bindSuccess'))
  selectedRowKeys.value = []
  loadList()
}

function confirmUnbind(ids: string[]) {
  Modal.confirm({
    title: t('devgFlowPage.unbindConfirm'),
    content: t('devgFlowPage.unbindContent', { count: ids.length }),
    onOk: async () => {
      await unbindDevgEquips(dgId.value, ids)
      message.success(t('devgFlowPage.unbindSuccess'))
      selectedRowKeys.value = []
      loadList()
    },
  })
}

onMounted(loadList)
</script>

<template>
  <div class="admin-page process-flow-page process-flow-page--devg">
    <a-card size="small" class="admin-panel-card panel-card">
      <template #title>
        <AdminPanelTitle :subtitle="t('devgFlowPage.subtitle')">
          <template #icon><ClusterOutlined /></template>
          {{ pageTitle }}
        </AdminPanelTitle>
      </template>
      <template #extra>
        <a-space :size="8" class="extra-tags">
          <a-tag v-if="hasSelection" color="blue">
            {{ t('devgFlowPage.selectedCount', { count: selectedRowKeys.length }) }}
          </a-tag>
          <a-button type="link" class="back-link" @click="goBack">
            <ArrowLeftOutlined />
            {{ t('devgFlowPage.back') }}
          </a-button>
        </a-space>
      </template>

      <div class="admin-panel-body">
        <div class="toolbar-strip">
          <div class="toolbar-strip__actions">
            <a-space wrap>
              <a-button type="primary" :disabled="!dgId" @click="openBind">
                <template #icon><PlusOutlined /></template>
                {{ t('devgFlowPage.add') }}
              </a-button>
              <a-button danger :disabled="!hasSelection" @click="confirmUnbind(selectedRowKeys)">
                <template #icon><DisconnectOutlined /></template>
                {{ t('devgFlowPage.batchDelete') }}
              </a-button>
              <a-button :loading="loading" @click="loadList">
                <template #icon><ReloadOutlined /></template>
                {{ t('devgFlowPage.refresh') }}
              </a-button>
            </a-space>
          </div>
        </div>

        <p v-if="!dgId" class="flow-state flow-state--error">{{ t('devgFlowPage.missingDgId') }}</p>

        <TableScrollWrap v-else :refresh-keys="[dataSource.length]">
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
              :pagination="false"
              :scroll="{ x: 1100, y: scrollY }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'workshop'">
                  {{ (record as DevgEquipRecord).workshop?.name || '-' }}
                </template>
                <template v-else-if="column.key === 'material'">
                  <template v-if="(record as DevgEquipRecord).processMaterials?.length">
                    <a-tag
                      v-for="item in (record as DevgEquipRecord).processMaterials"
                      :key="item.matCode"
                      class="mat-tag"
                    >
                      {{ formatMaterial(item) }}
                    </a-tag>
                  </template>
                  <span v-else>-</span>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-space size="small" class="action-group action-group--compact">
                    <a-tooltip :title="t('devgFlowPage.tipEdit')">
                      <a-button type="link" size="small" @click="openProcess(record as DevgEquipRecord)">
                        <EditOutlined />
                      </a-button>
                    </a-tooltip>
                    <a-tooltip :title="t('devgFlowPage.tipUnbind')">
                      <a-button
                        type="link"
                        size="small"
                        danger
                        @click="confirmUnbind([(record as DevgEquipRecord).id])"
                      >
                        <DisconnectOutlined />
                      </a-button>
                    </a-tooltip>
                  </a-space>
                </template>
              </template>
              <template #emptyText>
                <a-empty :description="t('devgFlowPage.empty')">
                  <a-button type="primary" :disabled="!dgId" @click="openBind">
                    <PlusOutlined />
                    {{ t('devgFlowPage.add') }}
                  </a-button>
                </a-empty>
              </template>
            </a-table>
          </template>
        </TableScrollWrap>
      </div>
    </a-card>

    <EquipBindPickerModal v-model:open="bindOpen" :exclude-ids="boundIds" @confirm="onBindConfirm" />
    <EquipProcessModal v-model:open="processOpen" :dg-id="dgId" :record="editingEquip" @success="loadList" />
  </div>
</template>

<style scoped>
.mat-tag {
  margin-inline-end: 6px;
  margin-bottom: 4px;
}
</style>
