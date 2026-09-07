<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'
import type { MpsTfRuntimeTone } from '@/types/mps'

defineProps<
  NodeProps<{
    terminalType: 'start' | 'end'
    label: string
    summary: string
    tone?: MpsTfRuntimeTone
  }>
>()
</script>

<template>
  <div
    class="mps-tf-runtime-node mps-tf-runtime-node--terminal"
    :class="[`tone-${$props.data.tone || 'pending'}`]"
  >
    <Handle
      v-if="$props.data.terminalType === 'start'"
      id="right"
      type="source"
      :position="Position.Right"
      class="mps-tf-runtime-node__handle"
    />
    <Handle
      v-else
      id="left"
      type="target"
      :position="Position.Left"
      class="mps-tf-runtime-node__handle"
    />
    <div class="mps-tf-runtime-node__title">{{ $props.data.label }}</div>
    <div class="mps-tf-runtime-node__summary">{{ $props.data.summary }}</div>
  </div>
</template>
