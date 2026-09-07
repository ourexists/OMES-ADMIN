<template>
  <div class="event-condition-row">
    <div v-if="equipmentOptions.length" class="field-row">
      <span class="field-label required">设备</span>
      <a-select
          v-model:value="model.equipmentCode"
          :options="equipmentOptions"
          :disabled="disabled"
          placeholder="选择设备"
          size="small"
          class="field-control"
      />
    </div>

    <div class="field-row">
      <span class="field-label">逻辑</span>
      <a-segmented
          v-model:value="model.logicType"
          :options="logicTypeOptions"
          :disabled="disabled || !equipmentOptions.length"
          size="small"
          class="field-control logic-segment"
      />
    </div>

    <div class="field-row">
      <span class="field-label">{{ model.logicType === 'RANGE' ? '范围' : '阈值' }}</span>
      <div class="threshold-row">
        <a-select
            v-model:value="model.variable"
            :options="variableOptions"
            :disabled="disabled || !equipmentOptions.length"
            size="small"
            class="cell-var"
        />
        <template v-if="model.logicType === 'RANGE'">
          <a-input-number
              v-model:value="model.min"
              :disabled="disabled || !equipmentOptions.length"
              size="small"
              class="cell-num"
              placeholder="下限"
          />
          <span class="range-sep">~</span>
          <a-input-number
              v-model:value="model.max"
              :disabled="disabled || !equipmentOptions.length"
              size="small"
              class="cell-num"
              placeholder="上限"
          />
        </template>
        <template v-else>
          <a-select
              v-model:value="model.operator"
              :options="operatorOptions"
              :disabled="disabled || !equipmentOptions.length"
              size="small"
              class="cell-op"
          />
          <a-input-number
              v-model:value="model.value"
              :disabled="disabled || !equipmentOptions.length"
              size="small"
              class="cell-num"
              placeholder="数值"
          />
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import {
  defaultEventConditionItem,
  EVENT_LOGIC_TYPE_OPTIONS,
  EVENT_OPERATOR_OPTIONS,
  EVENT_VARIABLE_OPTIONS
} from '@/utils/process/processStepScript'

const model = defineModel({ type: Object, required: true })

const props = defineProps({
  disabled: { type: Boolean, default: false },
  equipments: { type: Array, default: () => [] },
  equipmentOptions: { type: Array, default: () => [] }
})

const logicTypeOptions = EVENT_LOGIC_TYPE_OPTIONS
const variableOptions = EVENT_VARIABLE_OPTIONS
const operatorOptions = EVENT_OPERATOR_OPTIONS.map(({ value, label }) => ({ value, label }))

watch(
    () => props.equipmentOptions,
    (options) => {
      if (!options.length) return
      const valid = options.some((item) => item.value === model.value.equipmentCode)
      if (!valid) {
        Object.assign(model.value, defaultEventConditionItem(props.equipments))
      }
    },
    { immediate: true }
)

watch(
    () => model.value.logicType,
    (type) => {
      if (type === 'RANGE') {
        model.value.operator = '>='
        model.value.value = undefined
      } else {
        model.value.min = undefined
        model.value.max = undefined
      }
    }
)
</script>

<style scoped>
.event-condition-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-row {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  column-gap: 8px;
}

.field-label {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
  line-height: 1.2;
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

.logic-segment {
  max-width: 100%;
}

.logic-segment :deep(.ant-segmented) {
  width: 100%;
}

.logic-segment :deep(.ant-segmented-item) {
  flex: 1;
  text-align: center;
}

.threshold-row {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  align-items: center;
  min-width: 0;
  width: 100%;
}

.threshold-row .cell-var {
  flex: 0 0 76px;
  width: 76px;
  min-width: 76px;
}

.threshold-row .cell-op {
  flex: 0 0 48px;
  width: 48px;
  min-width: 48px;
}

.threshold-row .cell-num {
  flex: 1 1 0;
  min-width: 56px;
  width: 0;
}

.threshold-row .cell-num :deep(.ant-input-number) {
  width: 100%;
}

.range-sep {
  flex: 0 0 auto;
  color: rgba(0, 0, 0, 0.35);
  font-size: 12px;
  line-height: 24px;
}

.cell-op :deep(.ant-select-selector),
.cell-var :deep(.ant-select-selector) {
  padding-inline: 6px !important;
}
</style>
