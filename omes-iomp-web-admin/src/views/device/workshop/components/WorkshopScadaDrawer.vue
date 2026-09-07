<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { WorkshopNode } from '@/api/device'
import {
  fetchWorkshopScadaConfig,
  fetchWorkshopScadaServers,
  saveWorkshopScadaConfig,
} from '@/api/workshop-config'
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
const serverOptions = ref<{ value: string; label: string }[]>([])

const formState = reactive({
  server: undefined as string | undefined,
  privateKey: '',
  privateSecret: '',
  mapCode: '',
  interval: 5,
})

async function loadMeta() {
  const servers = await fetchWorkshopScadaServers()
  serverOptions.value = (servers || []).map((s) => ({ value: s, label: s }))
}

async function loadConfig() {
  if (!props.workshop?.id) {
    return
  }
  loading.value = true
  try {
    await loadMeta()
    const dto = await fetchWorkshopScadaConfig(props.workshop.id)
    const cfg = dto?.scadaConfig
    formState.server = cfg?.server
    formState.privateKey = cfg?.privateKey || ''
    formState.privateSecret = cfg?.privateSecret || ''
    formState.mapCode = cfg?.mapCode || ''
    formState.interval = cfg?.interval ?? 5
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!props.workshop?.id) {
    return
  }
  if (!formState.server || !formState.mapCode?.trim()) {
    message.warning(t('workshopScadaPage.requiredHint'))
    return
  }
  saving.value = true
  try {
    await saveWorkshopScadaConfig({
      workshopId: props.workshop.id,
      scadaConfig: {
        server: formState.server,
        privateKey: formState.privateKey.trim(),
        privateSecret: formState.privateSecret.trim(),
        mapCode: formState.mapCode.trim(),
        interval: Number(formState.interval) || 5,
      },
    })
    message.success(t('workshopScadaPage.saveSuccess'))
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
    :title="t('workshopScadaPage.title', { name: workshop?.name || '' })"
    width="560px"
    centered
    destroy-on-close
    :mask-closable="false"
    class="workshop-config-modal scada-modal"
    @update:open="emit('update:open', $event)"
    @cancel="emit('update:open', false)"
  >
    <a-spin :spinning="loading">
      <div class="config-panel">
        <p class="panel-desc">{{ t('workshopScadaPage.desc') }}</p>
        <a-form layout="vertical" class="scada-form">
          <a-form-item :label="t('workshopScadaPage.server')" required>
            <a-select
              v-model:value="formState.server"
              allow-clear
              :placeholder="t('workshopScadaPage.serverPlaceholder')"
              :options="serverOptions"
            />
          </a-form-item>
          <a-form-item label="Key" required>
            <a-input v-model:value="formState.privateKey" allow-clear />
          </a-form-item>
          <a-form-item label="Secret" required>
            <a-input v-model:value="formState.privateSecret" allow-clear />
          </a-form-item>
          <a-form-item :label="t('workshopScadaPage.mapCode')" required>
            <a-input v-model:value="formState.mapCode" allow-clear />
          </a-form-item>
          <a-form-item :label="t('workshopScadaPage.interval')" required>
            <a-input-number
              v-model:value="formState.interval"
              :min="1"
              :max="3600"
              class="interval-input"
            />
          </a-form-item>
        </a-form>
      </div>
    </a-spin>

    <template #footer>
      <a-space>
        <a-button @click="emit('update:open', false)">{{ t('workshopScadaPage.cancel') }}</a-button>
        <a-button @click="loadConfig">{{ t('workshopScadaPage.reset') }}</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">
          {{ t('workshopScadaPage.save') }}
        </a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<style scoped>
.config-panel {
  padding: 4px 2px;
}

.panel-desc {
  margin: 0 0 20px;
  padding: 12px 14px;
  font-size: 13px;
  color: var(--omes-color-text-tertiary);
  line-height: 1.6;
  background: #f6f8fa;
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
}

.scada-form :deep(.ant-form-item-label > label) {
  font-weight: 600;
}

.interval-input {
  width: 100%;
}
</style>
