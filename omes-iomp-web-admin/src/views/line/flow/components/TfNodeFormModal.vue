<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  ApartmentOutlined,
  NodeIndexOutlined,
  PlusOutlined,
  SettingOutlined,
  ToolOutlined,
} from '@ant-design/icons-vue'
import { fetchTfById } from '@/api/line'
import type { TfEquipmentRef, TfRecord, TfToolingRef } from '@/types/line'
import { isStepEngineConfigured, summarizeStepScript } from '@/utils/process/processStepScript'
import ProcessStepScriptModal from '@/components/process/ProcessStepScriptModal.vue'
import EquipMultiPickerModal from './EquipMultiPickerModal.vue'
import ToolingMaterialPickerModal from './ToolingMaterialPickerModal.vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  lineId: string
  draft: TfRecord | null
  localMode?: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  saved: [record: TfRecord]
}>()

const { t } = useI18n()
const loading = ref(false)
const equipPickerOpen = ref(false)
const toolingPickerOpen = ref(false)
const engineModalOpen = ref(false)

const formState = reactive({
  id: '' as string | undefined,
  lineId: '',
  name: '',
  selfCode: '',
  stepNo: undefined as number | undefined,
  stepContent: '',
  stepScript: '',
  stepEngineConfig: '',
  equipments: [] as TfEquipmentRef[],
  toolings: [] as TfToolingRef[],
})

const isPersisted = computed(() => Boolean(formState.id && !String(formState.id).startsWith('tmp_')))
const codeDisabled = computed(() => isPersisted.value)
const modalTitle = computed(() => {
  const name = formState.name.trim()
  return name ? `${t('lineFlowPage.tfFormTitle')} · ${name}` : t('lineFlowPage.tfFormTitle')
})

const engineConfigured = computed(() =>
  isStepEngineConfigured(formState.stepScript, formState.equipments, formState.stepEngineConfig),
)

const engineSummary = computed(() => {
  const summary = summarizeStepScript(formState.stepScript, formState.equipments)
  return summary || t('lineFlowPage.tfEngineSummaryEmpty')
})

function applyRecord(record: TfRecord) {
  formState.id = record.id
  formState.lineId = record.lineId || props.lineId
  formState.name = record.name?.trim() || ''
  formState.selfCode = record.selfCode || ''
  formState.stepNo = record.stepNo
  formState.stepContent = record.stepContent?.trim() || ''
  formState.stepScript = record.stepScript || ''
  formState.stepEngineConfig = record.stepEngineConfig || ''
  formState.equipments = [...(record.equipments || [])]
  formState.toolings = [...(record.toolings || [])]
}

function onEngineScriptOk(json: string) {
  formState.stepScript = json || ''
}

async function fillForm() {
  const draft = props.draft
  if (!draft) {
    return
  }
  if (draft.__localOnly || String(draft.id).startsWith('tmp_')) {
    applyRecord(draft)
    return
  }
  loading.value = true
  try {
    const detail = await fetchTfById(draft.id)
    if (detail) {
      applyRecord(detail)
    }
  } finally {
    loading.value = false
  }
}

function removeEquipment(index: number) {
  formState.equipments.splice(index, 1)
}

function removeTooling(index: number) {
  formState.toolings.splice(index, 1)
}

async function onSubmit() {
  if (!formState.name.trim()) {
    message.warning(t('lineFlowPage.tfNameRequired'))
    return
  }
  if (!formState.selfCode.trim()) {
    message.warning(t('lineFlowPage.tfCodeRequired'))
    return
  }
  if (formState.stepNo == null || formState.stepNo <= 0) {
    message.warning(t('lineFlowPage.tfStepNoRequired'))
    return
  }
  const payload: TfRecord = {
    id: formState.id || `tmp_${Date.now()}`,
    lineId: props.lineId,
    name: formState.name.trim(),
    selfCode: formState.selfCode.trim(),
    stepNo: formState.stepNo,
    stepContent: formState.stepContent.trim() || undefined,
    stepScript: formState.stepScript.trim() || undefined,
    stepEngineConfig: formState.stepEngineConfig.trim() || undefined,
    equipments: formState.equipments,
    toolings: formState.toolings,
    __localOnly: !isPersisted.value,
    __dirtyLocal: true,
  }
  emit('saved', payload)
  emit('update:open', false)
}

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      return
    }
    await fillForm()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :confirm-loading="loading"
    width="640px"
    destroy-on-close
    class="system-form-modal line-tf-form-modal"
    @cancel="emit('update:open', false)"
    @ok="onSubmit"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon line-tf-form-modal__icon">
          <NodeIndexOutlined />
        </span>
        {{ modalTitle }}
      </span>
    </template>

    <a-spin :spinning="loading">
      <a-form layout="vertical" class="system-form line-tf-form">
        <div class="line-tf-form__identity">
          <div class="line-tf-form__step-badge">
            <span class="line-tf-form__step-label">{{ t('lineFlowPage.tfStepNo') }}</span>
            <a-input-number
              v-model:value="formState.stepNo"
              :min="1"
              :max="9999"
              :precision="0"
              :controls="false"
              class="line-tf-form__step-input"
              placeholder="10"
            />
          </div>
          <div class="line-tf-form__identity-fields">
            <a-form-item :label="t('lineFlowPage.tfName')" required class="line-tf-form__field">
              <a-input
                v-model:value="formState.name"
                allow-clear
                :placeholder="t('lineFlowPage.tfNamePlaceholder')"
              />
            </a-form-item>
            <a-form-item :label="t('lineFlowPage.tfCode')" required class="line-tf-form__field">
              <a-input
                v-model:value="formState.selfCode"
                :disabled="codeDisabled"
                allow-clear
                :placeholder="t('lineFlowPage.tfCodePlaceholder')"
              />
            </a-form-item>
          </div>
        </div>

        <div class="system-form-section">
          <div class="system-form-section__title">
            <NodeIndexOutlined />
            {{ t('lineFlowPage.tfSectionContent') }}
          </div>
          <a-form-item>
            <a-textarea
              v-model:value="formState.stepContent"
              :rows="4"
              :placeholder="t('lineFlowPage.tfContentPlaceholder')"
            />
          </a-form-item>
        </div>

        <div class="system-form-section">
          <div class="system-form-section__title">
            <ApartmentOutlined />
            {{ t('lineFlowPage.tfSectionResource') }}
          </div>

          <div class="line-tf-form__resource">
            <div class="line-tf-form__resource-head">
              <span class="line-tf-form__resource-label">
                <ToolOutlined />
                {{ t('lineFlowPage.tfEquipments') }}
              </span>
              <a-button type="link" size="small" @click="equipPickerOpen = true">
                <PlusOutlined />
                {{ t('lineFlowPage.pickEquipments') }}
              </a-button>
            </div>
            <div v-if="formState.equipments.length" class="line-tf-form__chip-list">
              <a-tag
                v-for="(item, index) in formState.equipments"
                :key="item.equipmentId || item.equipmentCode || index"
                closable
                class="line-tf-form__chip"
                @close="removeEquipment(index)"
              >
                {{ item.equipmentName || item.equipmentCode }}
              </a-tag>
            </div>
            <div v-else class="line-tf-form__empty">{{ t('lineFlowPage.resourceEmpty') }}</div>
          </div>

          <div class="line-tf-form__resource">
            <div class="line-tf-form__resource-head">
              <span class="line-tf-form__resource-label">
                <ToolOutlined />
                {{ t('lineFlowPage.tfToolings') }}
              </span>
              <a-button type="link" size="small" @click="toolingPickerOpen = true">
                <PlusOutlined />
                {{ t('lineFlowPage.pickToolings') }}
              </a-button>
            </div>
            <div v-if="formState.toolings.length" class="line-tf-form__chip-list">
              <a-tag
                v-for="(item, index) in formState.toolings"
                :key="item.toolingId || item.toolingCode || index"
                closable
                class="line-tf-form__chip line-tf-form__chip--tooling"
                @close="removeTooling(index)"
              >
                {{ item.toolingName || item.toolingCode }}
              </a-tag>
            </div>
            <div v-else class="line-tf-form__empty">{{ t('lineFlowPage.resourceEmpty') }}</div>
          </div>
        </div>

        <div class="system-form-section">
          <div class="system-form-section__title">
            <SettingOutlined />
            {{ t('lineFlowPage.tfSectionBasic') }}
          </div>
          <p class="line-tf-form__engine-hint">{{ t('lineFlowPage.tfSectionEngineHint') }}</p>
          <div class="line-tf-form__engine">
            <div class="line-tf-form__engine-summary">{{ engineSummary }}</div>
            <button
              type="button"
              class="line-tf-form__engine-btn"
              :class="{ 'is-configured': engineConfigured }"
              @click="engineModalOpen = true"
            >
              <SettingOutlined class="line-tf-form__engine-icon" />
              <span>{{ engineConfigured ? t('lineFlowPage.tfEngineConfigured') : t('lineFlowPage.tfEngineNotConfigured') }}</span>
            </button>
          </div>
        </div>
      </a-form>
    </a-spin>

    <EquipMultiPickerModal
      v-model:open="equipPickerOpen"
      :selected="formState.equipments"
      @confirm="(value) => (formState.equipments = value)"
    />
    <ToolingMaterialPickerModal
      v-model:open="toolingPickerOpen"
      :selected="formState.toolings"
      @confirm="(value) => (formState.toolings = value)"
    />
    <ProcessStepScriptModal
      v-model:open="engineModalOpen"
      :step-script="formState.stepScript"
      :step-engine-config="formState.stepEngineConfig"
      :step-id="formState.id || formState.selfCode"
      :step-name="formState.name"
      :step-no="formState.stepNo != null ? String(formState.stepNo) : ''"
      :step-equipments="formState.equipments"
      @ok="onEngineScriptOk"
    />
  </a-modal>
</template>

<style scoped>
.line-tf-form-modal__icon {
  background: var(--omes-gradient-accent-purple);
  box-shadow: var(--omes-shadow-purple-icon);
}

.line-tf-form :deep(.ant-form-item) {
  margin-bottom: 14px;
}

.line-tf-form :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}

.line-tf-form__full {
  width: 100%;
}

.line-tf-form :deep(.system-form-section__title .anticon) {
  color: var(--omes-color-accent-purple-from);
}

.line-tf-form__identity {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f9f0ff 0%, #f5f8ff 100%);
  border: 1px solid #e8d4ff;
  border-radius: var(--omes-radius-lg);
}

.line-tf-form__step-badge {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 72px;
  padding: 8px 6px;
  background: #fff;
  border: 1px solid #d3adf7;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.08);
}

.line-tf-form__step-label {
  margin-bottom: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #722ed1;
  letter-spacing: 0.02em;
}

.line-tf-form__step-input {
  width: 100%;
}

.line-tf-form__step-input :deep(.ant-input-number-input) {
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  color: #531dab;
  padding: 2px 4px;
}

.line-tf-form__identity-fields {
  flex: 1;
  min-width: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 12px;
}

.line-tf-form__field {
  margin-bottom: 0 !important;
}

.line-tf-form__engine-hint {
  margin: 0 0 10px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--omes-color-text-tertiary);
}

.line-tf-form__engine {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  background: var(--omes-color-bg-container);
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-md);
}

.line-tf-form__engine-summary {
  flex: 1;
  min-width: 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--omes-color-text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}

.line-tf-form__engine-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px dashed #d3adf7;
  border-radius: 8px;
  background: #faf5ff;
  color: #722ed1;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.line-tf-form__engine-btn:hover {
  border-color: #b37feb;
  background: #f9f0ff;
}

.line-tf-form__engine-btn.is-configured {
  border-style: solid;
  border-color: #95de64;
  background: #f6ffed;
  color: #389e0d;
}

.line-tf-form__engine-btn.is-configured:hover {
  border-color: #73d13d;
  background: #f6ffed;
}

.line-tf-form__engine-icon {
  font-size: 14px;
}

.line-tf-form__resource {
  padding: 10px 12px;
  margin-bottom: 10px;
  background: var(--omes-color-bg-container);
  border: 1px dashed var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-md);
}

.line-tf-form__resource:last-child {
  margin-bottom: 0;
}

.line-tf-form__resource-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.line-tf-form__resource-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--omes-color-text-secondary);
}

.line-tf-form__chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.line-tf-form__chip {
  margin: 0;
  padding: 2px 10px;
  border-radius: 999px;
  background: #f0f5ff;
  border-color: #adc6ff;
  color: #1d39c4;
}

.line-tf-form__chip--tooling {
  background: #f9f0ff;
  border-color: #d3adf7;
  color: #531dab;
}

.line-tf-form__empty {
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
  line-height: 1.5;
}

@media (max-width: 576px) {
  .line-tf-form__identity {
    flex-direction: column;
  }

  .line-tf-form__identity-fields {
    grid-template-columns: 1fr;
  }
}
</style>
