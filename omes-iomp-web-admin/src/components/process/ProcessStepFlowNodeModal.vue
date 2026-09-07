<template>
  <a-modal
      :open="open"
      :title="modalTitle"
      width="520px"
      :mask-closable="false"
      destroy-on-close
      wrap-class-name="flow-node-modal-wrap"
      @cancel="emit('close')"
  >
    <fieldset v-if="node" :disabled="readonly" class="node-fieldset">
      <div class="node-panel">
        <div class="field-row node-name-row">
          <span class="field-label">名称</span>
          <a-input v-model:value="node.title" :disabled="readonly" size="small" class="field-control" />
        </div>

        <ProcessStepConditionFields
            v-if="isDriveNode"
            v-model="driveCondition"
            :disabled="readonly"
            :equipments="equipments"
            for-drive
        />

        <ProcessStepActionFields
            v-else-if="isActionNode"
            v-model="nodeAction"
            :disabled="readonly"
            :equipments="equipments"
        />

        <ProcessStepCompleteFields
            v-else-if="isCompleteNode"
            v-model="completeAction"
            :disabled="readonly"
        />

        <ProcessStepConditionFields
            v-else-if="isExceptionNode"
            v-model="exceptionCondition"
            :disabled="readonly"
            :equipments="equipments"
        />
      </div>
    </fieldset>

    <template #footer>
      <a-button type="primary" @click="emit('close')">{{ readonly ? '关闭' : '确定' }}</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import { computed } from 'vue'
import ProcessStepCompleteFields from '@/components/process/ProcessStepCompleteFields.vue'
import ProcessStepActionFields from '@/components/process/ProcessStepActionFields.vue'
import ProcessStepConditionFields from '@/components/process/ProcessStepConditionFields.vue'
import {
  defaultAction,
  defaultCompleteAction,
  defaultCondition,
  isCompleteActionKind,
  normalizeAction,
  normalizeCompleteAction
} from '@/utils/process/processStepScript'

const props = defineProps({
  open: { type: Boolean, default: false },
  node: { type: Object, default: null },
  readonly: { type: Boolean, default: false },
  equipments: { type: Array, default: () => [] }
})

const emit = defineEmits(['close'])

const modalTitle = computed(() => props.node?.title || '节点配置')
const isDriveNode = computed(() => props.node?.type === 'drive')
const isActionNode = computed(() => props.node?.type === 'action')
const isCompleteNode = computed(() => props.node?.type === 'complete')
const isExceptionNode = computed(() => props.node?.type === 'exception')

const driveCondition = computed({
  get() {
    if (!props.node) return defaultCondition('NONE', props.equipments)
    if (!props.node.condition) {
      props.node.condition = defaultCondition('NONE', props.equipments)
    }
    return props.node.condition
  },
  set(value) {
    if (!props.node) return
    props.node.condition = value
  }
})

const nodeAction = computed({
  get() {
    if (!props.node) return defaultAction('RAMP_TIME', props.equipments)
    props.node.action = normalizeAction(props.node.action, props.equipments)
    return props.node.action
  },
  set(value) {
    if (!props.node) return
    props.node.action = value
  }
})

const completeAction = computed({
  get() {
    if (!props.node) return defaultCompleteAction()
    if (!props.node.completeAction) {
      props.node.completeAction = defaultCompleteAction()
    }
    const k = String(props.node.completeAction.kind || 'AUTO_NEXT').toUpperCase()
    if (!isCompleteActionKind(k)) {
      props.node.completeAction.kind = 'AUTO_NEXT'
    } else if (props.node.completeAction.kind !== k) {
      props.node.completeAction.kind = k
    }
    return props.node.completeAction
  },
  set(value) {
    if (!props.node) return
    props.node.completeAction = normalizeCompleteAction(value)
  }
})

const exceptionCondition = computed({
  get() {
    if (!props.node) return defaultCondition('NONE', props.equipments)
    if (!props.node.condition) {
      props.node.condition = defaultCondition('NONE', props.equipments)
    }
    return props.node.condition
  },
  set(value) {
    if (!props.node) return
    props.node.condition = value
  }
})
</script>

<style scoped>
.node-fieldset {
  border: none;
  margin: 0;
  padding: 0;
  min-width: 0;
}

.node-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-row {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  column-gap: 8px;
}

.node-name-row {
  margin-bottom: 0;
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
</style>

<style>
.flow-node-modal-wrap .ant-modal-body {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}
</style>
