<script setup lang="ts">
import { useVueFlow } from '@vue-flow/core'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/controls/dist/style.css'

const { fitView } = useVueFlow()
let initialFitDone = false

const FIT_VIEW_PADDING = 0.42
const FIT_VIEW_MAX_ZOOM = 0.86

function fitGraphView(force = false) {
  if (initialFitDone && !force) {
    return
  }
  try {
    fitView({
      padding: FIT_VIEW_PADDING,
      maxZoom: FIT_VIEW_MAX_ZOOM,
      minZoom: 0.2,
      duration: force ? 200 : 0,
    })
    initialFitDone = true
  } catch {
    /* ignore */
  }
}

function scheduleFitView(force = false) {
  requestAnimationFrame(() => {
    fitGraphView(force)
  })
}

defineExpose({ scheduleFitView })
</script>

<template>
  <Controls position="bottom-right" :show-interactive="false" />
</template>
