<template>
  <div class="complete-fields">
    <div class="ramp-base-card">
      <div class="field-row">
        <span class="field-label required">方式</span>
        <a-select
            v-model:value="model.kind"
            :options="completeModeOptions"
            :disabled="disabled"
            size="small"
            class="field-control"
        />
      </div>
      <p class="complete-hint">{{ modeHint }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { COMPLETE_ACTION_OPTIONS } from '@/utils/process/processStepScript'

const model = defineModel({ type: Object, required: true })

defineProps({
  disabled: { type: Boolean, default: false }
})

const completeModeOptions = COMPLETE_ACTION_OPTIONS

const modeHint = computed(() => {
  if (model.value?.kind === 'MANUAL_CONFIRM') {
    return '执行动作结束后暂停，待操作员确认后再进入下一驱动组合。'
  }
  return '执行动作结束后自动进入下一驱动组合，无需人工确认。'
})
</script>

<style scoped>
.complete-fields {
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

.field-label.required::before {
  content: '*';
  color: #ff4d4f;
  margin-right: 2px;
}

.field-control {
  width: 100%;
  min-width: 0;
}

.complete-hint {
  margin: 0;
  font-size: 11px;
  line-height: 1.5;
  color: rgba(0, 0, 0, 0.45);
}
</style>
