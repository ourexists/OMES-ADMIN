<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { WorkshopNode } from '@/api/device'
import { fetchWorkshopCollectConfig, saveWorkshopCollectConfig } from '@/api/workshop-config'
import { fetchGatewayList, type GatewayRecord } from '@/api/gateway'
import type { WorkshopCollectAttr } from '@/types/workshop-config'
import ConfigSpreadsheet, { type SpreadsheetColumn } from '@/components/config/ConfigSpreadsheet.vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  workshop: WorkshopNode | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const attrs = ref<WorkshopCollectAttr[]>([])
const gatewayOptions = ref<{ value: string; label: string }[]>([])
const jsonOpen = ref(false)
const jsonText = ref('')

function emptyRow(): WorkshopCollectAttr {
  return { needCollect: true }
}

const collectColumns = computed<SpreadsheetColumn[]>(() => [
  { key: 'name', title: t('workshopCollectPage.colName'), width: 130 },
  { key: 'map', title: t('workshopCollectPage.colMap'), width: 160 },
  {
    key: 'gwId',
    title: t('workshopCollectPage.colGateway'),
    width: 160,
    type: 'select',
    options: gatewayOptions.value,
    placeholder: t('workshopCollectPage.gatewayPlaceholder'),
  },
  { key: 'value', title: t('workshopCollectPage.colValue'), width: 110 },
  { key: 'unit', title: t('workshopCollectPage.colUnit'), width: 80 },
  { key: 'needCollect', title: t('workshopCollectPage.colCollect'), width: 72, type: 'checkbox' },
])

async function loadGateways() {
  const list = await fetchGatewayList({ requirePage: false })
  gatewayOptions.value = (list || [])
    .filter((g: GatewayRecord) => g.id)
    .map((g) => ({ value: g.id!, label: g.serverName || g.id! }))
}

async function loadConfig() {
  if (!props.workshop?.id) {
    return
  }
  loading.value = true
  try {
    await loadGateways()
    const dto = await fetchWorkshopCollectConfig(props.workshop.id)
    const rows = dto?.config?.attrs
    attrs.value = Array.isArray(rows) && rows.length ? rows.map((r) => ({ ...r })) : [emptyRow()]
  } finally {
    loading.value = false
  }
}

function openJson() {
  jsonText.value = JSON.stringify(attrs.value, null, 2)
  jsonOpen.value = true
}

function applyJson() {
  try {
    const parsed = JSON.parse(jsonText.value || '[]')
    const arr = Array.isArray(parsed) ? parsed : parsed?.attrs
    attrs.value = Array.isArray(arr) && arr.length ? arr : [emptyRow()]
    jsonOpen.value = false
    message.success(t('workshopCollectPage.jsonApplied'))
  } catch {
    message.error(t('workshopCollectPage.jsonInvalid'))
  }
}

function deleteRow(idx: number) {
  attrs.value.splice(idx, 1)
  if (!attrs.value.length) {
    attrs.value = [emptyRow()]
  }
}

async function handleSave() {
  if (!props.workshop?.id) {
    return
  }
  saving.value = true
  try {
    await saveWorkshopCollectConfig({
      workshopId: props.workshop.id,
      config: { attrs: attrs.value },
    })
    message.success(t('workshopCollectPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    saving.value = false
  }
}

watch(
  () => [props.open, props.workshop?.id] as const,
  ([open, id]) => {
    if (open && id) {
      loadConfig()
    }
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="t('workshopCollectPage.title', { name: workshop?.name || '' })"
    width="1100px"
    destroy-on-close
    :mask-closable="false"
    class="workshop-config-modal collect-modal"
    :styles="{ body: { maxHeight: '70vh', overflowY: 'auto', padding: '12px 20px 8px' } }"
    @update:open="emit('update:open', $event)"
    @cancel="emit('update:open', false)"
  >
    <a-spin :spinning="loading">
      <div class="config-panel">
        <div class="panel-head">
          <div>
            <h3 class="panel-title">{{ t('workshopCollectPage.sectionAttrs') }}</h3>
            <p class="panel-desc">{{ t('workshopCollectPage.sectionDesc') }}</p>
          </div>
          <a-space>
            <a-button size="small" @click="attrs.push(emptyRow())">
              {{ t('workshopCollectPage.addRow') }}
            </a-button>
            <a-button size="small" @click="openJson">{{ t('workshopCollectPage.jsonMode') }}</a-button>
          </a-space>
        </div>

        <div class="sheet-wrap">
          <ConfigSpreadsheet
            :columns="collectColumns"
            :rows="attrs as Record<string, unknown>[]"
            max-height="min(58vh, 480px)"
            @delete-row="deleteRow"
          />
        </div>
      </div>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="emit('update:open', false)">{{ t('workshopCollectPage.cancel') }}</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          {{ t('workshopCollectPage.save') }}
        </a-button>
      </a-space>
    </template>

    <a-modal
      v-model:open="jsonOpen"
      :title="t('workshopCollectPage.jsonMode')"
      width="720px"
      destroy-on-close
      @ok="applyJson"
    >
      <a-textarea v-model:value="jsonText" :rows="16" class="json-textarea" />
    </a-modal>
  </a-modal>
</template>

<style scoped>
.collect-modal :deep(.ant-modal-header) {
  margin-bottom: 0;
}

.config-panel {
  border: 1px solid var(--omes-color-border);
  border-radius: 12px;
  background: var(--omes-color-bg-elevated);
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  background: linear-gradient(180deg, #fafbff 0%, #fff 100%);
  border-bottom: 1px solid var(--omes-color-border);
}

.panel-title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
}

.panel-desc {
  margin: 0;
  font-size: 13px;
  color: var(--omes-color-text-quaternary);
}

.sheet-wrap {
  padding: 12px;
  background: var(--omes-color-bg-container);
}

.json-textarea {
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 13px;
}
</style>
