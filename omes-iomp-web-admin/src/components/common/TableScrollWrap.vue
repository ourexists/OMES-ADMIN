<script setup lang="ts">
import { watch } from 'vue'
import { useTableScroll } from '@/composables/useTableScroll'

const props = withDefaults(
  defineProps<{
    minHeight?: number
    /** Shrink table body to content when rows do not fill the available height */
    fitContent?: boolean
    /** Recompute body height when table data / pagination changes */
    refreshKeys?: unknown[]
  }>(),
  {
    minHeight: 200,
    fitContent: false,
    refreshKeys: () => [],
  },
)

const { wrapRef, scrollY, scrollReady, refreshScroll } = useTableScroll({
  minHeight: props.minHeight,
  fitContent: props.fitContent,
})

watch(
  () => props.refreshKeys,
  () => {
    void refreshScroll()
  },
  { deep: true },
)

defineExpose({ refreshScroll, scrollY, scrollReady, wrapRef })
</script>

<template>
  <div
    ref="wrapRef"
    class="table-scroll-wrap"
    :data-scroll-ready="scrollReady ? '1' : undefined"
  >
    <slot :scroll-y="scrollY" :scroll-ready="scrollReady" />
  </div>
</template>
