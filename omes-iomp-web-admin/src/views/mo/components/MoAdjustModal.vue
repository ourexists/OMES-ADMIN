<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs, { type Dayjs } from 'dayjs'
import { AlertOutlined, CheckCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons-vue'
import {
  adjustMo,
  previewMoAdjust,
  type MoAdjustCommand,
  type MoAdjustPreviewResult,
  type MoAdjustResult,
} from '@/api/mo'
import type { MoRecord } from '@/types/mo'
import type { MpsDetailRecord, MpsRecord } from '@/types/mps'
import LinePickerModal from './LinePickerModal.vue'
import { message } from 'ant-design-vue'

export type MoAdjustType =
  | 'CANCEL_MO'
  | 'CANCEL_MPS'
  | 'RESCHEDULE'
  | 'CHANGE_LINE'
  | 'CHANGE_DEV'
  | 'QTY_UP'
  | 'QTY_DOWN'

const props = defineProps<{
  open: boolean
  adjustType: MoAdjustType | null
  moRecord?: MoRecord | null
  mpsRecord?: MpsRecord | null
  /** 批次取消时可传入多个 id（同 mo） */
  mpsIds?: string[]
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  success: [result: MoAdjustResult]
}>()

const { t } = useI18n()
const submitting = ref(false)
const previewing = ref(false)
const linePickerOpen = ref(false)
const preview = ref<MoAdjustPreviewResult | null>(null)

const form = reactive({
  execTime: null as Dayjs | null,
  newLineCode: '',
  newLineName: '',
  delta: 1 as number | null,
  newNum: 1 as number | null,
  matCode: '',
  devNo: '',
  devName: '',
  force: false,
  operator: 'admin',
})

const moCode = computed(() => props.moRecord?.selfCode || props.mpsRecord?.moCode || '')

const title = computed(() => {
  const map: Record<MoAdjustType, string> = {
    CANCEL_MO: t('moPage.cancelOrder'),
    CANCEL_MPS: t('mpsPage.delete'),
    RESCHEDULE: t('moPage.reschedule'),
    CHANGE_LINE: t('moPage.changeLine'),
    CHANGE_DEV: t('mpsPage.changeDev'),
    QTY_UP: t('moPage.qtyUp'),
    QTY_DOWN: t('moPage.qtyDown'),
  }
  return props.adjustType ? map[props.adjustType] : t('moPage.adjustTitle')
})

const targetMpsIds = computed(() => {
  if (props.mpsIds?.length) return props.mpsIds
  if (props.mpsRecord?.id) return [props.mpsRecord.id]
  return [] as string[]
})

const mpsDetails = ref<MpsDetailRecord[]>([])

const detailOptions = computed(() => {
  const details = mpsDetails.value.length
    ? mpsDetails.value
    : props.mpsRecord?.details || []
  return details.filter((d): d is MpsDetailRecord & { matCode: string } => !!d.matCode)
})

function resetForm() {
  preview.value = null
  mpsDetails.value = props.mpsRecord?.details ? [...props.mpsRecord.details] : []
  form.execTime = props.moRecord?.execTime
    ? dayjs(props.moRecord.execTime)
    : props.mpsRecord?.execTime
      ? dayjs(props.mpsRecord.execTime)
      : dayjs()
  form.newLineCode = props.moRecord?.lineCode || props.mpsRecord?.line || ''
  form.newLineName = props.moRecord?.lineName || props.mpsRecord?.lineVo?.name || ''
  form.delta = 1
  form.newNum = props.moRecord?.num ?? 1
  const first = mpsDetails.value.find((d) => d.matCode)
  form.matCode = first?.matCode || ''
  form.devNo = first?.devNo || ''
  form.devName = first?.devName || ''
  form.force = props.adjustType === 'CANCEL_MPS' && props.mpsRecord?.status === 2
  form.operator = 'admin'
}

function buildCommand(): MoAdjustCommand | null {
  if (!props.adjustType || !moCode.value) return null
  const cmd: MoAdjustCommand = {
    moCode: moCode.value,
    adjustType: props.adjustType,
    payload: {},
    force: form.force || undefined,
    operator: form.force ? form.operator.trim() || undefined : undefined,
  }

  switch (props.adjustType) {
    case 'CANCEL_MO':
      cmd.payload = {}
      break
    case 'CANCEL_MPS':
      if (!targetMpsIds.value.length) return null
      cmd.payload = { mpsIds: targetMpsIds.value }
      break
    case 'RESCHEDULE': {
      if (!form.execTime) return null
      const execTime = form.execTime.format('YYYY-MM-DD HH:mm:ss')
      cmd.payload = {
        execTime,
        mpsIds: targetMpsIds.value.length ? targetMpsIds.value : undefined,
        dequeueQueued: true,
        syncMo: true,
      }
      break
    }
    case 'CHANGE_LINE':
      if (!form.newLineCode.trim()) return null
      cmd.payload = {
        newLineCode: form.newLineCode.trim(),
        mpsIds: targetMpsIds.value.length ? targetMpsIds.value : undefined,
      }
      break
    case 'CHANGE_DEV': {
      const mpsId = targetMpsIds.value[0]
      if (!mpsId || !form.matCode.trim() || !form.devNo.trim()) return null
      cmd.payload = {
        mpsId,
        matCode: form.matCode.trim(),
        devNo: form.devNo.trim(),
        devName: form.devName.trim() || undefined,
      }
      break
    }
    case 'QTY_UP':
      if (!form.delta || form.delta <= 0) return null
      cmd.payload = { delta: form.delta }
      break
    case 'QTY_DOWN':
      if (!form.newNum || form.newNum <= 0) return null
      cmd.payload = { newNum: form.newNum }
      break
    default:
      return null
  }
  return cmd
}

async function runPreview() {
  const cmd = buildCommand()
  if (!cmd) {
    message.warning(t('moPage.adjustFormIncomplete'))
    return
  }
  previewing.value = true
  try {
    preview.value = (await previewMoAdjust(cmd)) || null
  } finally {
    previewing.value = false
  }
}

async function onSubmit() {
  const cmd = buildCommand()
  if (!cmd) {
    message.warning(t('moPage.adjustFormIncomplete'))
    return
  }
  if (form.force && !form.operator.trim()) {
    message.warning(t('moPage.forceOperator'))
    return
  }
  submitting.value = true
  try {
    if (!preview.value) {
      await runPreview()
    }
    if (preview.value?.allowed === false) {
      if (preview.value.requiresForce && !form.force) {
        form.force = true
        message.warning(t('moPage.adjustNeedsForce'))
        return
      }
      message.error(preview.value.rejectReason || t('moPage.adjustRejected'))
      return
    }
    const result = await adjustMo(cmd)
    message.success(t('moPage.adjustSuccess'))
    emit('success', result || {})
    emit('update:open', false)
  } finally {
    submitting.value = false
  }
}

function onClose() {
  emit('update:open', false)
}

function onLineSelect(line: { selfCode?: string; name?: string }) {
  form.newLineCode = line.selfCode || ''
  form.newLineName = line.name || ''
  preview.value = null
}

function onMatChange(matCode: unknown) {
  form.matCode = String(matCode || '')
  const hit = detailOptions.value.find((d) => d.matCode === form.matCode)
  form.devNo = hit?.devNo || ''
  form.devName = hit?.devName || ''
  preview.value = null
}

watch(
  () => props.open,
  async (open) => {
    if (!open) return
    resetForm()
    if (props.adjustType === 'CHANGE_DEV' && props.mpsRecord?.id && !detailOptions.value.length) {
      try {
        const { fetchMpsById } = await import('@/api/mps')
        const full = await fetchMpsById(props.mpsRecord.id)
        if (full?.details?.length) {
          mpsDetails.value = full.details
          const first = full.details.find((d) => d.matCode)
          form.matCode = first?.matCode || ''
          form.devNo = first?.devNo || ''
          form.devName = first?.devName || ''
        }
      } catch {
        // ignore
      }
    }
  },
)

watch(
  () => [
    form.execTime,
    form.newLineCode,
    form.delta,
    form.newNum,
    form.matCode,
    form.devNo,
    form.force,
    form.operator,
  ],
  () => {
    preview.value = null
  },
)
</script>

<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="submitting"
    :ok-text="t('moPage.adjustSubmit')"
    :cancel-text="t('moPage.cancel')"
    destroy-on-close
    width="560px"
    @ok="onSubmit"
    @cancel="onClose"
  >
    <div class="mo-adjust-modal">
      <div class="mo-adjust-modal__meta">
        <div class="meta-row">
          <span class="meta-label">{{ t('moPage.moCode') }}</span>
          <code>{{ moCode || '—' }}</code>
        </div>
        <div v-if="moRecord?.productName || mpsRecord?.moDto?.productName" class="meta-row">
          <span class="meta-label">{{ t('moPage.bomName') }}</span>
          <span>{{ moRecord?.productName || mpsRecord?.moDto?.productName }}</span>
        </div>
        <div v-if="targetMpsIds.length" class="meta-row">
          <span class="meta-label">{{ t('mpsPage.batch') }}</span>
          <span>{{ targetMpsIds.length }} {{ t('moPage.adjustBatchCount') }}</span>
        </div>
      </div>

      <a-form layout="vertical" class="mo-adjust-modal__form">
        <template v-if="adjustType === 'RESCHEDULE'">
          <a-form-item :label="t('moPage.execTime')" required>
            <a-date-picker
              v-model:value="form.execTime"
              show-time
              format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </a-form-item>
          <p class="mo-adjust-modal__hint">{{ t('moPage.rescheduleContent') }}</p>
        </template>

        <template v-else-if="adjustType === 'CHANGE_LINE'">
          <a-form-item :label="t('moPage.lineCode')" required>
            <a-input-group compact>
              <a-input
                v-model:value="form.newLineCode"
                :placeholder="t('moPage.changeLineRequired')"
                style="width: calc(100% - 88px)"
                readonly
              />
              <a-button style="width: 88px" @click="linePickerOpen = true">
                {{ t('moPage.pickerSelect') }}
              </a-button>
            </a-input-group>
          </a-form-item>
          <p v-if="form.newLineName" class="mo-adjust-modal__hint">{{ form.newLineName }}</p>
          <p class="mo-adjust-modal__hint">{{ t('moPage.changeLineHint') }}</p>
        </template>

        <template v-else-if="adjustType === 'CHANGE_DEV'">
          <a-form-item :label="t('mpsPage.matCode')" required>
            <a-select
              v-if="detailOptions.length"
              :value="form.matCode"
              style="width: 100%"
              :options="detailOptions.map((d) => ({ value: d.matCode, label: `${d.matName || d.matCode} (${d.matCode})` }))"
              @change="onMatChange"
            />
            <a-input v-else v-model:value="form.matCode" :placeholder="t('mpsPage.changeDevMatPrompt')" />
          </a-form-item>
          <a-form-item :label="t('mpsPage.devNo')" required>
            <a-input v-model:value="form.devNo" :placeholder="t('mpsPage.changeDevNoPrompt')" allow-clear />
          </a-form-item>
          <a-form-item :label="t('mpsPage.devName')">
            <a-input v-model:value="form.devName" allow-clear />
          </a-form-item>
        </template>

        <template v-else-if="adjustType === 'QTY_UP'">
          <a-form-item :label="t('moPage.qtyDelta')" required>
            <a-input-number v-model:value="form.delta" :min="1" style="width: 100%" />
          </a-form-item>
          <p class="mo-adjust-modal__hint">
            {{ t('moPage.qtyUpHint', { current: moRecord?.num ?? '—', surplus: moRecord?.surplus ?? '—' }) }}
          </p>
        </template>

        <template v-else-if="adjustType === 'QTY_DOWN'">
          <a-form-item :label="t('moPage.qtyNewNum')" required>
            <a-input-number v-model:value="form.newNum" :min="1" style="width: 100%" />
          </a-form-item>
          <p class="mo-adjust-modal__hint">
            {{ t('moPage.qtyDownHint', { current: moRecord?.num ?? '—' }) }}
          </p>
        </template>

        <template v-else-if="adjustType === 'CANCEL_MO' || adjustType === 'CANCEL_MPS'">
          <a-alert
            type="warning"
            show-icon
            :message="adjustType === 'CANCEL_MO' ? t('moPage.cancelContent', { count: 1 }) : t('mpsPage.deleteContent', { count: targetMpsIds.length || 1 })"
            style="margin-bottom: 12px"
          />
          <a-form-item>
            <a-checkbox v-model:checked="form.force">{{ t('moPage.cancelForce') }}</a-checkbox>
          </a-form-item>
          <a-form-item v-if="form.force" :label="t('moPage.forceOperator')" required>
            <a-input v-model:value="form.operator" allow-clear />
          </a-form-item>
          <p v-if="form.force" class="mo-adjust-modal__hint mo-adjust-modal__hint--danger">
            {{ t('moPage.cancelForceContent') }}
          </p>
        </template>
      </a-form>

      <div class="mo-adjust-modal__preview">
        <div class="preview-head">
          <span>{{ t('moPage.preview') }}</span>
          <a-button size="small" type="link" :loading="previewing" @click="runPreview">
            {{ t('moPage.adjustRefreshPreview') }}
          </a-button>
        </div>
        <div v-if="!preview" class="preview-empty">{{ t('moPage.adjustPreviewEmpty') }}</div>
        <div v-else class="preview-body" :class="{ 'preview-body--reject': preview.allowed === false }">
          <div class="preview-status">
            <CheckCircleOutlined v-if="preview.allowed !== false" class="ok" />
            <ExclamationCircleOutlined v-else class="bad" />
            <span>
              {{
                preview.allowed === false
                  ? preview.rejectReason || t('moPage.adjustRejected')
                  : t('moPage.adjustPreviewOk')
              }}
            </span>
          </div>
          <ul v-if="preview.wouldVoidMpsIds?.length" class="preview-list">
            <li>{{ t('moPage.adjustWouldVoid', { count: preview.wouldVoidMpsIds.length }) }}</li>
          </ul>
          <ul v-if="preview.wouldAffectMpsIds?.length" class="preview-list">
            <li>{{ t('moPage.adjustWouldAffect', { count: preview.wouldAffectMpsIds.length }) }}</li>
          </ul>
          <p v-if="preview.surplusDelta != null" class="preview-line">
            surplus Δ {{ preview.surplusDelta }}
            <template v-if="preview.newNum != null"> · num → {{ preview.newNum }}</template>
            <template v-if="preview.newSurplus != null"> · surplus → {{ preview.newSurplus }}</template>
          </p>
          <p v-for="(h, i) in preview.hints || []" :key="`h-${i}`" class="preview-line">{{ h }}</p>
          <p v-for="(w, i) in preview.warnings || []" :key="`w-${i}`" class="preview-line preview-line--warn">
            <AlertOutlined /> {{ w }}
          </p>
          <p v-if="preview.requiresForce" class="preview-line preview-line--warn">
            {{ t('moPage.adjustNeedsForce') }}
          </p>
        </div>
      </div>
    </div>

    <LinePickerModal v-model:open="linePickerOpen" @select="onLineSelect" />
  </a-modal>
</template>

<style scoped>
.mo-adjust-modal {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.mo-adjust-modal__meta {
  display: grid;
  gap: 6px;
  padding: 10px 12px;
  background: var(--omes-color-fill-quaternary, #f7f8fa);
  border-radius: 8px;
}

.meta-row {
  display: flex;
  gap: 10px;
  align-items: baseline;
  min-width: 0;
  font-size: 13px;
}

.meta-label {
  flex: 0 0 72px;
  color: var(--omes-color-text-secondary, #8c8c8c);
}

.meta-row code {
  font-size: 12px;
}

.mo-adjust-modal__hint {
  margin: -4px 0 0;
  color: var(--omes-color-text-secondary, #8c8c8c);
  font-size: 12px;
  line-height: 1.5;
}

.mo-adjust-modal__hint--danger {
  color: var(--omes-color-error, #cf1322);
}

.mo-adjust-modal__preview {
  border: 1px solid var(--omes-color-border-secondary, #f0f0f0);
  border-radius: 8px;
  overflow: hidden;
}

.preview-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 10px;
  background: var(--omes-color-fill-quaternary, #fafafa);
  font-size: 13px;
  font-weight: 500;
}

.preview-empty {
  padding: 12px;
  color: var(--omes-color-text-quaternary, #bfbfbf);
  font-size: 12px;
}

.preview-body {
  padding: 10px 12px;
  font-size: 12px;
}

.preview-body--reject {
  background: #fff2f0;
}

.preview-status {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  margin-bottom: 6px;
  font-weight: 500;
}

.preview-status .ok {
  color: #389e0d;
  margin-top: 2px;
}

.preview-status .bad {
  color: #cf1322;
  margin-top: 2px;
}

.preview-list {
  margin: 0 0 4px;
  padding-left: 18px;
}

.preview-line {
  margin: 2px 0;
  color: var(--omes-color-text-secondary, #595959);
}

.preview-line--warn {
  color: #d48806;
}
</style>
