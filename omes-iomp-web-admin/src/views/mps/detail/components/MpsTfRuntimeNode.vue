<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'
import type { MpsTfRuntimeTone } from '@/types/mps'

defineProps<
  NodeProps<{
    title: string
    summary?: string
    statusDesc?: string
    tone?: MpsTfRuntimeTone
  }>
>()
</script>

<template>
  <div class="mps-tf-runtime-node" :class="[`tone-${$props.data.tone || 'pending'}`]">
    <Handle id="left" type="target" :position="Position.Left" class="mps-tf-runtime-node__handle" />
    <Handle id="top" type="target" :position="Position.Top" class="mps-tf-runtime-node__handle" />
    <Handle id="right" type="source" :position="Position.Right" class="mps-tf-runtime-node__handle" />
    <Handle id="bottom" type="source" :position="Position.Bottom" class="mps-tf-runtime-node__handle" />
    <div class="mps-tf-runtime-node__head">
      <div class="mps-tf-runtime-node__title">{{ $props.data.title }}</div>
      <div class="mps-tf-runtime-node__status">{{ $props.data.statusDesc || '—' }}</div>
    </div>
    <div class="mps-tf-runtime-node__summary">{{ $props.data.summary || '—' }}</div>
  </div>
</template>
