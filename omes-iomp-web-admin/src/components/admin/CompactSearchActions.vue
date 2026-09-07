<script setup lang="ts">
import { SearchOutlined, UndoOutlined } from '@ant-design/icons-vue'

withDefaults(
  defineProps<{
    queryTitle: string
    resetTitle: string
    loading?: boolean
    disabled?: boolean
  }>(),
  {
    loading: false,
    disabled: false,
  },
)

const emit = defineEmits<{
  reset: []
}>()
</script>

<template>
  <a-form-item class="compact-search-actions">
    <a-space :size="4">
      <a-tooltip :title="queryTitle">
        <a-button
          type="primary"
          size="small"
          html-type="submit"
          :loading="loading"
          :disabled="disabled"
        >
          <SearchOutlined />
        </a-button>
      </a-tooltip>
      <a-tooltip :title="resetTitle">
        <a-button size="small" :disabled="disabled" @click="emit('reset')">
          <UndoOutlined />
        </a-button>
      </a-tooltip>
      <slot />
    </a-space>
  </a-form-item>
</template>
