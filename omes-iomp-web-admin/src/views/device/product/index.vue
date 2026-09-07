<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { AppstoreOutlined, ClusterOutlined, PlusOutlined } from '@ant-design/icons-vue'
import type { ProductRecord } from '@/api/product'
import { deleteProducts, resolveProductImageUrl } from '@/api/product'
import ProductListPanel from './components/ProductListPanel.vue'
import ProductModelPanel from './components/ProductModelPanel.vue'
import ProductFormModal from './components/ProductFormModal.vue'
import ProductAttrConfigModal from './components/ProductAttrConfigModal.vue'
import AdminPanelTitle from '@/components/admin/AdminPanelTitle.vue'
import { message, Modal } from 'ant-design-vue'

const { t } = useI18n()

const productPanelRef = ref<InstanceType<typeof ProductListPanel> | null>(null)

const selectedProduct = ref<ProductRecord | null>(null)
const productTotal = ref(0)
const brokenThumb = ref(false)

const formOpen = ref(false)
const editingRecord = ref<ProductRecord | null>(null)
const attrConfigOpen = ref(false)
const attrConfigRecord = ref<ProductRecord | null>(null)

const rightTitle = computed(() => {
  if (!selectedProduct.value?.name) {
    return t('productPage.modelsTitleDefault')
  }
  return t('productPage.modelsTitle', { name: selectedProduct.value.name })
})

const selectedThumb = computed(() => resolveProductImageUrl(selectedProduct.value?.imageUrl))

function onProductLoaded(_list: ProductRecord[], total: number) {
  productTotal.value = total
}

function onProductChange() {
  brokenThumb.value = false
}

function openCreate() {
  editingRecord.value = null
  formOpen.value = true
}

function openEdit() {
  if (!selectedProduct.value) {
    return
  }
  editingRecord.value = selectedProduct.value
  formOpen.value = true
}

function openAttrConfig() {
  if (!selectedProduct.value) {
    return
  }
  attrConfigRecord.value = selectedProduct.value
  attrConfigOpen.value = true
}

function confirmDeleteSelected() {
  const record = selectedProduct.value
  if (!record?.id) {
    return
  }
  Modal.confirm({
    title: t('productPage.deleteConfirm'),
    content: t('productPage.deleteContent', { count: 1 }),
    onOk: async () => {
      await deleteProducts([record.id])
      message.success(t('productPage.deleteSuccess'))
      selectedProduct.value = null
      await productPanelRef.value?.reload()
    },
  })
}

async function onProductSaved() {
  await productPanelRef.value?.reload()
}

async function onAttrSaved() {
  await productPanelRef.value?.reload()
}
</script>

<template>
  <div class="admin-page product-page">
    <a-row :gutter="16" class="admin-page-row page-body">
      <a-col :xs="24" :lg="8" :xl="7" class="admin-page-col">
        <a-card size="small" class="admin-panel-card panel-card product-card">
          <template #title>
            <AdminPanelTitle
              icon-class="card-title__icon--product"
              :subtitle="t('productPage.subtitleLeft')"
            >
              <template #icon><AppstoreOutlined /></template>
              {{ t('productPage.title') }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-space :size="6" wrap class="product-card__extra">
              <a-tag v-if="productTotal" color="processing" class="count-tag">
                {{ productTotal }}
              </a-tag>
              <a-button type="primary" size="small" @click="openCreate">
                <PlusOutlined />
                {{ t('productPage.add') }}
              </a-button>
            </a-space>
          </template>
          <div class="admin-panel-body product-card__body">
            <ProductListPanel
              ref="productPanelRef"
              v-model="selectedProduct"
              @loaded="onProductLoaded"
              @change="onProductChange"
              @edit="openEdit"
              @attr-config="openAttrConfig"
              @delete="confirmDeleteSelected"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="16" :xl="17" class="admin-page-col">
        <a-card size="small" class="admin-panel-card panel-card model-card">
          <template #title>
            <AdminPanelTitle
              icon-class="card-title__icon--model"
              :subtitle="selectedProduct ? t('productPage.subtitleRight') : undefined"
            >
              <template #icon><ClusterOutlined /></template>
              {{ rightTitle }}
            </AdminPanelTitle>
          </template>
          <template #extra>
            <a-tag v-if="selectedProduct?.code" class="code-tag" color="blue">
              {{ selectedProduct.code }}
            </a-tag>
          </template>
          <div class="admin-panel-body model-card__body">
            <div v-if="selectedProduct" class="product-summary">
              <div class="product-summary__thumb">
                <img
                  v-if="selectedThumb && !brokenThumb"
                  :src="selectedThumb"
                  alt=""
                  class="product-summary__img"
                  @error="brokenThumb = true"
                />
                <AppstoreOutlined v-else class="product-summary__placeholder" />
              </div>
              <div class="product-summary__meta">
                <div class="product-summary__name">{{ selectedProduct.name || '—' }}</div>
                <div class="product-summary__desc">{{ t('productPage.modelPanelHint') }}</div>
              </div>
            </div>
            <ProductModelPanel :product="selectedProduct" />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <ProductFormModal v-model:open="formOpen" :record="editingRecord" @success="onProductSaved" />
    <ProductAttrConfigModal
      v-model:open="attrConfigOpen"
      :record="attrConfigRecord"
      @success="onAttrSaved"
    />
  </div>
</template>

<style scoped>
.page-body {
  flex: 1;
  min-height: 0;
}

.panel-card {
  height: 100%;
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
  overflow: hidden;
}

.panel-card :deep(.ant-card-head) {
  min-height: 56px;
  padding-inline: 16px;
  border-bottom: 1px solid var(--omes-color-border);
  background: var(--omes-gradient-card-head);
}

.panel-card :deep(.ant-card-body) {
  padding: 16px;
}

.count-tag {
  margin-inline-end: 0;
  font-variant-numeric: tabular-nums;
}

.code-tag {
  margin-inline-end: 0;
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
}

.product-card :deep(.ant-card-body),
.model-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 280px);
}

.product-card__body,
.model-card__body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.product-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-bottom: 14px;
  padding: 12px 14px;
  border: 1px solid var(--omes-color-primary-border);
  border-radius: var(--omes-radius-md);
  background: linear-gradient(120deg, var(--omes-color-primary-bg) 0%, #ffffff 58%);
}

.product-summary__thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 10px;
  border: 1px solid var(--omes-color-primary-border);
  background: var(--omes-color-bg-container);
}

.product-summary__img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-summary__placeholder {
  font-size: 18px;
  color: var(--omes-color-primary);
}

.product-summary__meta {
  min-width: 0;
  flex: 1;
}

.product-summary__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--omes-color-text);
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-summary__desc {
  margin-top: 2px;
  font-size: 12px;
  color: var(--omes-color-text-tertiary);
  line-height: 1.4;
}

@media (max-width: 991px) {
  .product-card {
    margin-bottom: 16px;
  }

  .product-card :deep(.ant-card-body),
  .model-card :deep(.ant-card-body) {
    min-height: 420px;
  }
}
</style>
