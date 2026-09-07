<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'
import type { NodeProps } from '@vue-flow/core'

defineProps<NodeProps<{ title: string; summary?: string }>>()
</script>

<template>
  <div class="line-tf-step-node tone-drive">
    <Handle id="left" type="target" :position="Position.Left" class="line-tf-step-node__handle" />
    <Handle id="top" type="target" :position="Position.Top" class="line-tf-step-node__handle" />
    <Handle id="right" type="source" :position="Position.Right" class="line-tf-step-node__handle" />
    <Handle id="bottom" type="source" :position="Position.Bottom" class="line-tf-step-node__handle" />
    <div class="line-tf-step-node__title">{{ $props.data.title }}</div>
    <div class="line-tf-step-node__summary">{{ $props.data.summary || '—' }}</div>
  </div>
</template>
