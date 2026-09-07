<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { WorkshopNode } from '@/api/device'
import { saveWorkshop } from '@/api/device'
import BaiduMapPicker from '@/components/map/BaiduMapPicker.vue'
import { message } from 'ant-design-vue'

const { t } = useI18n()

const props = defineProps<{
  open: boolean
  record: WorkshopNode | null
  parentCode?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() => {
  if (isEdit.value) {
    return t('workshopPage.formEdit')
  }
  return props.parentCode ? t('workshopPage.formAddChild') : t('workshopPage.formAdd')
})

const formState = reactive({
  name: '',
  selfCode: '',
  lng: '',
  lat: '',
  address: '',
})

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    formState.name = props.record?.name || ''
    formState.selfCode = props.record?.selfCode || ''
    formState.lng = props.record?.lng != null ? String(props.record.lng) : ''
    formState.lat = props.record?.lat != null ? String(props.record.lat) : ''
    formState.address = props.record?.address || ''
  },
)

async function handleSubmit() {
  const name = formState.name.trim()
  const selfCode = formState.selfCode.trim()
  if (!name) {
    message.warning(t('workshopPage.nameRequired'))
    return
  }
  if (!selfCode) {
    message.warning(t('workshopPage.codeRequired'))
    return
  }
  const payload: Partial<WorkshopNode> = {
    ...(props.record || {}),
    name,
    selfCode,
    lng: formState.lng || undefined,
    lat: formState.lat || undefined,
    address: formState.address.trim() || undefined,
  }
  if (!isEdit.value && props.parentCode) {
    payload.pcode = props.parentCode
  }
  await saveWorkshop(payload)
  message.success(t('workshopPage.saveSuccess'))
  emit('update:open', false)
  emit('success')
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="min(960px, 96vw)"
    centered
    destroy-on-close
    class="workshop-form-modal"
    :mask-closable="false"
    @update:open="emit('update:open', $event)"
    @ok="handleSubmit"
  >
    <div class="workshop-form">
      <section class="workshop-form__panel workshop-form__panel--basic">
        <header class="workshop-form__panel-head">{{ t('workshopPage.formBasic') }}</header>
        <a-form layout="vertical" class="workshop-form__fields">
          <a-form-item :label="t('workshopPage.name')" required>
            <a-input v-model:value="formState.name" />
          </a-form-item>
          <a-form-item :label="t('workshopPage.code')" required>
            <a-input v-model:value="formState.selfCode" :disabled="isEdit" />
          </a-form-item>
          <p class="workshop-form__hint">{{ t('workshopPage.locationHint') }}</p>
        </a-form>
      </section>

      <section class="workshop-form__panel workshop-form__panel--map">
        <header class="workshop-form__panel-head">{{ t('workshopPage.locationInfo') }}</header>
        <BaiduMapPicker
          v-if="open"
          v-model:lat="formState.lat"
          v-model:lng="formState.lng"
          v-model:address="formState.address"
          compact
          :height="300"
        />
      </section>
    </div>
  </a-modal>
</template>

<style scoped>
.workshop-form-modal :deep(.ant-modal-body) {
  padding: 16px 24px 12px;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
}

.workshop-form {
  display: grid;
  grid-template-columns: minmax(0, 320px) minmax(0, 1fr);
  gap: 16px;
  align-items: stretch;
  min-height: 360px;
}

.workshop-form__panel {
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-md);
  background: var(--omes-color-bg-container);
  min-width: 0;
}

.workshop-form__panel-head {
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--omes-color-text);
  background: var(--omes-color-bg-elevated);
  border-bottom: 1px solid var(--omes-color-border);
  line-height: 1.5;
}

.workshop-form__panel--map {
  display: flex;
  flex-direction: column;
}

.workshop-form__panel--map :deep(.map-picker) {
  flex: 1;
  padding: 12px 14px 14px;
}

.workshop-form__fields {
  padding: 14px 16px 8px;
}

.workshop-form__hint {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--omes-color-text-quaternary);
}

@media (max-width: 768px) {
  .workshop-form {
    grid-template-columns: 1fr;
  }
}
</style>
