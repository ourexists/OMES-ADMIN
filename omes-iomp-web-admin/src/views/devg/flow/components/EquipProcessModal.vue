<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ClusterOutlined } from '@ant-design/icons-vue'
import { saveDevgEquipProcess } from '@/api/devg'
import type { MaterialRecord } from '@/api/material'
import type { DevgEquipRecord, EquipProcessMaterial } from '@/types/devg'
import MaterialPickerModal from './MaterialPickerModal.vue'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  dgId: string
  record?: DevgEquipRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()
const loading = ref(false)
const matPickerOpen = ref(false)
const equipName = ref('')
const equipCode = ref('')

const formState = reactive({
  processMaterials: [] as EquipProcessMaterial[],
})

const title = computed(() => t('devgFlowPage.processFormTitle'))

function toCapacity(value?: number | string | null) {
  if (value == null || value === '') {
    return undefined
  }
  const num = Number(value)
  return Number.isFinite(num) ? num : undefined
}

function resetForm() {
  equipName.value = ''
  equipCode.value = ''
  formState.processMaterials = []
}

function fillFromRecord(record?: DevgEquipRecord | null) {
  if (!record) {
    resetForm()
    return
  }
  equipName.value = record.name || ''
  equipCode.value = record.selfCode || ''
  formState.processMaterials = (record.processMaterials || []).map((item) => ({
    matCode: item.matCode,
    matName: item.matName,
    maxCapacity: toCapacity(item.maxCapacity),
  }))
}

function onMaterialsConfirm(records: MaterialRecord[]) {
  const prev = new Map(
    formState.processMaterials.map((item) => [item.matCode, toCapacity(item.maxCapacity)]),
  )
  formState.processMaterials = records
    .map((item) => ({
      matCode: item.selfCode,
      matName: item.name,
      maxCapacity: prev.get(item.selfCode),
    }))
    .filter((item) => item.matCode)
}

function removeMaterial(code?: string) {
  formState.processMaterials = formState.processMaterials.filter((item) => item.matCode !== code)
}

async function onSubmit() {
  if (!props.dgId || !props.record?.id) {
    return
  }
  loading.value = true
  try {
    await saveDevgEquipProcess({
      dgId: props.dgId,
      equipId: props.record.id,
      processMaterials: formState.processMaterials.map((item) => ({
        matCode: item.matCode,
        matName: item.matName,
        maxCapacity: toCapacity(item.maxCapacity),
      })),
    })
    message.success(t('devgFlowPage.saveSuccess'))
    emit('success')
    emit('update:open', false)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      return
    }
    fillFromRecord(props.record)
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :confirm-loading="loading"
    width="640px"
    destroy-on-close
    class="process-form-modal process-form-modal--devg"
    @cancel="emit('update:open', false)"
    @ok="onSubmit"
  >
    <template #title>
      <span class="modal-title">
        <span class="modal-title__icon">
          <ClusterOutlined />
        </span>
        {{ title }}
      </span>
    </template>
    <a-spin :spinning="loading">
      <p class="form-hint">{{ t('devgFlowPage.processFormHint') }}</p>
      <a-form layout="vertical" class="system-form">
        <a-form-item :label="t('devgFlowPage.devName')">
          <a-input :value="equipName" disabled />
        </a-form-item>
        <a-form-item :label="t('devgFlowPage.devCode')">
          <a-input :value="equipCode" disabled />
        </a-form-item>
        <a-form-item :label="t('devgFlowPage.material')">
          <div class="material-editor">
            <a-table
              v-if="formState.processMaterials.length"
              row-key="matCode"
              size="small"
              :pagination="false"
              :data-source="formState.processMaterials"
              :columns="[
                { title: t('materialPage.name'), key: 'name', ellipsis: true },
                { title: t('devgFlowPage.maxCapacity'), key: 'maxCapacity', width: 180 },
                { title: t('devgFlowPage.colAction'), key: 'action', width: 72, align: 'center' },
              ]"
            >
              <template #bodyCell="{ column, record, index }">
                <template v-if="column.key === 'name'">
                  {{ record.matName || record.matCode }}
                </template>
                <template v-else-if="column.key === 'maxCapacity'">
                  <a-input-number
                    v-model:value="formState.processMaterials[index].maxCapacity"
                    class="full-width"
                    :min="0"
                    :placeholder="t('devgFlowPage.capacityPlaceholder')"
                    :addon-after="t('devgFlowPage.capacityUnit')"
                  />
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" danger @click="removeMaterial(record.matCode)">
                    {{ t('devgPage.delete') }}
                  </a-button>
                </template>
              </template>
            </a-table>
            <a-button size="small" @click="matPickerOpen = true">
              {{ t('devgFlowPage.selectMaterial') }}
            </a-button>
          </div>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
  <MaterialPickerModal
    v-model:open="matPickerOpen"
    :selected="formState.processMaterials"
    @confirm="onMaterialsConfirm"
  />
</template>

<style scoped>
.full-width {
  width: 100%;
}

.form-hint {
  margin: 0 0 12px;
  color: var(--omes-color-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.material-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
