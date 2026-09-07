<template>
  <div class="event-fields">
    <a-alert
        v-if="!equipmentOptions.length"
        type="warning"
        show-icon
        message="请先在工序行配置设备，再设置事件条件"
        class="equipment-hint"
    />

    <template v-if="multiple">
      <div class="conditions-toolbar">
        <span class="toolbar-label">设备条件</span>
        <a-button v-if="!disabled" type="dashed" size="small" @click="addCondition">+ 添加</a-button>
      </div>

      <div v-for="(item, idx) in eventConditions" :key="idx" class="condition-card">
        <div class="condition-card-head">
          <span class="condition-index">{{ idx + 1 }}</span>
          <span class="condition-index-label">设备条件</span>
          <a-button
              v-if="!disabled && eventConditions.length > 1"
              type="text"
              danger
              size="small"
              class="condition-remove"
              @click="removeCondition(idx)"
          >
            删除
          </a-button>
        </div>
        <EventConditionRow
            v-model="eventConditions[idx]"
            :disabled="disabled"
            :equipments="equipments"
            :equipment-options="equipmentOptions"
        />
      </div>
    </template>

    <template v-else>
      <EventConditionRow
          v-if="eventConditions.length"
          v-model="eventConditions[0]"
          :disabled="disabled"
          :equipments="equipments"
          :equipment-options="equipmentOptions"
      />
    </template>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import EventConditionRow from '@/components/process/EventConditionRow.vue'
import {
  buildStepEquipmentOptions,
  defaultEventConditionItem
} from '@/utils/process/processStepScript'

const model = defineModel({ type: Object, required: true })

const props = defineProps({
  disabled: { type: Boolean, default: false },
  equipments: { type: Array, default: () => [] },
  /** 驱动条件：支持多条设备条件 */
  multiple: { type: Boolean, default: false }
})

const equipmentOptions = computed(() => buildStepEquipmentOptions(props.equipments))

const eventConditions = computed({
  get() {
    if (!model.value.eventConditions?.length) {
      model.value.eventConditions = [defaultEventConditionItem(props.equipments)]
    }
    return model.value.eventConditions
  },
  set(list) {
    model.value.eventConditions = list
  }
})

function addCondition() {
  eventConditions.value.push(defaultEventConditionItem(props.equipments))
}

function removeCondition(idx) {
  if (eventConditions.value.length <= 1) return
  eventConditions.value.splice(idx, 1)
}

watch(
    () => model.value.kind,
    (kind) => {
      if (kind === 'EVENT' && !model.value.eventConditions?.length) {
        model.value.eventConditions = [defaultEventConditionItem(props.equipments)]
      }
    },
    { immediate: true }
)
</script>

<style scoped>
.equipment-hint {
  margin-bottom: 10px;
}

.equipment-hint :deep(.ant-alert-message) {
  font-size: 12px;
}

.conditions-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.toolbar-label {
  font-size: 12px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.65);
}

.condition-card {
  margin-bottom: 8px;
  padding: 10px 10px 10px 12px;
  background: #fafbfc;
  border: 1px solid #eef0f3;
  border-left: 3px solid #6366f1;
  border-radius: 6px;
}

.condition-card:last-child {
  margin-bottom: 0;
}

.condition-card-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.condition-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  background: #6366f1;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.condition-index-label {
  flex: 1;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.55);
}

.condition-remove {
  margin-left: auto;
  padding: 0 4px;
  height: auto;
  font-size: 12px;
}
</style>
