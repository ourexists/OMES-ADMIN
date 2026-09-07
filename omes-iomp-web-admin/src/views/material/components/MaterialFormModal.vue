<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { AppstoreOutlined, BarcodeOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import type { MaterialClassifyRecord, MaterialRecord } from '@/api/material'
import { saveMaterial } from '@/api/material'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: MaterialRecord | null
  classifies: MaterialClassifyRecord[]
  defaultClassifyCode?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const saving = ref(false)

const formState = reactive({
  id: '',
  name: '',
  selfCode: '',
  classifyCode: undefined as string | undefined,
})

const initialSnapshot = ref({
  name: '',
  selfCode: '',
  classifyCode: undefined as string | undefined,
})

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() => (isEdit.value ? t('materialPage.formEdit') : t('materialPage.formAdd')))
const modeTag = computed(() =>
  isEdit.value ? t('materialPage.formModeEdit') : t('materialPage.formModeAdd'),
)

const classifyOptions = computed(() =>
  props.classifies
    .filter((item) => item.selfCode)
    .map((item) => ({
      value: item.selfCode!,
      label: item.name ? `${item.name} (${item.selfCode})` : item.selfCode!,
    })),
)

const presetClassifyLabel = computed(() => {
  const code = props.defaultClassifyCode
  if (!code) {
    return ''
  }
  const item = props.classifies.find((row) => row.selfCode === code)
  return item?.name || code
})

const showPresetHint = computed(
  () => !isEdit.value && Boolean(props.defaultClassifyCode) && Boolean(presetClassifyLabel.value),
)

function snapshotForm() {
  initialSnapshot.value = {
    name: formState.name,
    selfCode: formState.selfCode,
    classifyCode: formState.classifyCode,
  }
}

function resetForm() {
  formState.name = initialSnapshot.value.name
  formState.selfCode = initialSnapshot.value.selfCode
  formState.classifyCode = initialSnapshot.value.classifyCode
}

function filterClassifyOption(input: string, option?: { label?: string }) {
  const label = option?.label ?? ''
  return label.toLowerCase().includes(input.trim().toLowerCase())
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    if (props.record) {
      formState.id = props.record.id || ''
      formState.name = props.record.name || ''
      formState.selfCode = props.record.selfCode || ''
      formState.classifyCode = props.record.classifyCode || undefined
    } else {
      formState.id = ''
      formState.name = ''
      formState.selfCode = ''
      formState.classifyCode = props.defaultClassifyCode || undefined
    }
    snapshotForm()
  },
)

function closeModal() {
  emit('update:open', false)
}

async function handleSubmit() {
  const name = formState.name.trim()
  const selfCode = formState.selfCode.trim()
  if (!name) {
    message.warning(t('materialPage.nameRequired'))
    return
  }
  if (!selfCode) {
    message.warning(t('materialPage.codeRequired'))
    return
  }
  saving.value = true
  try {
    await saveMaterial({
      id: formState.id || undefined,
      name,
      selfCode,
      classifyCode: formState.classifyCode,
    })
    message.success(t('materialPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <a-modal
    :open="open"
    width="520px"
    destroy-on-close
    class="material-form-modal"
    :footer="null"
    @cancel="closeModal"
    @update:open="emit('update:open', $event)"
  >
    <template #title>
      <div class="modal-title-wrap">
        <span class="modal-title">{{ title }}</span>
        <a-tag :color="isEdit ? 'processing' : 'success'">{{ modeTag }}</a-tag>
      </div>
    </template>

    <a-form layout="vertical" class="material-form">
      <a-alert
        v-if="showPresetHint"
        type="info"
        show-icon
        class="form-alert"
        :message="t('materialPage.classifyPresetHint', { name: presetClassifyLabel })"
      />

      <div class="form-section">
        <div class="section-title">
          <AppstoreOutlined />
          {{ t('materialPage.formBasic') }}
        </div>

        <a-form-item :label="t('materialPage.name')" required>
          <a-input
            v-model:value="formState.name"
            allow-clear
            :placeholder="t('materialPage.namePlaceholder')"
            @press-enter="handleSubmit"
          >
            <template #prefix>
              <AppstoreOutlined class="input-prefix-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item :label="t('materialPage.code')" required>
          <a-input
            v-model:value="formState.selfCode"
            allow-clear
            :disabled="isEdit"
            :placeholder="t('materialPage.codePlaceholder')"
            @press-enter="handleSubmit"
          >
            <template #prefix>
              <BarcodeOutlined class="input-prefix-icon" />
            </template>
          </a-input>
          <div v-if="isEdit" class="field-hint">
            <InfoCircleOutlined />
            {{ t('materialPage.codeEditHint') }}
          </div>
        </a-form-item>

        <a-form-item :label="t('materialPage.classify')">
          <a-select
            v-model:value="formState.classifyCode"
            allow-clear
            show-search
            :filter-option="filterClassifyOption"
            :placeholder="t('materialPage.classifyPlaceholder')"
            :options="classifyOptions"
            :not-found-content="t('materialPage.classifyEmpty')"
          />
          <div v-if="!classifyOptions.length" class="field-hint">
            {{ t('materialPage.classifyEmptyAddFirst') }}
          </div>
        </a-form-item>
      </div>
    </a-form>

    <div class="modal-footer">
      <a-button @click="resetForm">{{ t('materialPage.reset') }}</a-button>
      <a-button @click="closeModal">{{ t('materialPage.cancel') }}</a-button>
      <a-button type="primary" :loading="saving" @click="handleSubmit">
        {{ t('materialPage.save') }}
      </a-button>
    </div>
  </a-modal>
</template>

<style scoped>
.material-form-modal :deep(.ant-modal-body) {
  padding: 16px 24px 0;
}

.modal-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.material-form {
  padding-bottom: 4px;
}

.form-alert {
  margin-bottom: 14px;
  border-radius: var(--omes-radius-md);
}

.form-section {
  padding: 16px 18px;
  background: var(--omes-color-bg-elevated);
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-lg);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--omes-color-text);
}

.section-title::before {
  content: '';
  width: 3px;
  height: 16px;
  margin-right: 4px;
  background: var(--omes-color-primary);
  border-radius: 2px;
  flex-shrink: 0;
}

.section-title :deep(.anticon) {
  font-size: 15px;
  color: var(--omes-color-primary);
}

.material-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.material-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.material-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--omes-color-text-label);
}

.material-form :deep(.ant-input-disabled),
.material-form :deep(.ant-input-affix-wrapper-disabled) {
  background: var(--omes-color-bg-layout);
  color: var(--omes-color-text-secondary);
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

.field-hint {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--omes-color-text-quaternary);
}

.field-hint :deep(.anticon) {
  margin-top: 3px;
  flex-shrink: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding: 16px 0 8px;
  border-top: 1px solid var(--omes-color-border);
}
</style>
