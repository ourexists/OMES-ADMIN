<script setup lang="ts">
import { theme } from 'ant-design-vue'
import { computed } from 'vue'

defineProps<{
  subtitle?: string
  subtitleClass?: string
  iconClass?: string
}>()

const { token } = theme.useToken()

const headStyle = computed(() => ({
  gap: `${token.value.marginXS}px`,
}))

const iconStyle = computed(() => ({
  width: `${token.value.controlHeight - 4}px`,
  height: `${token.value.controlHeight - 4}px`,
  borderRadius: `${token.value.borderRadiusLG}px`,
  fontSize: `${token.value.fontSizeLG}px`,
}))

const titleStyle = computed(() => ({
  fontSize: `${token.value.fontSizeLG}px`,
  lineHeight: token.value.lineHeightLG,
  color: token.value.colorText,
}))

const subtitleStyle = computed(() => ({
  fontSize: `${token.value.fontSizeSM}px`,
  lineHeight: token.value.lineHeightSM,
  color: token.value.colorTextQuaternary,
}))
</script>

<template>
  <div class="card-head admin-panel-title" :style="headStyle">
    <span class="card-title__icon" :class="iconClass" :style="iconStyle">
      <slot name="icon" />
    </span>
    <div class="admin-panel-title__body">
      <span class="card-title" :style="titleStyle">
        <slot />
      </span>
      <p
        v-if="subtitle || $slots.subtitle"
        class="card-subtitle"
        :class="subtitleClass"
        :style="subtitleStyle"
      >
        <slot name="subtitle">{{ subtitle }}</slot>
      </p>
    </div>
  </div>
</template>

<style scoped>
.admin-panel-title {
  display: flex;
  align-items: center;
  min-width: 0;
}

.admin-panel-title__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.card-title {
  display: block;
  font-weight: 600;
}

.card-title__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.card-subtitle {
  margin: 0;
}
</style>
