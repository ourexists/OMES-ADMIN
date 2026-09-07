<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  CalendarOutlined,
  EnvironmentOutlined,
  FileTextOutlined,
  InfoCircleOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import type { InspectPlanRecord } from '@/api/inspect-plan'
import { fetchInspectPlanById, INSPECT_PLAN_CYCLE_TYPES, saveInspectPlan } from '@/api/inspect-plan'
import { fetchInspectTemplateList } from '@/api/inspect-template'
import type { InspectTemplateRecord } from '@/api/inspect-template'
import { fetchWorkshopTree } from '@/api/device'
import { buildWorkshopTreeIndex } from '@/utils/workshop-tree'
import WorkshopMultiPickerModal from './WorkshopMultiPickerModal.vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: InspectPlanRecord | null
  viewOnly?: boolean
  presetTemplateId?: string
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const saving = ref(false)
const loading = ref(false)
const templateOptions = ref<InspectTemplateRecord[]>([])
const workshopPickerOpen = ref(false)
const workshopDisplayNames = ref<string[]>([])

const formState = reactive({
  id: '' as string | undefined,
  name: '',
  templateId: undefined as string | undefined,
  cycleType: 1 as number | undefined,
  cycleConfig: '',
  workshopCode: '' as string | undefined,
  remark: '',
})

const isEdit = computed(() => Boolean(formState.id))
const lockedByStatus = ref(false)
const readOnly = computed(() => props.viewOnly === true || lockedByStatus.value)
const modalTitle = computed(() => {
  if (readOnly.value) {
    return t('inspectPlanPage.formView')
  }
  return isEdit.value ? t('inspectPlanPage.formEdit') : t('inspectPlanPage.formAdd')
})

const cycleOptions = computed(() =>
  Object.entries(INSPECT_PLAN_CYCLE_TYPES).map(([value, label]) => ({
    value: Number(value),
    label: t(`inspectPlanPage.cycleType.${label}`, label),
  })),
)

const hasWorkshopSelection = computed(() => workshopDisplayNames.value.length > 0)

async function resolveWorkshopNames(codesStr?: string) {
  const raw = codesStr?.trim()
  if (!raw) {
    workshopDisplayNames.value = []
    return
  }
  const codes = raw.split(',').map((s) => s.trim()).filter(Boolean)
  const tree = await fetchWorkshopTree()
  const nodes = Array.isArray(tree) ? tree : []
  const { nodeByCode } = buildWorkshopTreeIndex(nodes)
  workshopDisplayNames.value = codes.map((code) => nodeByCode.get(code)?.name || code)
}

async function loadTemplateOptions() {
  const list = await fetchInspectTemplateList()
  templateOptions.value = Array.isArray(list) ? list : []
}

function resetForm() {
  formState.id = undefined
  formState.name = ''
  formState.templateId = props.presetTemplateId || undefined
  formState.cycleType = 1
  formState.cycleConfig = ''
  formState.workshopCode = ''
  formState.remark = ''
  workshopDisplayNames.value = []
}

async function loadDetail(id: string) {
  loading.value = true
  try {
    const data = await fetchInspectPlanById(id)
    if (!data) {
      return
    }
    formState.id = data.id
    formState.name = data.name || ''
    formState.templateId = data.templateId || undefined
    formState.cycleType = data.cycleType ?? 1
    formState.cycleConfig = data.cycleConfig || ''
    formState.workshopCode = data.workshopCode || ''
    formState.remark = data.remark || ''
    lockedByStatus.value = data.status === 1
    await resolveWorkshopNames(formState.workshopCode)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  async (visible) => {
    if (!visible) {
      return
    }
    await loadTemplateOptions()
    if (props.record?.id) {
      await loadDetail(props.record.id)
      return
    }
    lockedByStatus.value = false
    resetForm()
  },
)

function closeModal() {
  emit('update:open', false)
}

function onWorkshopPicked(codes: string, names: string[]) {
  formState.workshopCode = codes || undefined
  workshopDisplayNames.value = names
}

function clearWorkshops() {
  if (readOnly.value) {
    return
  }
  formState.workshopCode = ''
  workshopDisplayNames.value = []
}

async function onSubmit() {
  if (readOnly.value) {
    closeModal()
    return
  }
  const name = formState.name.trim()
  if (!name) {
    message.warning(t('inspectPlanPage.nameRequired'))
    return
  }
  if (!formState.templateId) {
    message.warning(t('inspectPlanPage.templateRequired'))
    return
  }
  if (!formState.cycleType) {
    message.warning(t('inspectPlanPage.cycleTypeRequired'))
    return
  }
  const cycleConfig = formState.cycleConfig.trim()
  if (!cycleConfig) {
    message.warning(t('inspectPlanPage.cycleConfigRequired'))
    return
  }

  saving.value = true
  try {
    const workshopCode = formState.workshopCode?.trim() || undefined
    await saveInspectPlan({
      id: formState.id,
      name,
      templateId: formState.templateId,
      cycleType: formState.cycleType,
      cycleConfig,
      workshopCode,
      remark: formState.remark.trim() || undefined,
    })
    message.success(t('inspectPlanPage.saveSuccess'))
    emit('success')
    closeModal()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <a-modal
    :open="open"
    width="680px"
    destroy-on-close
    class="inspect-form-modal"
    :footer="readOnly ? undefined : null"
    @cancel="closeModal"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon">
          <CalendarOutlined />
        </span>
        {{ modalTitle }}
      </span>
    </template>

    <a-spin :spinning="loading">
      <div v-if="lockedByStatus && !props.viewOnly" class="inspect-form-alert">
        <InfoCircleOutlined />
        <span>{{ t('inspectPlanPage.enabledEditBlocked') }}</span>
      </div>

      <a-form layout="vertical" class="plan-form">
        <div class="inspect-form-section">
          <div class="inspect-form-section__title">
            <SettingOutlined />
            {{ t('inspectPlanPage.formBasic') }}
          </div>
          <a-row :gutter="16">
            <a-col :span="24">
              <a-form-item :label="t('inspectPlanPage.colTemplate')" required>
                <a-select
                  v-model:value="formState.templateId"
                  :disabled="readOnly"
                  allow-clear
                  show-search
                  option-filter-prop="label"
                  :placeholder="t('inspectPlanPage.templatePlaceholder')"
                  :options="templateOptions.map((item) => ({ value: item.id, label: item.name || item.id }))"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item :label="t('inspectPlanPage.colName')" required>
                <a-input
                  v-model:value="formState.name"
                  :disabled="readOnly"
                  :placeholder="t('inspectPlanPage.namePlaceholder')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('inspectPlanPage.colCycleType')" required>
                <a-select
                  v-model:value="formState.cycleType"
                  :disabled="readOnly"
                  :options="cycleOptions"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('inspectPlanPage.colCycleConfig')" required>
                <a-input
                  v-model:value="formState.cycleConfig"
                  :disabled="readOnly"
                  :placeholder="t('inspectPlanPage.cycleConfigPlaceholder')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <p class="inspect-form-hint">{{ t('inspectPlanPage.cycleConfigHint') }}</p>
            </a-col>
          </a-row>
        </div>

        <div class="inspect-form-section">
          <div class="inspect-form-section__title">
            <EnvironmentOutlined />
            {{ t('inspectPlanPage.formScope') }}
          </div>
          <a-form-item :label="t('inspectPlanPage.colWorkshop')">
            <div
              class="inspect-workshop-box"
              :class="{ 'is-empty': !hasWorkshopSelection }"
            >
              <template v-if="hasWorkshopSelection">
                <a-tag v-for="name in workshopDisplayNames" :key="name" color="blue">
                  {{ name }}
                </a-tag>
              </template>
              <template v-else>
                {{ t('inspectPlanPage.workshopAllHint') }}
              </template>
            </div>
            <div v-if="!readOnly" class="inspect-workshop-actions">
              <a-space>
                <a-button size="small" @click="workshopPickerOpen = true">
                  {{ t('inspectPlanPage.selectWorkshop') }}
                </a-button>
                <a-button v-if="hasWorkshopSelection" size="small" type="link" @click="clearWorkshops">
                  {{ t('inspectPlanPage.clearWorkshop') }}
                </a-button>
              </a-space>
            </div>
            <p class="inspect-form-hint">{{ t('inspectPlanPage.workshopHint') }}</p>
          </a-form-item>
        </div>

        <div class="inspect-form-section">
          <div class="inspect-form-section__title">
            <FileTextOutlined />
            {{ t('inspectPlanPage.formRemark') }}
          </div>
          <a-form-item :label="t('inspectPlanPage.colRemark')">
            <a-textarea
              v-model:value="formState.remark"
              :disabled="readOnly"
              :rows="3"
              :placeholder="t('inspectPlanPage.remarkPlaceholder')"
            />
          </a-form-item>
        </div>
      </a-form>
    </a-spin>

    <template v-if="!readOnly" #footer>
      <a-button @click="closeModal">{{ t('inspectPlanPage.cancel') }}</a-button>
      <a-button type="primary" :loading="saving" @click="onSubmit">
        {{ t('inspectPlanPage.save') }}
      </a-button>
    </template>
    <template v-else #footer>
      <a-button type="primary" @click="closeModal">{{ t('inspectPlanPage.close') }}</a-button>
    </template>

    <WorkshopMultiPickerModal
      v-model:open="workshopPickerOpen"
      :selected-codes="formState.workshopCode"
      @confirm="onWorkshopPicked"
    />
  </a-modal>
</template>

<style scoped>
.plan-form {
  margin-top: 0;
}
</style>
