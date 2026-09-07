<script setup lang="ts">
import CompactSearchActions from '@/components/admin/CompactSearchActions.vue'
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  ApiOutlined,
  GatewayOutlined,
  InboxOutlined,
  LinkOutlined,
  ReloadOutlined, TeamOutlined,
} from '@ant-design/icons-vue'
import { fetchEquipPage, type EquipRecord } from '@/api/device'
import { fetchGatewayById, type GatewayRecord } from '@/api/gateway'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'

const props = defineProps<{
  gatewayId: string
  gatewayName?: string
  gateway?: GatewayRecord | null
}>()

const { t } = useI18n()

// gatewayPage.bindingHint: 展示通过设备实时配置关联网关的设备列表；绑定关系在设备属性/实时配置中维护。系统管理端说明文案不向普通用户展示。

const loading = ref(false)
const gatewayLoading = ref(false)
const gatewayInfo = ref<GatewayRecord | null>(null)
const dataSource = ref<EquipRecord[]>([])
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

const searchForm = reactive({
  name: '',
})

const displayGateway = computed(() => gatewayInfo.value || props.gateway || null)

const displayName = computed(
  () => displayGateway.value?.serverName || props.gatewayName || props.gatewayId || '-',
)

const columns = computed(() => [
  { title: t('gatewayPage.equipName'), dataIndex: 'name', key: 'name', ellipsis: true, minWidth: 160 },
  { title: t('gatewayPage.equipCode'), dataIndex: 'selfCode', key: 'selfCode', width: 140 },
  { title: t('gatewayPage.equipType'), dataIndex: 'typeDesc', key: 'typeDesc', width: 120 },
  { title: t('gatewayPage.workshopName'), key: 'workshop', width: 160, ellipsis: true },
  { title: 'ID', dataIndex: 'id', key: 'id', width: 120, ellipsis: true },
])

async function loadGateway() {
  if (props.gateway) {
    gatewayInfo.value = props.gateway
    return
  }
  if (!props.gatewayId) {
    gatewayInfo.value = null
    return
  }
  gatewayLoading.value = true
  try {
    gatewayInfo.value = await fetchGatewayById(props.gatewayId)
  } catch {
    gatewayInfo.value = { id: props.gatewayId, serverName: props.gatewayName }
  } finally {
    gatewayLoading.value = false
  }
}

async function loadTable() {
  if (!props.gatewayId) {
    dataSource.value = []
    pagination.total = 0
    return
  }
  loading.value = true
  try {
    const result = await fetchEquipPage({
      page: pagination.current,
      pageSize: pagination.pageSize,
      gwId: props.gatewayId,
      name: searchForm.name?.trim() || undefined,
      queryWorkshop: true,
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
  onSearch()
}

function onPageChange(page: number, pageSize: number) {
  pagination.current = page
  pagination.pageSize = pageSize
  loadTable()
}

function onPageSizeChange(_page: number, pageSize: number) {
  pagination.current = 1
  pagination.pageSize = pageSize
  loadTable()
}

watch(
  () => [props.gatewayId, props.gateway] as const,
  async ([id]) => {
    if (!id) {
      return
    }
    pagination.current = 1
    await loadGateway()
    await loadTable()
  },
  { immediate: true },
)
</script>

<template>
  <div class="binding-panel">
    <div class="binding-panel__fixed">
    <a-spin :spinning="gatewayLoading">
      <div class="gateway-hero" :class="{ 'gateway-hero--on': displayGateway?.enabled }">
        <div class="gateway-hero__icon">
          <GatewayOutlined />
        </div>
        <div class="gateway-hero__main">
          <div class="gateway-hero__top">
            <h3 class="gateway-hero__name">{{ displayName }}</h3>
            <a-space size="small" wrap>
              <a-tag v-if="displayGateway?.protocol" color="blue">{{ displayGateway.protocol }}</a-tag>
              <a-badge
                :status="displayGateway?.enabled ? 'processing' : 'default'"
                :text="displayGateway?.enabled ? t('gatewayPage.enabled') : t('gatewayPage.disabled')"
              />
            </a-space>
          </div>
          <div v-if="displayGateway?.uri" class="gateway-hero__uri">
            <LinkOutlined />
            <span :title="displayGateway.uri">{{ displayGateway.uri }}</span>
          </div>
        </div>
        <div v-if="pagination.total > 0 || !loading" class="gateway-hero__stat">
          <div class="stat-value">{{ pagination.total }}</div>
          <div class="stat-label">{{ t('gatewayPage.bindingStatLabel') }}</div>
        </div>
      </div>
    </a-spin>

    <div class="search-toolbar search-toolbar--compact">
      <a-form layout="inline" class="search-form" :model="searchForm" @finish="onSearch">
        <a-form-item :label="t('gatewayPage.equipName')" name="name">
          <a-input size="small"
            v-model:value="searchForm.name"
            allow-clear
            class="search-input"
            :placeholder="t('gatewayPage.equipNamePlaceholder')"
          />
        </a-form-item>
        <CompactSearchActions
          :query-title="t('gatewayPage.query')"
          :reset-title="t('gatewayPage.reset')"
          @reset="onReset"
        >
          <a-tooltip :title="t('gatewayPage.refresh')">
            <a-button size="small" @click="loadTable">
              <ReloadOutlined />
            </a-button>
          </a-tooltip>
        </CompactSearchActions>
      </a-form>
    </div>
    </div>

    <a-card size="small" class="table-card binding-panel__table">
      <template #title>
        <span class="section-title">
          <TeamOutlined />
          {{ t('gatewayPage.bindingListTitle') }}
        </span>
      </template>
      <template #extra>
        <a-tag v-if="pagination.total" color="processing">
          {{ t('gatewayPage.bindingTotal', { total: pagination.total }) }}
        </a-tag>
      </template>

      <TableScrollWrap :min-height="280" :refresh-keys="[dataSource.length, pagination.total]">
        <template #default="{ scrollY }">
          <a-table
            row-key="id"
            class="scroll-table binding-table"
            :columns="columns"
            :data-source="dataSource"
            :loading="loading"
            :pagination="false"
            :scroll="{ y: scrollY }"
          >
        <template #emptyText>
          <div class="table-empty">
            <InboxOutlined class="table-empty__icon" />
            <p class="table-empty__title">{{ t('gatewayPage.bindingEmpty') }}</p>
            <p class="table-empty__hint">{{ t('gatewayPage.bindingEmptyHint') }}</p>
          </div>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <span class="equip-name">{{ record.name || '—' }}</span>
          </template>
          <template v-else-if="column.key === 'selfCode'">
            <span class="equip-code">{{ record.selfCode || '—' }}</span>
          </template>
          <template v-else-if="column.key === 'typeDesc'">
            <a-tag v-if="record.typeDesc" class="type-tag">{{ record.typeDesc }}</a-tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'workshop'">
            <a-tag v-if="record.workshop?.name" color="geekblue" class="workshop-tag">
              <ApiOutlined />
              {{ record.workshop.name }}
            </a-tag>
            <span v-else class="text-muted">-</span>
          </template>
          <template v-else-if="column.key === 'id'">
            <span class="id-cell" :title="record.id">{{ record.id }}</span>
          </template>
        </template>
          </a-table>
          <div class="table-pagination-bar">
            <a-pagination
              v-model:current="pagination.current"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              show-size-changer
              :show-total="(total: number) => t('gatewayPage.bindingTotal', { total })"
              @change="onPageChange"
              @show-size-change="onPageSizeChange"
            />
          </div>
        </template>
      </TableScrollWrap>
    </a-card>
  </div>
</template>

<style scoped>
.binding-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-height: 520px;
  max-height: calc(100vh - 180px);
}

.binding-panel__fixed {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.binding-panel__table {
  flex: 1;
  min-height: 320px;
  display: flex;
  flex-direction: column;
}

.binding-panel__table :deep(.table-scroll-wrap) {
  flex: 1;
  min-height: 280px;
}

.gateway-hero {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #f0f5ff 0%, var(--omes-color-bg-elevated) 55%, #fff 100%);
  border: 1px solid var(--omes-color-primary-border);
  border-radius: 12px;
  transition: border-color 0.2s;
}

.gateway-hero--on {
  background: linear-gradient(135deg, #f6ffed 0%, var(--omes-color-bg-elevated) 55%, #fff 100%);
  border-color: #b7eb8f;
}

.gateway-hero__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  font-size: 26px;
  color: var(--omes-color-primary);
  background: var(--omes-color-bg-container);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.12);
  flex-shrink: 0;
}

.gateway-hero--on .gateway-hero__icon {
  color: var(--omes-color-success);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.15);
}

.gateway-hero__main {
  flex: 1;
  min-width: 0;
}

.gateway-hero__top {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.gateway-hero__name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.gateway-hero__uri {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--omes-color-text-secondary);
}

.gateway-hero__uri span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.gateway-hero__stat {
  flex-shrink: 0;
  min-width: 72px;
  padding: 10px 14px;
  text-align: center;
  background: var(--omes-color-bg-container);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-lg);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--omes-color-primary);
}

.gateway-hero--on .stat-value {
  color: var(--omes-color-success);
}

.stat-label {
  margin-top: 2px;
  font-size: 11px;
  color: var(--omes-color-text-quaternary);
}

.search-toolbar {
  padding: 12px 16px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
}

.search-form {
  margin-bottom: 0;
}

.search-input {
  width: 240px;
}

.table-card {
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
}

.table-card :deep(.ant-card-head) {
  flex-shrink: 0;
  min-height: 48px;
  border-bottom: 1px solid var(--omes-color-border);
}

.table-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0 12px 12px;
  overflow: hidden;
}

.table-pagination-bar {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  padding-top: 4px;
  border-top: 1px solid var(--omes-color-border);
}

.section-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.section-title::before {
  content: '';
  width: 3px;
  height: 14px;
  margin-right: 4px;
  background: var(--omes-color-primary);
  border-radius: 2px;
}

.section-title :deep(.anticon) {
  color: var(--omes-color-primary);
}

.binding-table :deep(.ant-table) {
  border-radius: var(--omes-radius-md);
}

.binding-table :deep(.ant-table-thead > tr > th) {
  background: var(--omes-color-bg-elevated);
  font-weight: 600;
  color: var(--omes-color-text-label);
}

.binding-table :deep(.ant-table-tbody > tr:hover > td) {
  background: var(--omes-color-primary-bg-hover);
}

.equip-name {
  font-weight: 500;
  color: var(--omes-color-text);
}

.equip-code {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: var(--omes-color-text-secondary);
}

.type-tag {
  margin: 0;
  border-radius: 4px;
}

.workshop-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 0;
  max-width: 100%;
}

.id-cell {
  font-family: Consolas, Monaco, monospace;
  font-size: 11px;
  color: var(--omes-color-text-quaternary);
}

.text-muted {
  color: var(--omes-color-text-placeholder);
}

.table-empty {
  padding: 32px 16px;
  text-align: center;
}

.table-empty__icon {
  font-size: 48px;
  color: rgba(0, 0, 0, 0.15);
}

.table-empty__title {
  margin: 12px 0 4px;
  font-size: 15px;
  font-weight: 500;
  color: var(--omes-color-text-secondary);
}

.table-empty__hint {
  margin: 0;
  font-size: 13px;
  color: var(--omes-color-text-quaternary);
}

@media (max-width: 768px) {
  .gateway-hero {
    flex-wrap: wrap;
  }

  .gateway-hero__stat {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .stat-value {
    font-size: 20px;
  }

  .search-input {
    width: 100%;
  }
}
</style>
