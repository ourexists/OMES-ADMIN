<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { BarcodeOutlined, FolderOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import type { BomClassifyNode } from '@/api/bom'
import { saveBomClassify } from '@/api/bom'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: Partial<BomClassifyNode> | null
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
  pcode: '' as string | undefined,
  code: '' as string | undefined,
})

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() =>
  isEdit.value ? t('bomPage.classifyFormEdit') : t('bomPage.classifyFormAdd'),
)
const modeTag = computed(() =>
  isEdit.value ? t('bomPage.formModeEdit') : t('bomPage.formModeAdd'),
)

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    const record = props.record
    formState.id = record?.id || ''
    formState.name = record?.name || ''
    formState.selfCode = record?.selfCode || ''
    formState.pcode = record?.pcode
    formState.code = record?.code
  },
)

function closeModal() {
  emit('update:open', false)
}

async function handleSubmit() {
  const name = formState.name.trim()
  const selfCode = formState.selfCode.trim()
  if (!name) {
    message.warning(t('bomPage.classifyNameRequired'))
    return
  }
  if (!selfCode) {
    message.warning(t('bomPage.classifyCodeRequired'))
    return
  }
  saving.value = true
  try {
    await saveBomClassify({
      id: formState.id || undefined,
      name,
      selfCode,
      pcode: formState.pcode,
      code: formState.code,
    })
    message.success(t('bomPage.saveSuccess'))
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
    width="480px"
    destroy-on-close
    class="bom-classify-form-modal"
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

    <a-form layout="vertical" class="classify-form">
      <div class="form-section">
        <div class="section-title">
          <FolderOutlined />
          {{ t('bomPage.classifyTitle') }}
        </div>

        <a-form-item :label="t('bomPage.classifyName')" required>
          <a-input
            v-model:value="formState.name"
            allow-clear
            :placeholder="t('bomPage.classifyNamePlaceholder')"
            @press-enter="handleSubmit"
          >
            <template #prefix>
              <FolderOutlined class="input-prefix-icon" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item :label="t('bomPage.classifyCode')" required>
          <a-input
            v-model:value="formState.selfCode"
            allow-clear
            :disabled="isEdit"
            :placeholder="t('bomPage.classifyCodePlaceholder')"
            @press-enter="handleSubmit"
          >
            <template #prefix>
              <BarcodeOutlined class="input-prefix-icon" />
            </template>
          </a-input>
          <div v-if="isEdit" class="field-hint">
            <InfoCircleOutlined />
            {{ t('bomPage.classifyCodeEditHint') }}
          </div>
        </a-form-item>
      </div>
    </a-form>

    <div class="modal-footer">
      <a-button @click="closeModal">{{ t('bomPage.cancel') }}</a-button>
      <a-button type="primary" :loading="saving" @click="handleSubmit">
        {{ t('bomPage.save') }}
      </a-button>
    </div>
  </a-modal>
</template>

<style scoped>
.bom-classify-form-modal :deep(.ant-modal-body) {
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

.classify-form {
  padding-bottom: 4px;
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

.classify-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: var(--omes-color-text-label);
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
