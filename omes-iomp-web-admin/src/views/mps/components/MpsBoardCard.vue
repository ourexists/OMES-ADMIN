<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { HolderOutlined } from '@ant-design/icons-vue'
import type { MpsRecord } from '@/types/mps'

const props = defineProps<{
  record: MpsRecord
  rank?: number
  tone?: 'pending' | 'queue-first' | 'queue' | 'running' | 'done'
  showDragHandle?: boolean
}>()

const { t } = useI18n()

function productName(record: MpsRecord): string {
  return record.moDto?.productName || '-'
}

function lineName(record: MpsRecord): string {
  return record.lineVo?.name || record.line || '-'
}
</script>

<template>
  <div
    class="board-card"
    :class="{
      'board-card--pending': tone === 'pending',
      'board-card--queue-first': tone === 'queue-first',
      'board-card--queue': tone === 'queue',
      'board-card--running': tone === 'running',
      'board-card--done': tone === 'done',
      'board-card--has-leading': !!$slots.leading,
    }"
  >
    <div v-if="$slots.leading" class="board-card__leading">
      <slot name="leading" />
    </div>

    <button
      v-if="showDragHandle"
      type="button"
      class="board-card__drag"
      :title="t('mpsPage.queueDrag')"
      :aria-label="t('mpsPage.queueDrag')"
    >
      <HolderOutlined />
    </button>

    <div v-if="rank != null" class="board-card__rank">
      <span class="rank-num">{{ rank }}</span>
    </div>

    <div v-if="$slots.trailing" class="board-card__trailing">
      <slot name="trailing" />
    </div>

    <div class="board-card__main">
      <h4 class="board-card__product">{{ productName(record) }}</h4>
      <dl class="board-card__meta">
        <div class="meta-item">
          <dt>{{ t('mpsPage.moCode') }}</dt>
          <dd class="meta-value--code" :title="record.moCode">{{ record.moCode || '—' }}</dd>
        </div>
        <div class="meta-item">
          <dt>{{ t('mpsPage.line') }}</dt>
          <dd :title="lineName(record)">{{ lineName(record) }}</dd>
        </div>
        <div class="meta-row">
          <div class="meta-item meta-item--half">
            <dt>{{ t('mpsPage.batch') }}</dt>
            <dd>{{ record.batch ?? '-' }}</dd>
          </div>
          <div v-if="record.weight != null" class="meta-item meta-item--half">
            <dt>{{ t('mpsPage.weight') }}</dt>
            <dd>{{ record.weight }}</dd>
          </div>
        </div>
        <div v-if="record.execTime && tone === 'done'" class="meta-item">
          <dt>{{ t('mpsPage.execTime') }}</dt>
          <dd :title="record.execTime">{{ record.execTime }}</dd>
        </div>
      </dl>
    </div>

    <div v-if="$slots.actions" class="board-card__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.board-card {
  display: grid;
  gap: 6px 8px;
  align-items: start;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  padding: 9px 10px;
  overflow: hidden;
  background: var(--omes-color-bg-container);
  border: 1px solid var(--omes-color-border-secondary);
  border-radius: var(--omes-radius-md);
  box-shadow: var(--omes-shadow-card-sm);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.board-card:hover {
  border-color: var(--omes-color-border-tertiary);
  box-shadow: var(--omes-shadow-card);
}

.board-card--pending {
  grid-template-columns: 1fr;
  grid-template-rows: auto auto;
  align-items: stretch;
}

.board-card--queue,
.board-card--queue-first {
  grid-template-columns: 18px 34px minmax(0, 1fr);
  grid-template-rows: auto auto;
  align-items: center;
}

.board-card--queue.board-card--has-leading,
.board-card--queue-first.board-card--has-leading {
  grid-template-columns: auto 18px 34px minmax(0, 1fr);
}

.board-card--queue-first {
  background: linear-gradient(
    135deg,
    var(--omes-color-success-bg) 0%,
    var(--omes-color-bg-container) 72%
  );
  border-color: var(--omes-color-success-border);
  box-shadow: var(--omes-shadow-success-soft);
}

.board-card--queue-first:hover {
  border-color: var(--omes-color-success);
}

.board-card--running {
  grid-template-columns: 1fr;
  grid-template-rows: auto auto;
  background: linear-gradient(
    135deg,
    var(--omes-color-primary-bg) 0%,
    var(--omes-color-bg-container) 72%
  );
  border-color: var(--omes-color-primary-border);
}

.board-card--running:hover {
  border-color: var(--omes-color-primary);
}

.board-card--done {
  grid-template-columns: 1fr;
  grid-template-rows: auto auto;
  opacity: 0.92;
}

.board-card__drag {
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: start;
  width: 18px;
  height: 30px;
  margin-top: 2px;
  padding: 0;
  font-size: 12px;
  color: var(--omes-color-text-placeholder);
  background: transparent;
  border: none;
  border-radius: var(--omes-radius-sm);
  cursor: grab;
  grid-row: 1;
  grid-column: 1;
}

.board-card__drag:hover {
  color: var(--omes-color-primary);
  background: var(--omes-color-primary-bg-hover);
}

.board-card__drag:active {
  cursor: grabbing;
}

.board-card--has-leading .board-card__drag {
  grid-column: 2;
}

.board-card__leading {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  grid-row: 1;
  grid-column: 1;
  align-self: center;
  padding-right: 4px;
}

.board-card__leading :deep(.icon-btn) {
  width: 28px;
  min-width: 28px;
  height: 28px;
  padding-inline: 0;
  border-radius: var(--omes-radius-sm);
}

.board-card__leading :deep(.icon-btn .anticon) {
  margin: 0;
  font-size: 13px;
}

.board-card__rank {
  display: flex;
  align-items: center;
  justify-content: center;
  align-self: start;
  grid-row: 1;
  grid-column: 2;
}

.board-card--has-leading .board-card__rank {
  grid-column: 3;
}

.rank-num {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  font-size: 14px;
  font-weight: 700;
  color: var(--omes-color-primary);
  background: var(--omes-color-primary-bg);
  border: 1.5px solid var(--omes-color-primary-border);
  border-radius: 50%;
}

.board-card--queue-first .rank-num {
  color: var(--omes-color-success);
  background: var(--omes-color-success-bg);
  border-color: var(--omes-color-success-border);
}

.board-card__main {
  grid-row: 1;
  grid-column: 3;
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
}

.board-card--has-leading.board-card--queue .board-card__main,
.board-card--has-leading.board-card--queue-first .board-card__main {
  grid-column: 4;
}

.board-card--pending .board-card__main {
  grid-row: 1;
  grid-column: 1;
  align-self: start;
}

.board-card__actions :deep(.board-action-btn) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 26px;
  padding-inline: 8px;
  font-size: 12px;
}

.board-card__actions :deep(.board-action-btn .anticon) {
  margin: 0;
  font-size: 12px;
}

.board-card--pending .board-card__actions {
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 6px;
}

.board-card--running .board-card__main,
.board-card--done .board-card__main {
  grid-row: 1;
  grid-column: 1;
}

.board-card__trailing {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  grid-row: 1;
  grid-column: 2;
  align-self: center;
  padding-left: 4px;
}

.board-card__trailing :deep(.icon-btn) {
  width: 28px;
  min-width: 28px;
  height: 28px;
  padding-inline: 0;
  border-radius: var(--omes-radius-sm);
  box-shadow: var(--omes-shadow-primary-icon);
}

.board-card__trailing :deep(.icon-btn .anticon) {
  margin: 0;
  font-size: 13px;
}

.board-card__product {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.35;
  color: var(--omes-color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.board-card__meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
  width: 100%;
  min-width: 0;
  margin: 0;
}

.meta-row {
  display: flex;
  gap: 8px;
  width: 100%;
  min-width: 0;
}

.meta-item {
  display: grid;
  grid-template-columns: 6em minmax(0, 1fr);
  gap: 4px;
  align-items: baseline;
  width: 100%;
  min-width: 0;
}

.meta-item--half {
  flex: 1;
  width: auto;
  min-width: 0;
}

.meta-item dt {
  margin: 0;
  font-size: 11px;
  line-height: 1.4;
  color: var(--omes-color-text-quaternary);
  white-space: nowrap;
}

.meta-item dd {
  margin: 0;
  min-width: 0;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.4;
  color: var(--omes-color-text-label);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-value--code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 11px;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.board-card__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  grid-column: 1 / -1;
  grid-row: 2;
  padding-top: 4px;
  margin-top: 2px;
  border-top: 1px solid var(--omes-color-border-secondary);
}

.board-card--queue .board-card__actions,
.board-card--queue-first .board-card__actions {
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 6px;
}

.board-card__actions :deep(.action-tools) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 1;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.board-card__actions :deep(.ant-btn-sm) {
  height: 22px;
  padding-inline: 6px;
  font-size: 12px;
  line-height: 20px;
}

.board-card__actions :deep(.ant-tag) {
  margin: 0;
  padding-inline: 5px;
  font-size: 11px;
  line-height: 18px;
}

.board-card__actions :deep(.icon-btn) {
  width: 22px;
  min-width: 22px;
  padding-inline: 0;
}

.board-card__actions :deep(.icon-btn .anticon) {
  margin: 0;
  font-size: 12px;
}

.board-card__actions :deep(.queue-status-tag) {
  flex-shrink: 0;
}
</style>
