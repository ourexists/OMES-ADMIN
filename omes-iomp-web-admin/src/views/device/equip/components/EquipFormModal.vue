<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ApartmentOutlined, AppstoreOutlined, BarcodeOutlined } from '@ant-design/icons-vue'
import type { EquipRecord, ProductOption, WorkshopNode } from '@/api/device'
import { fetchEquipById, fetchProductListAll, saveEquip } from '@/api/device'
import type { ProductModelRecord } from '@/api/product-model'
import { fetchProductModels } from '@/api/product-model'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: EquipRecord | null
  workshop: WorkshopNode | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()

const loading = ref(false)
const typeOptions = ref<{ value: string; label: string }[]>([])
const modelOptions = ref<{ value: string; label: string }[]>([])

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() =>
  isEdit.value ? t('equipFormPage.titleEdit') : t('equipFormPage.titleAdd'),
)

const formState = reactive({
  name: '',
  selfCode: '',
  type: undefined as string | undefined,
  modelId: undefined as string | undefined,
  enableDate: '',
})

function resetForm() {
  formState.name = ''
  formState.selfCode = ''
  formState.type = undefined
  formState.modelId = undefined
  formState.enableDate = ''
  modelOptions.value = []
}

async function loadTypeOptions() {
  const list = await fetchProductListAll()
  const items = Array.isArray(list) ? list : []
  typeOptions.value = items.map((item: ProductOption) => ({
    value: String(item.code ?? item.id ?? ''),
    label: item.name || String(item.code ?? item.id ?? ''),
  }))
}

async function loadModelOptions(productCode?: string, keepModelId?: string) {
  const code = String(productCode || '').trim()
  if (!code) {
    modelOptions.value = []
    formState.modelId = undefined
    return
  }
  const list = (await fetchProductModels(code)) || []
  modelOptions.value = list.map((item: ProductModelRecord) => ({
    value: item.id,
    label: item.name ? `${item.name}（${item.code || item.id}）` : item.code || item.id,
  }))
  if (keepModelId && modelOptions.value.some((item) => item.value === keepModelId)) {
    formState.modelId = keepModelId
  } else {
    formState.modelId = undefined
  }
}

async function loadDetail(id: string) {
  loading.value = true
  try {
    const data = await fetchEquipById(id)
    formState.name = data.name || ''
    formState.selfCode = data.selfCode || ''
    formState.type = data.type != null ? String(data.type) : undefined
    formState.enableDate = data.enableDate?.substring(0, 10) || ''
    await loadModelOptions(formState.type, data.modelId)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      return
    }
    await loadTypeOptions()
    if (props.record?.id) {
      await loadDetail(props.record.id)
    } else {
      resetForm()
    }
  },
)

async function onProductChange(value?: string) {
  await loadModelOptions(value)
}

async function handleSubmit() {
  if (!formState.name.trim()) {
    message.warning(t('equipFormPage.nameRequired'))
    return
  }
  if (!formState.selfCode.trim()) {
    message.warning(t('equipFormPage.codeRequired'))
    return
  }
  if (!formState.type) {
    message.warning(t('equipFormPage.typeRequired'))
    return
  }
  if (!formState.modelId) {
    message.warning(t('equipFormPage.modelRequired'))
    return
  }
  if (!props.workshop?.selfCode && !isEdit.value) {
    message.warning(t('equipPage.selectWorkshop'))
    return
  }

  loading.value = true
  try {
    const payload: EquipRecord = {
      ...(props.record || { id: '' }),
      name: formState.name.trim(),
      selfCode: formState.selfCode.trim(),
      type: formState.type,
      modelId: formState.modelId,
      enableDate: formState.enableDate ? `${formState.enableDate} 00:00:00` : undefined,
      workshopCode: props.workshop?.selfCode || props.record?.workshopCode,
    }
    await saveEquip(payload)
    message.success(t('equipFormPage.saveSuccess'))
    emit('update:open', false)
    emit('success')
  } finally {
    loading.value = false
  }
}

function handleClose() {
  emit('update:open', false)
}
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    width="560px"
    centered
    destroy-on-close
    class="equip-form-modal"
    :mask-closable="false"
    :confirm-loading="loading"
    @update:open="emit('update:open', $event)"
    @cancel="handleClose"
    @ok="handleSubmit"
  >
    <template v-if="workshop?.name" #title>
      <span>{{ title }}</span>
      <a-tag color="processing" class="equip-form-modal__scene">
        <ApartmentOutlined />
        {{ workshop.name }}
      </a-tag>
    </template>

    <a-spin :spinning="loading">
      <a-form layout="vertical" class="equip-form">
        <div class="equip-form__grid">
          <a-form-item :label="t('equipPage.name')" required>
            <a-input v-model:value="formState.name" allow-clear :placeholder="t('equipPage.searchName')">
              <template #prefix>
                <AppstoreOutlined class="input-prefix" />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item :label="t('equipPage.code')" required>
            <a-input
              v-model:value="formState.selfCode"
              allow-clear
              :disabled="isEdit"
              :placeholder="t('equipPage.searchCode')"
            >
              <template #prefix>
                <BarcodeOutlined class="input-prefix" />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item :label="t('equipPage.type')" required>
            <a-select
              v-model:value="formState.type"
              allow-clear
              show-search
              option-filter-prop="label"
              :placeholder="t('equipFormPage.typePlaceholder')"
              :options="typeOptions"
              @change="onProductChange"
            />
          </a-form-item>
          <a-form-item :label="t('equipFormPage.model')" required>
            <a-select
              v-model:value="formState.modelId"
              allow-clear
              show-search
              option-filter-prop="label"
              :placeholder="t('equipFormPage.modelPlaceholder')"
              :options="modelOptions"
              :disabled="!formState.type"
            />
          </a-form-item>
          <a-form-item :label="t('equipFormPage.enableDate')" class="equip-form__span">
            <a-date-picker
              v-model:value="formState.enableDate"
              value-format="YYYY-MM-DD"
              class="w-full"
              allow-clear
            />
          </a-form-item>
        </div>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<style scoped>
.equip-form-modal :deep(.ant-modal-content) {
  border-radius: 12px;
}

.equip-form-modal :deep(.ant-modal-header) {
  padding: 14px 20px;
}

.equip-form-modal :deep(.ant-modal-title) {
  font-size: 16px;
}

.equip-form-modal :deep(.ant-modal-body) {
  padding: 12px 20px 4px;
}

.equip-form-modal :deep(.ant-modal-footer) {
  padding: 10px 20px 14px;
}

.equip-form-modal :deep(.ant-modal-footer .ant-btn) {
  min-width: 80px;
  height: 36px;
}

.equip-form-modal__scene {
  margin-left: 10px;
  vertical-align: middle;
  border-radius: 999px;
  font-weight: normal;
}

.equip-form__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 16px;
}

.equip-form__span {
  grid-column: 1 / -1;
}

.equip-form :deep(.ant-form-item) {
  margin-bottom: 12px;
}

.equip-form :deep(.ant-form-item-label) {
  padding-bottom: 2px;
}

.equip-form :deep(.ant-form-item-label > label) {
  height: 24px;
}

.input-prefix {
  color: var(--omes-color-text-tertiary);
}

.w-full {
  width: 100%;
}

@media (max-width: 560px) {
  .equip-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
