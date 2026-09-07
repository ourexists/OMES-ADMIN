<template>
  <div class="condition-fields">
    <div class="field-row">
      <span class="field-label">类型</span>
      <a-select
          v-model:value="model.kind"
          :options="kindOptions"
          :disabled="disabled"
          size="small"
          class="field-control"
      />
    </div>

    <template v-if="model.kind === 'TIME'">
      <div class="field-row">
        <span class="field-label">时长</span>
        <a-input-number
            v-model:value="model.duration"
            :min="1"
            :precision="0"
            :disabled="disabled"
            size="small"
            class="field-control"
            addon-after="秒"
        />
      </div>
    </template>

    <ProcessStepEventClauses
        v-else-if="model.kind === 'EVENT'"
        v-model="model"
        :disabled="disabled"
        :equipments="equipments"
        :multiple="forDrive"
    />
  </div>
</template>

<script setup>
import { watch } from 'vue'
import ProcessStepEventClauses from '@/components/process/ProcessStepEventClauses.vue'
import { CONDITION_KIND_OPTIONS, defaultDriveEventCondition, defaultEventConditionItem } from '@/utils/process/processStepScript'

const model = defineModel({ type: Object, required: true })

const props = defineProps({
  disabled: { type: Boolean, default: false },
  equipments: { type: Array, default: () => [] },
  /** 驱动条件节点：事件类型支持多条设备条件 */
  forDrive: { type: Boolean, default: false }
})

const kindOptions = CONDITION_KIND_OPTIONS

watch(
    () => model.value.kind,
    (kind) => {
      if (kind !== 'EVENT') return
      if (!model.value.eventConditions?.length) {
        if (props.forDrive) {
          Object.assign(model.value, defaultDriveEventCondition(props.equipments))
        } else {
          model.value.eventConditions = [defaultEventConditionItem(props.equipments)]
        }
      }
    }
)

watch(
    () => props.forDrive,
    (forDrive) => {
      if (forDrive && model.value.kind === 'EVENT' && !model.value.eventConditions?.length) {
        Object.assign(model.value, defaultDriveEventCondition(props.equipments))
      }
    },
    { immediate: true }
)
</script>

<style scoped>
.condition-fields {
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
  white-space: nowrap;
}

.field-control {
  width: 100%;
  min-width: 0;
}

.field-control :deep(.ant-input-number),
.field-control :deep(.ant-input-number-group-wrapper) {
  width: 100%;
}
</style>
