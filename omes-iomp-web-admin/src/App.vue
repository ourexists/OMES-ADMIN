<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { ConfigProvider } from 'ant-design-vue'
import { useLocale } from '@/composables/useLocale'
import { useThemeStore } from '@/stores/theme'

const { antdLocale } = useLocale()
const themeStore = useThemeStore()
const { antdTheme } = storeToRefs(themeStore)
</script>

<template>
  <ConfigProvider :locale="antdLocale" :theme="antdTheme">
    <router-view />
  </ConfigProvider>
</template>
