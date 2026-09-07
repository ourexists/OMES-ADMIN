<template>
  <div class="action-fields">
    <div class="field-row">
      <span class="field-label required">类型</span>
      <a-select
          v-model:value="model.kind"
          :options="actionKindOptions"
          :disabled="disabled"
          size="small"
          class="field-control"
          @change="onKindChange"
      />
    </div>

    <template v-if="model.kind === 'RAMP_TIME'">
      <div class="ramp-base-card">
        <div v-if="equipmentOptions.length" class="field-row">
          <span class="field-label required">设备</span>
          <a-select
              v-model:value="model.rampEquipmentCode"
              :options="equipmentOptions"
              :disabled="disabled"
              placeholder="选择设备"
              size="small"
              class="field-control"
          />
        </div>
        <a-alert
            v-else
            type="warning"
            show-icon
            message="请先在工序行配置设备"
            class="equipment-hint"
        />
        <div v-if="equipmentOptions.length" class="field-row">
          <span class="field-label required">过程量</span>
          <a-select
              v-model:value="model.rampVariable"
              :options="rampVariableOptions"
              :disabled="disabled"
              size="small"
              class="field-control"
          />
        </div>
        <p v-if="equipmentOptions.length" class="ramp-device-value">
          起点：执行时读取所选设备的<strong>{{ rampVariableLabel }}</strong>当前值
        </p>
      </div>

      <p class="ramp-hint">
        从设备当前过程量起，每段先升至目标值，可选保持 N 秒后再进入下一段；末段目标为最终值。
      </p>

      <div class="section-toolbar">
        <span class="section-label">斜坡段</span>
        <a-button v-if="!disabled" type="dashed" size="small" @click="addSegment">+ 添加</a-button>
      </div>
      <div v-for="(seg, idx) in model.rampSegments" :key="idx" class="segment-card">
        <div class="segment-card-head">
          <span class="segment-index">{{ idx + 1 }}</span>
          <span class="segment-index-label">第 {{ idx + 1 }} 段</span>
          <a-button
              v-if="!disabled && model.rampSegments.length > 1"
              type="text"
              danger
              size="small"
              class="segment-remove"
              @click="removeSegment(idx)"
          >
            删除
          </a-button>
        </div>
        <div class="segment-flow">
          <span class="flow-arrow">运行</span>
          <a-input-number
              v-model:value="seg.duration"
              :disabled="disabled"
              size="small"
              class="flow-duration"
              :min="1"
              :precision="0"
              placeholder="n"
              addon-after="秒"
          />
          <span class="flow-arrow">升至</span>
          <a-input-number
              v-model:value="seg.to"
              :disabled="disabled"
              size="small"
              class="flow-target"
              placeholder="目标值"
          />
          <span class="flow-arrow">保持</span>
          <a-input-number
              v-model:value="seg.holdDuration"
              :disabled="disabled"
              size="small"
              class="flow-hold"
              :min="0"
              :precision="0"
              placeholder="0"
              addon-after="秒"
          />
        </div>
      </div>
      <div v-if="rampFinal != null" class="ramp-final">
        斜坡终值：<strong>{{ rampFinal }}</strong>
      </div>

      <div class="after-control-card">
        <a-checkbox v-model:checked="afterControl.enabled" :disabled="disabled">
          斜坡后设备控制（PID）
        </a-checkbox>
        <template v-if="afterControl.enabled">
          <p class="after-control-hint">
            斜坡结束后对所选设备过程量进行 PID 闭环控制；默认以斜坡终值为控制目标。
          </p>
          <div class="ramp-base-card">
            <div v-if="equipmentOptions.length" class="field-row">
              <span class="field-label required">设备</span>
              <a-select
                  v-model:value="afterControl.equipmentCode"
                  :options="equipmentOptions"
                  :disabled="disabled"
                  placeholder="选择设备"
                  size="small"
                  class="field-control"
              />
            </div>
            <a-alert
                v-else
                type="warning"
                show-icon
                message="请先在工序行配置设备"
                class="equipment-hint"
            />
            <div v-if="equipmentOptions.length" class="field-row">
              <span class="field-label required">控制</span>
              <a-select
                  v-model:value="afterControl.variable"
                  :options="rampVariableOptions"
                  :disabled="disabled"
                  size="small"
                  class="field-control"
              />
            </div>
            <div class="field-row">
              <span class="field-label required">目标</span>
              <a-input-number
                  v-model:value="afterControl.target"
                  :disabled="disabled"
                  size="small"
                  class="field-control"
                  placeholder="控制目标值"
              />
            </div>
            <div class="field-row field-row-check">
              <span class="field-label"/>
              <a-checkbox v-model:checked="afterControl.useAdvancedPid" :disabled="disabled">高级 PID</a-checkbox>
            </div>
            <div v-if="afterControl.useAdvancedPid" class="field-row">
              <span class="field-label">系数</span>
              <div class="inline-params inline-params-triple">
                <a-input-number v-model:value="afterControl.kp" :disabled="disabled" size="small" class="cell-num"
                                placeholder="Kp"/>
                <a-input-number v-model:value="afterControl.ki" :disabled="disabled" size="small" class="cell-num"
                                placeholder="Ki"/>
                <a-input-number v-model:value="afterControl.kd" :disabled="disabled" size="small" class="cell-num"
                                placeholder="Kd"/>
              </div>
            </div>
          </div>
        </template>
      </div>
    </template>

    <template v-else-if="model.kind === 'PID_CONTROL'">
      <div class="ramp-base-card">
        <div v-if="equipmentOptions.length" class="field-row">
          <span class="field-label required">设备</span>
          <a-select
              v-model:value="model.pidEquipmentCode"
              :options="equipmentOptions"
              :disabled="disabled"
              placeholder="选择设备"
              size="small"
              class="field-control"
          />
        </div>
        <a-alert
            v-else
            type="warning"
            show-icon
            message="请先在工序行配置设备"
            class="equipment-hint"
        />
        <div v-if="equipmentOptions.length" class="field-row">
          <span class="field-label required">控制</span>
          <a-select
              v-model:value="model.pidVariable"
              :options="rampVariableOptions"
              :disabled="disabled"
              size="small"
              class="field-control"
          />
        </div>
        <div class="field-row">
          <span class="field-label required">目标</span>
          <a-input-number
              v-model:value="model.target"
              :disabled="disabled"
              size="small"
              class="field-control"
              placeholder="目标值"
          />
        </div>
        <div class="field-row field-row-check">
          <span class="field-label"/>
          <a-checkbox v-model:checked="model.useAdvancedPid" :disabled="disabled">高级 PID</a-checkbox>
        </div>
        <div v-if="model.useAdvancedPid" class="field-row">
          <span class="field-label">系数</span>
          <div class="inline-params inline-params-triple">
            <a-input-number v-model:value="model.kp" :disabled="disabled" size="small" class="cell-num"
                            placeholder="Kp"/>
            <a-input-number v-model:value="model.ki" :disabled="disabled" size="small" class="cell-num"
                            placeholder="Ki"/>
            <a-input-number v-model:value="model.kd" :disabled="disabled" size="small" class="cell-num"
                            placeholder="Kd"/>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import {computed, watch} from 'vue'
import {
  ACTION_KIND_OPTIONS,
  buildStepEquipmentOptions,
  defaultAfterControl,
  defaultEventEquipmentCode,
  defaultRampSegment,
  normalizeRampVariable,
  RAMP_VARIABLE_OPTIONS,
  rampFinalValue,
  resolveProcessVariableLabel
} from '@/utils/process/processStepScript'

const model = defineModel({type: Object, required: true})

const props = defineProps({
  disabled: {type: Boolean, default: false},
  equipments: {type: Array, default: () => []}
})

const actionKindOptions = ACTION_KIND_OPTIONS
const rampVariableOptions = RAMP_VARIABLE_OPTIONS
const equipmentOptions = computed(() => buildStepEquipmentOptions(props.equipments))
const rampFinal = computed(() => rampFinalValue(model.value))
const rampVariableLabel = computed(() => resolveProcessVariableLabel(model.value.rampVariable))
const pidVariableLabel = computed(() => resolveProcessVariableLabel(model.value.pidVariable))

const afterControl = computed({
  get() {
    if (!model.value.afterControl) {
      model.value.afterControl = defaultAfterControl()
    }
    return model.value.afterControl
  },
  set(val) {
    model.value.afterControl = val
  }
})

const afterControlVariableLabel = computed(() => resolveProcessVariableLabel(afterControl.value.variable))

function ensureAfterControlBase() {
  const ac = afterControl.value
  if (!ac.equipmentCode && equipmentOptions.value.length) {
    ac.equipmentCode = model.value.rampEquipmentCode || defaultEventEquipmentCode(props.equipments)
  }
  ac.variable = normalizeRampVariable(ac.variable || model.value.rampVariable)
}

function ensureRampBase() {
  if (!model.value.rampEquipmentCode && equipmentOptions.value.length) {
    model.value.rampEquipmentCode = defaultEventEquipmentCode(props.equipments)
  }
  model.value.rampVariable = normalizeRampVariable(model.value.rampVariable)
  if (!model.value.afterControl) {
    model.value.afterControl = defaultAfterControl()
  }
  ensureAfterControlBase()
  if (!model.value.rampSegments?.length) {
    model.value.rampSegments = [defaultRampSegment()]
  }
}

function ensurePidBase() {
  if (!model.value.pidEquipmentCode && equipmentOptions.value.length) {
    model.value.pidEquipmentCode = defaultEventEquipmentCode(props.equipments)
  }
  model.value.pidVariable = normalizeRampVariable(model.value.pidVariable)
}

function onKindChange() {
  if (model.value.kind === 'RAMP_TIME') {
    ensureRampBase()
  }
  if (model.value.kind === 'PID_CONTROL') {
    ensurePidBase()
  }
}

function addSegment() {
  if (!model.value.rampSegments) model.value.rampSegments = []
  model.value.rampSegments.push(defaultRampSegment())
}

function removeSegment(idx) {
  model.value.rampSegments.splice(idx, 1)
}

watch(
    () => model.value.kind,
    (kind) => {
      if (kind === 'RAMP_TIME') {
        ensureRampBase()
      }
      if (kind === 'PID_CONTROL') {
        ensurePidBase()
      }
    },
    {immediate: true}
)

watch(
    () => [afterControl.value.enabled, rampFinal.value],
    ([enabled, finalVal]) => {
      if (enabled) {
        ensureAfterControlBase()
      }
      if (enabled && (afterControl.value.target == null || afterControl.value.target === '')
          && finalVal != null) {
        afterControl.value.target = finalVal
      }
    }
)

watch(
    equipmentOptions,
    (options) => {
      if (!options.length) return
      if (model.value.kind === 'RAMP_TIME') {
        const validRamp = options.some((item) => item.value === model.value.rampEquipmentCode)
        if (!validRamp) {
          model.value.rampEquipmentCode = options[0].value
        }
        if (afterControl.value.enabled) {
          const validAfter = options.some((item) => item.value === afterControl.value.equipmentCode)
          if (!validAfter) {
            afterControl.value.equipmentCode = model.value.rampEquipmentCode || options[0].value
          }
        }
      }
      if (model.value.kind === 'PID_CONTROL') {
        const validPid = options.some((item) => item.value === model.value.pidEquipmentCode)
        if (!validPid) {
          model.value.pidEquipmentCode = options[0].value
        }
      }
    },
    {immediate: true}
)
</script>

<style scoped>
.action-fields {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ramp-base-card {
  padding: 10px 10px 10px 12px;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-left: 3px solid #f59e0b;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.after-control-card {
  margin-top: 4px;
  padding: 10px 10px 10px 12px;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-left: 3px solid #f59e0b;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.after-control-hint {
  margin: 0;
  font-size: 11px;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.45);
}

.equipment-hint {
  margin: 0;
}

.equipment-hint :deep(.ant-alert-message) {
  font-size: 12px;
}

.ramp-device-value {
  margin: 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
}

.ramp-device-value strong {
  color: #c2410c;
  font-weight: 600;
}

.ramp-hint {
  margin: 0;
  font-size: 11px;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.45);
}

.ramp-final {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
  text-align: right;
}

.ramp-final strong {
  color: #d97706;
  font-weight: 600;
}

.segment-flow {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.flow-arrow {
  flex: 0 0 auto;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.flow-duration {
  flex: 0 0 100px;
  width: 100px;
}

.flow-target {
  flex: 1 1 72px;
  min-width: 72px;
}

.flow-hold {
  flex: 0 0 96px;
  width: 96px;
}

.flow-duration :deep(.ant-input-number),
.flow-target :deep(.ant-input-number),
.flow-hold :deep(.ant-input-number) {
  width: 100%;
}

.flow-duration :deep(.ant-input-number-group-wrapper),
.flow-target :deep(.ant-input-number-group-wrapper),
.flow-hold :deep(.ant-input-number-group-wrapper) {
  width: 100%;
}

.field-row {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  column-gap: 8px;
}

.field-row-check {
  margin-top: -4px;
}

.field-label {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
  white-space: nowrap;
}

.field-label.required::before {
  content: '*';
  color: #ff4d4f;
  margin-right: 2px;
}

.field-control {
  width: 100%;
  min-width: 0;
}

.section-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
}

.section-label,
.section-label-block {
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.65);
}

.section-label-block {
  margin-bottom: 6px;
}

.segment-card,
.event-section {
  padding: 10px 10px 10px 12px;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-left: 3px solid #f59e0b;
  border-radius: 6px;
}

.event-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.segment-card-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.segment-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  background: #f59e0b;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.segment-index-label {
  flex: 1;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
}

.segment-remove {
  margin-left: auto;
  padding: 0 4px;
  height: auto;
  font-size: 12px;
}

.inline-params {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  align-items: center;
  min-width: 0;
  width: 100%;
}

.inline-params-triple .cell-num {
  flex: 1 1 0;
  min-width: 0;
  width: 0;
}

.inline-params .cell-num {
  flex: 1 1 0;
  min-width: 0;
  width: 0;
}

.inline-params .cell-num :deep(.ant-input-number) {
  width: 100%;
}

.inline-params .cell-num :deep(.ant-input-number-group-wrapper) {
  width: 100%;
}

.field-control :deep(.ant-input-number),
.field-control :deep(.ant-input-number-group-wrapper) {
  width: 100%;
}
</style>
