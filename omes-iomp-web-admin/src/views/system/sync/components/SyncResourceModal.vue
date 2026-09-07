<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { DatabaseOutlined } from '@ant-design/icons-vue'
import { fetchSyncById } from '@/api/sync'
import type { SyncResourceRecord } from '@/types/sync'
import TableScrollWrap from '@/components/common/TableScrollWrap.vue'

const props = defineProps<{
  open: boolean
  syncId: string | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const { t } = useI18n()

const loading = ref(false)
const resources = ref<SyncResourceRecord[]>([])

const columns = computed(() => [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 180, ellipsis: true },
  { title: t('syncPage.resourceColPoint'), dataIndex: 'point', key: 'point', width: 140, ellipsis: true },
  { title: t('syncPage.resourceColStatus'), key: 'status', width: 100 },
  { title: t('syncPage.resourceColReq'), dataIndex: 'reqData', key: 'reqData', minWidth: 160, ellipsis: true },
  { title: t('syncPage.resourceColResp'), dataIndex: 'respData', key: 'respData', minWidth: 160, ellipsis: true },
  { title: t('syncPage.resourceColExcep'), dataIndex: 'excep', key: 'excep', width: 160, ellipsis: true },
])

function statusColor(status?: string): string {
  if (status === 'error') {
    return 'error'
  }
  if (status === 'end') {
    return 'success'
  }
  if (status === 'start') {
    return 'processing'
  }
  return 'default'
}

async function loadResources() {
  if (!props.syncId) {
    resources.value = []
    return
  }
  loading.value = true
  try {
    const detail = await fetchSyncById(props.syncId)
    resources.value = detail?.resources || []
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      loadResources()
    }
  },
)

function handleClose() {
  emit('update:open', false)
}
</script>

<template>
  <a-modal
    :open="open"
    :title="t('syncPage.resourceTitle')"
    width="960px"
    class="system-form-modal sync-resource-modal"
    :footer="null"
    destroy-on-close
    @cancel="handleClose"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon modal-title__icon--sync">
          <DatabaseOutlined />
        </span>
        {{ t('syncPage.resourceTitle') }}
      </span>
    </template>

    <TableScrollWrap :refresh-keys="[resources.length]" :min-height="280">
      <template #default="{ scrollY }">
        <a-table
          row-key="id"
          size="middle"
          bordered
          class="system-module-table scroll-table"
          :columns="columns"
          :data-source="resources"
          :pagination="false"
          :scroll="{ x: 900, y: scrollY }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="statusColor(record.status)">{{ record.status || '—' }}</a-tag>
            </template>
            <template v-else-if="column.key === 'reqData'">
              <a-tooltip v-if="record.reqData" :title="record.reqData">
                <span class="code-cell">{{ record.reqData }}</span>
              </a-tooltip>
              <span v-else class="muted-cell">-</span>
            </template>
            <template v-else-if="column.key === 'respData'">
              <a-tooltip v-if="record.respData" :title="record.respData">
                <span class="code-cell">{{ record.respData }}</span>
              </a-tooltip>
              <span v-else class="muted-cell">-</span>
            </template>
            <template v-else-if="column.key === 'excep'">
              <a-tooltip v-if="record.excep" :title="record.excep">
                <span class="code-cell code-cell--error">{{ record.excep }}</span>
              </a-tooltip>
              <span v-else class="muted-cell">-</span>
            </template>
          </template>

          <template #emptyText>
            <a-empty :description="t('syncPage.resourceEmpty')" />
          </template>
        </a-table>
      </template>
    </TableScrollWrap>
  </a-modal>
</template>

<style scoped>
.code-cell {
  display: inline-block;
  max-width: 100%;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12px;
  color: var(--omes-color-text-secondary);
}

.code-cell--error {
  color: #cf1322;
}

.muted-cell {
  color: var(--omes-color-text-quaternary);
}
</style>
