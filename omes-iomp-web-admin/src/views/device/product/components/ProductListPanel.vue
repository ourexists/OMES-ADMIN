<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  DeleteOutlined,
  EditOutlined,
  PictureOutlined,
  SearchOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import type { ProductRecord } from '@/api/product'
import { fetchProductPage, resolveProductImageUrl } from '@/api/product'

const props = defineProps<{
  modelValue?: ProductRecord | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ProductRecord | null]
  change: [value: ProductRecord | null]
  loaded: [list: ProductRecord[], total: number]
  edit: []
  attrConfig: []
  delete: []
}>()

const { t } = useI18n()

const loading = ref(false)
const keyword = ref('')
const products = ref<ProductRecord[]>([])
const total = ref(0)
const brokenImages = ref<Set<string>>(new Set())
const selectedId = ref('')

const filteredList = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) {
    return products.value
  }
  return products.value.filter((item) => {
    const name = (item.name || '').toLowerCase()
    const code = (item.code || '').toLowerCase()
    return name.includes(q) || code.includes(q)
  })
})

const showEmpty = computed(() => !loading.value && products.value.length === 0)

function productImageUrl(record: ProductRecord): string {
  return resolveProductImageUrl(record.imageUrl)
}

function showImage(record: ProductRecord): boolean {
  const url = productImageUrl(record)
  return Boolean(url) && !brokenImages.value.has(record.id)
}

function onImageError(record: ProductRecord) {
  brokenImages.value.add(record.id)
}

function isActive(record: ProductRecord): boolean {
  return selectedId.value === record.id
}

function emitSelection(value: ProductRecord | null) {
  emit('update:modelValue', value)
  emit('change', value)
}

function syncSelected() {
  const current = props.modelValue
  if (!current?.id) {
    selectedId.value = ''
    return
  }
  selectedId.value = current.id
}

function onSelect(record: ProductRecord) {
  selectedId.value = record.id
  emitSelection(record)
}

function onEditClick(event: Event) {
  event.stopPropagation()
  emit('edit')
}

function onAttrClick(event: Event) {
  event.stopPropagation()
  emit('attrConfig')
}

function onDeleteClick(event: Event) {
  event.stopPropagation()
  emit('delete')
}

function pickDefault(list: ProductRecord[]) {
  if (!list.length) {
    selectedId.value = ''
    emitSelection(null)
    return
  }
  const currentId = props.modelValue?.id
  const hit = currentId ? list.find((item) => item.id === currentId) : null
  const next = hit || list[0]
  selectedId.value = next.id
  emitSelection(next)
}

async function loadList() {
  loading.value = true
  try {
    const result = await fetchProductPage({
      page: 1,
      pageSize: 500,
    })
    products.value = result.records || []
    total.value = result.total || products.value.length
    brokenImages.value = new Set()
    emit('loaded', products.value, total.value)
    syncSelected()
    pickDefault(products.value)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue?.id,
  () => {
    syncSelected()
  },
)

defineExpose({ reload: loadList })

onMounted(() => {
  syncSelected()
  loadList()
})
</script>

<template>
  <div class="product-list-panel">
    <a-input
      v-model:value="keyword"
      allow-clear
      size="middle"
      class="product-list-panel__search"
      :placeholder="t('productPage.productSearch')"
    >
      <template #prefix>
        <SearchOutlined class="input-prefix-icon" />
      </template>
    </a-input>

    <a-spin :spinning="loading" class="product-list-panel__spin">
      <div v-if="showEmpty" class="product-list-panel__empty-wrap">
        <a-empty :description="t('productPage.empty')" :image-style="{ height: '56px' }" />
      </div>
      <div v-else class="product-list-panel__list">
        <button
          v-for="item in filteredList"
          :key="item.id"
          type="button"
          class="product-item"
          :class="{ 'product-item--active': isActive(item) }"
          @click="onSelect(item)"
        >
          <div class="product-item__main">
            <div class="product-item__thumb">
              <img
                v-if="showImage(item)"
                :src="productImageUrl(item)"
                alt=""
                class="product-item__img"
                @error="onImageError(item)"
              />
              <PictureOutlined v-else class="product-item__placeholder" />
            </div>
            <div class="product-item__body">
              <span class="product-item__label">{{ item.name || '—' }}</span>
              <span v-if="item.code" class="product-item__code">{{ item.code }}</span>
            </div>
          </div>
          <div v-if="isActive(item)" class="product-item__actions" @click.stop>
            <a-tooltip :title="t('productPage.edit')">
              <a-button type="text" size="small" class="product-item__action-btn" @click="onEditClick">
                <EditOutlined />
                <span>{{ t('productPage.edit') }}</span>
              </a-button>
            </a-tooltip>
            <a-tooltip :title="t('productPage.attrConfig')">
              <a-button type="text" size="small" class="product-item__action-btn" @click="onAttrClick">
                <SettingOutlined />
                <span>{{ t('productPage.attrConfig') }}</span>
              </a-button>
            </a-tooltip>
            <a-tooltip :title="t('productPage.delete')">
              <a-button
                type="text"
                size="small"
                danger
                class="product-item__action-btn product-item__action-btn--danger"
                @click="onDeleteClick"
              >
                <DeleteOutlined />
                <span>{{ t('productPage.delete') }}</span>
              </a-button>
            </a-tooltip>
          </div>
        </button>
        <div
          v-if="!loading && !filteredList.length && keyword.trim()"
          class="product-list-panel__filter-empty"
        >
          {{ t('productPage.productSearchEmpty') }}
        </div>
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.product-list-panel {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  height: 100%;
}

.product-list-panel__search {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.product-list-panel__spin {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.product-list-panel__spin :deep(.ant-spin-container) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.product-list-panel__list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 6px;
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-lg);
  background: linear-gradient(180deg, #f7f8fb 0%, #f3f4f8 100%);
  scrollbar-width: thin;
  scrollbar-color: #d9d9d9 transparent;
}

.product-list-panel__list::-webkit-scrollbar {
  width: 6px;
}

.product-list-panel__list::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.product-list-panel__empty-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  border: 1px dashed var(--omes-color-border-hover);
  border-radius: var(--omes-radius-lg);
  background: var(--omes-color-bg-elevated);
}

.product-list-panel__filter-empty {
  padding: 28px 12px;
  text-align: center;
  font-size: 13px;
  color: var(--omes-color-text-quaternary);
}

.product-item {
  display: flex;
  flex-direction: column;
  width: 100%;
  margin: 0 0 6px;
  padding: 0;
  text-align: left;
  cursor: pointer;
  border-radius: var(--omes-radius-md);
  border: 1px solid transparent;
  background: var(--omes-color-bg-container);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition:
    background 0.15s,
    border-color 0.15s,
    box-shadow 0.15s;
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-item:hover {
  border-color: var(--omes-color-primary-border);
  box-shadow: 0 2px 8px rgba(0, 120, 212, 0.08);
}

.product-item--active {
  border-color: #91caff;
  background: linear-gradient(180deg, #f7fbff 0%, #ffffff 100%);
  box-shadow: 0 2px 10px rgba(0, 120, 212, 0.12);
}

.product-item--active .product-item__label {
  color: var(--omes-color-primary);
  font-weight: 600;
}

.product-item--active .product-item__thumb {
  border-color: var(--omes-color-primary-border);
  background: var(--omes-color-primary-bg);
}

.product-item__main {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
}

.product-item__thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 10px;
  border: 1px solid var(--omes-color-border-secondary);
  background: linear-gradient(145deg, var(--omes-color-bg-elevated) 0%, var(--omes-color-bg-layout) 100%);
}

.product-item__img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-item__placeholder {
  font-size: 18px;
  color: #d9d9d9;
}

.product-item__body {
  min-width: 0;
  flex: 1;
}

.product-item__label {
  display: block;
  font-size: 14px;
  line-height: 1.4;
  color: var(--omes-color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-item__code {
  display: inline-block;
  margin-top: 6px;
  max-width: 100%;
  padding: 1px 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--omes-color-primary);
  background: var(--omes-color-primary-bg);
  border: 1px solid var(--omes-color-primary-border);
  border-radius: var(--omes-radius-sm);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.product-item__actions {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 6px 8px 8px;
  border-top: 1px solid var(--omes-color-border);
  background: rgba(240, 249, 255, 0.55);
}

.product-item__action-btn {
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 28px !important;
  padding: 0 8px !important;
  border-radius: var(--omes-radius-sm);
  font-size: 12px !important;
  color: var(--omes-color-text-secondary);
}

.product-item__action-btn:hover {
  color: var(--omes-color-primary) !important;
  background: var(--omes-color-bg-container);
}

.product-item__action-btn--danger:hover {
  color: #ff4d4f !important;
  background: #fff1f0;
}

.product-item__action-btn :deep(.anticon) {
  font-size: 13px;
}

.product-item__action-btn span:not(.anticon) {
  line-height: 1;
}

.input-prefix-icon {
  color: var(--omes-color-text-placeholder);
}

@media (max-width: 420px) {
  .product-item__action-btn span:not(.anticon) {
    display: none;
  }
}
</style>
