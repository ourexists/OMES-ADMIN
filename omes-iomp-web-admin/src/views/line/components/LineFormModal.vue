<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  BarcodeOutlined,
  ClockCircleOutlined,
  DashboardOutlined,
  NodeIndexOutlined,
  ProfileOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import { fetchLineById, saveLine } from '@/api/line'
import type { LineRecord } from '@/types/line'
import { message } from 'ant-design-vue'

const props = defineProps<{
  open: boolean
  record: LineRecord | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: []
}>()

const { t } = useI18n()
const loading = ref(false)

const formState = reactive({
  id: '' as string | undefined,
  name: '',
  selfCode: '',
  productCode: '',
  productName: '',
  materialCode: '',
  materialName: '',
  versionNo: '',
  throughput: undefined as number | undefined,
  stepInterval: undefined as number | undefined,
})

/** 编辑时保留原 PLC 字段，避免提交时被清空（下载等功能仍可用） */
const preservedPlc = reactive({
  type: 0 as number,
  mapDb: undefined as number | undefined,
  mapOffset: '',
})

const isEdit = computed(() => Boolean(props.record?.id))
const title = computed(() => (isEdit.value ? t('linePage.formEdit') : t('linePage.formAdd')))
const modeTag = computed(() => (isEdit.value ? t('linePage.formModeEdit') : t('linePage.formModeAdd')))
const modeTagColor = computed(() => (isEdit.value ? 'processing' : 'success'))

async function loadDetail() {
  if (!props.record?.id) {
    formState.id = undefined
    formState.name = ''
    formState.selfCode = ''
    formState.productCode = ''
    formState.productName = ''
    formState.materialCode = ''
    formState.materialName = ''
    formState.versionNo = ''
    formState.throughput = undefined
    formState.stepInterval = undefined
    preservedPlc.type = 0
    preservedPlc.mapDb = undefined
    preservedPlc.mapOffset = ''
    return
  }
  loading.value = true
  try {
    const detail = await fetchLineById(props.record.id)
    formState.id = detail?.id
    formState.name = detail?.name || ''
    formState.selfCode = detail?.selfCode || ''
    formState.productCode = detail?.productCode || ''
    formState.productName = detail?.productName || ''
    formState.materialCode = detail?.materialCode || ''
    formState.materialName = detail?.materialName || ''
    formState.versionNo = detail?.versionNo || ''
    formState.throughput = detail?.throughput
    formState.stepInterval = detail?.stepInterval
    preservedPlc.type = detail?.type ?? 0
    preservedPlc.mapDb = detail?.mapDb
    preservedPlc.mapOffset = detail?.mapOffset || ''
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!formState.name.trim()) {
    message.warning(t('linePage.nameRequired'))
    return
  }
  if (!formState.selfCode.trim()) {
    message.warning(t('linePage.codeRequired'))
    return
  }
  loading.value = true
  try {
    await saveLine({
      id: formState.id,
      name: formState.name.trim(),
      selfCode: formState.selfCode.trim(),
      productCode: formState.productCode.trim() || undefined,
      productName: formState.productName.trim() || undefined,
      materialCode: formState.materialCode.trim() || undefined,
      materialName: formState.materialName.trim() || undefined,
      versionNo: formState.versionNo.trim() || undefined,
      throughput: formState.throughput,
      stepInterval: formState.stepInterval,
      type: preservedPlc.type,
      mapDb: preservedPlc.mapDb,
      mapOffset: preservedPlc.mapOffset.trim() || undefined,
    })
    message.success(t('linePage.saveSuccess'))
    emit('success')
    emit('update:open', false)
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
    await loadDetail()
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :confirm-loading="loading"
    width="680px"
    destroy-on-close
    class="process-form-modal process-form-modal--line line-form-modal"
    @cancel="emit('update:open', false)"
    @ok="onSubmit"
  >
    <template #title>
      <div class="modal-title-wrap">
        <span class="modal-title">
          <span class="modal-title__icon">
            <NodeIndexOutlined />
          </span>
          {{ title }}
        </span>
        <a-tag :color="modeTagColor">{{ modeTag }}</a-tag>
      </div>
    </template>
    <a-spin :spinning="loading">
      <div class="line-form-layout">
        <section class="line-form-block line-form-block--basic">
          <header class="line-form-block__head">
            <span class="line-form-block__icon">
              <ProfileOutlined />
            </span>
            <div class="line-form-block__titles">
              <h4 class="line-form-block__title">{{ t('linePage.sectionBasic') }}</h4>
              <p class="line-form-block__hint">{{ t('linePage.sectionBasicHint') }}</p>
            </div>
          </header>
          <div class="line-form-block__body">
            <a-form layout="vertical" class="line-form-basic">
              <a-row :gutter="16">
                <a-col :xs="24" :md="12">
                  <a-form-item :label="t('linePage.name')" required>
                    <a-input v-model:value="formState.name" allow-clear />
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="7">
                  <a-form-item :label="t('linePage.code')" required>
                    <a-input
                      v-model:value="formState.selfCode"
                      :disabled="isEdit"
                      allow-clear
                    >
                      <template #prefix>
                        <BarcodeOutlined class="line-form-input-icon" />
                      </template>
                    </a-input>
                  </a-form-item>
                </a-col>
                <a-col :xs="24" :md="5">
                  <a-form-item :label="t('linePage.versionNo')">
                    <a-input
                      v-model:value="formState.versionNo"
                      allow-clear
                      :placeholder="t('linePage.versionNoPlaceholder')"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <div class="line-form-field-group">
                <div class="line-form-field-group__title">{{ t('linePage.productTarget') }}</div>
                <a-row :gutter="16">
                  <a-col :xs="24" :md="12">
                    <a-form-item :label="t('linePage.productCode')">
                      <a-input
                        v-model:value="formState.productCode"
                        allow-clear
                        :placeholder="t('linePage.productCodePlaceholder')"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item :label="t('linePage.productName')">
                      <a-input
                        v-model:value="formState.productName"
                        allow-clear
                        :placeholder="t('linePage.productNamePlaceholder')"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
              </div>
              <div class="line-form-field-group">
                <div class="line-form-field-group__title">{{ t('linePage.material') }}</div>
                <a-row :gutter="16">
                  <a-col :xs="24" :md="12">
                    <a-form-item :label="t('linePage.materialCode')">
                      <a-input
                        v-model:value="formState.materialCode"
                        allow-clear
                        :placeholder="t('linePage.materialCodePlaceholder')"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item :label="t('linePage.materialName')">
                      <a-input
                        v-model:value="formState.materialName"
                        allow-clear
                        :placeholder="t('linePage.materialNamePlaceholder')"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
              </div>
            </a-form>
          </div>
        </section>

        <section class="line-form-block line-form-block--params">
          <header class="line-form-block__head">
            <span class="line-form-block__icon">
              <SettingOutlined />
            </span>
            <div class="line-form-block__titles">
              <h4 class="line-form-block__title">{{ t('linePage.sectionParams') }}</h4>
              <p class="line-form-block__hint">{{ t('linePage.sectionParamsHint') }}</p>
            </div>
          </header>
          <div class="line-form-block__body">
            <div class="line-form-param-grid">
              <div class="line-form-param">
                <label class="line-form-param__label">
                  <DashboardOutlined />
                  {{ t('linePage.throughput') }}
                </label>
                <div class="line-form-param__control">
                  <a-input-number
                    v-model:value="formState.throughput"
                    :min="0"
                    :controls="false"
                    class="line-form-param__input"
                    placeholder="0"
                  />
                  <span class="line-form-param__unit">{{ t('linePage.throughputUnit') }}</span>
                </div>
              </div>
              <div class="line-form-param">
                <label class="line-form-param__label">
                  <ClockCircleOutlined />
                  {{ t('linePage.stepInterval') }}
                </label>
                <div class="line-form-param__control">
                  <a-input-number
                    v-model:value="formState.stepInterval"
                    :min="0"
                    :precision="0"
                    :controls="false"
                    class="line-form-param__input"
                    placeholder="0"
                  />
                  <span class="line-form-param__unit">{{ t('linePage.stepIntervalUnit') }}</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </a-spin>
  </a-modal>
</template>
