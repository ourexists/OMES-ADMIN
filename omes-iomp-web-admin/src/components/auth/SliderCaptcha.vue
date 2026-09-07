<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { CheckOutlined, DoubleRightOutlined } from '@ant-design/icons-vue'
import { initSliderCaptcha, verifySliderCaptcha } from '@/api/auth'

const props = defineProps<{
  uuid: string
}>()

const emit = defineEmits<{
  success: [code: string]
  reset: []
}>()

const { t } = useI18n()

const THUMB_WIDTH = 44
const PASS_RATIO = 0.92

const trackRef = ref<HTMLElement | null>(null)
const thumbRef = ref<HTMLElement | null>(null)
const offset = ref(0)
const dragging = ref(false)
const status = ref<'default' | 'success' | 'fail'>('default')
const ready = ref(false)

let startX = 0
let startOffset = 0
let activePointerId: number | null = null

const hintText = computed(() => {
  if (status.value === 'success') {
    return t('login.sliderSuccess')
  }
  if (status.value === 'fail') {
    return t('login.sliderFail')
  }
  return t('login.sliderHint')
})

function maxOffset(): number {
  const track = trackRef.value
  if (!track) {
    return 0
  }
  return Math.max(0, track.clientWidth - THUMB_WIDTH)
}

async function refresh() {
  status.value = 'default'
  offset.value = 0
  dragging.value = false
  activePointerId = null
  ready.value = false
  emit('reset')
  await initSliderCaptcha(props.uuid)
  ready.value = true
}

function onPointerDown(event: PointerEvent) {
  if (!ready.value || status.value === 'success') {
    return
  }
  dragging.value = true
  startX = event.clientX
  startOffset = offset.value
  activePointerId = event.pointerId
  thumbRef.value?.setPointerCapture(event.pointerId)
}

function onPointerMove(event: PointerEvent) {
  if (!dragging.value || activePointerId !== event.pointerId) {
    return
  }
  const max = maxOffset()
  offset.value = Math.min(max, Math.max(0, startOffset + event.clientX - startX))
}

async function onPointerUp(event: PointerEvent) {
  if (!dragging.value || activePointerId !== event.pointerId) {
    return
  }
  dragging.value = false
  activePointerId = null
  thumbRef.value?.releasePointerCapture(event.pointerId)

  const max = maxOffset()
  const trackWidth = trackRef.value?.clientWidth ?? 0

  if (offset.value >= max * PASS_RATIO) {
    try {
      const code = await verifySliderCaptcha(props.uuid, Math.round(offset.value), trackWidth)
      status.value = 'success'
      offset.value = max
      emit('success', code)
      return
    } catch {
      await fail()
      return
    }
  }

  await fail()
}

async function fail() {
  status.value = 'fail'
  window.setTimeout(() => {
    void refresh()
  }, 600)
}

onMounted(() => {
  void refresh()
})

defineExpose({ refresh })
</script>

<template>
  <div
    ref="trackRef"
    class="slider-captcha"
    :class="{
      'slider-captcha--success': status === 'success',
      'slider-captcha--fail': status === 'fail',
      'slider-captcha--dragging': dragging,
    }"
  >
    <div class="slider-captcha__fill" :style="{ width: `${offset + THUMB_WIDTH / 2}px` }" />
    <span class="slider-captcha__hint">{{ hintText }}</span>
    <button
      ref="thumbRef"
      type="button"
      class="slider-captcha__thumb"
      :style="{ transform: `translateX(${offset}px)` }"
      :disabled="status === 'success' || !ready"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerUp"
    >
      <CheckOutlined v-if="status === 'success'" />
      <DoubleRightOutlined v-else />
    </button>
  </div>
</template>

<style scoped>
.slider-captcha {
  position: relative;
  height: 40px;
  border: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-sm);
  background: var(--omes-color-bg-elevated);
  overflow: hidden;
  user-select: none;
  touch-action: none;
}

.slider-captcha__fill {
  position: absolute;
  inset: 0 auto 0 0;
  background: var(--omes-color-primary-bg);
  transition: width 0.08s linear;
  pointer-events: none;
}

.slider-captcha--success .slider-captcha__fill {
  width: 100% !important;
  background: #f6ffed;
}

.slider-captcha--fail {
  border-color: var(--omes-color-error);
}

.slider-captcha--fail .slider-captcha__fill {
  background: #fff1f0;
}

.slider-captcha__hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 48px;
  color: var(--omes-color-text-quaternary);
  font-size: 13px;
  pointer-events: none;
}

.slider-captcha--success .slider-captcha__hint {
  color: var(--omes-color-success);
}

.slider-captcha--fail .slider-captcha__hint {
  color: var(--omes-color-error);
}

.slider-captcha__thumb {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
  width: 44px;
  height: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-right: 1px solid var(--omes-color-border);
  border-radius: var(--omes-radius-sm);
  background: var(--omes-color-bg-container);
  color: var(--omes-color-primary);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  cursor: grab;
  transition: background 0.2s ease, color 0.2s ease;
}

.slider-captcha--dragging .slider-captcha__thumb {
  cursor: grabbing;
  background: var(--omes-color-primary-bg-hover);
}

.slider-captcha--success .slider-captcha__thumb {
  cursor: default;
  color: var(--omes-color-success);
  border-right-color: #b7eb8f;
}

.slider-captcha__thumb:disabled {
  cursor: not-allowed;
}
</style>
