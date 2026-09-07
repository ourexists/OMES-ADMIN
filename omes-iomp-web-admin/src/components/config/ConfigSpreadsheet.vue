<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { DeleteOutlined } from '@ant-design/icons-vue'
import { parseClipboardBool, parseClipboardGrid } from '@/utils/spreadsheet-paste'

export type SpreadsheetColumnType = 'text' | 'number' | 'checkbox' | 'select' | 'readonly'

export interface SpreadsheetColumn {
  key: string
  title: string
  width?: number | string
  type?: SpreadsheetColumnType
  options?: { value: string | number; label: string }[]
  placeholder?: string
  readonlyWhen?: (row: Record<string, unknown>) => boolean
}

const props = withDefaults(
  defineProps<{
    columns: SpreadsheetColumn[]
    rows: Record<string, unknown>[]
    maxHeight?: string
    /** 表格滚动区固定高度（px），表头 sticky、底部提示固定 */
    bodyHeight?: number
    /** 在父级 flex 容器中撑满剩余高度，避免 JS 测算导致弹窗无限增高 */
    fill?: boolean
    showRowIndex?: boolean
    rowDeletable?: (row: Record<string, unknown>, index: number) => boolean
  }>(),
  {
    maxHeight: 'min(52vh, 520px)',
    showRowIndex: true,
  },
)

const fillParent = computed(() => props.fill === true)
const useBodyHeight = computed(() => !fillParent.value && props.bodyHeight != null && props.bodyHeight > 0)

const scrollStyle = computed(() => {
  if (fillParent.value) {
    return {
      flex: '1 1 auto',
      minHeight: '0',
      overflowX: 'auto' as const,
      overflowY: 'auto' as const,
    }
  }
  if (useBodyHeight.value) {
    return {
      height: `${props.bodyHeight}px`,
      maxHeight: `${props.bodyHeight}px`,
      overflowX: 'auto' as const,
      overflowY: 'scroll' as const,
    }
  }
  return { maxHeight: props.maxHeight }
})

const emit = defineEmits<{
  deleteRow: [index: number]
}>()

const { t } = useI18n()
const activeRow = ref(0)
const activeCol = ref(0)
const wrapRef = ref<HTMLElement | null>(null)

function focusCell(row: number, col: number) {
  const el = wrapRef.value?.querySelector<HTMLElement>(
    `[data-ss-row="${row}"][data-ss-col="${col}"]`,
  )
  el?.focus()
  activeRow.value = row
  activeCol.value = col
}

function onCellFocus(row: number, col: number) {
  activeRow.value = row
  activeCol.value = col
}

function moveCell(row: number, col: number, dr: number, dc: number) {
  let r = row + dr
  let c = col + dc
  if (c >= props.columns.length) {
    c = 0
    r += 1
  } else if (c < 0) {
    c = props.columns.length - 1
    r -= 1
  }
  if (r < 0 || r >= props.rows.length) {
    return
  }
  focusCell(r, c)
}

function onCellKeydown(ev: KeyboardEvent, row: number, col: number) {
  if (ev.key === 'Tab') {
    ev.preventDefault()
    moveCell(row, col, 0, ev.shiftKey ? -1 : 1)
  } else if (ev.key === 'Enter') {
    ev.preventDefault()
    moveCell(row, col, 1, 0)
  }
}

function isCellReadonly(col: SpreadsheetColumn, row: Record<string, unknown>) {
  if (col.type === 'readonly') {
    return true
  }
  return Boolean(col.readonlyWhen?.(row))
}

function canDeleteRow(row: Record<string, unknown>, idx: number) {
  if (!props.rowDeletable) {
    return true
  }
  return props.rowDeletable(row, idx)
}

function setCellValue(row: Record<string, unknown>, col: SpreadsheetColumn, raw: string) {
  if (isCellReadonly(col, row)) {
    return
  }
  if (col.type === 'checkbox') {
    const b = parseClipboardBool(raw)
    if (b !== undefined) {
      row[col.key] = b
    }
    return
  }
  if (col.type === 'number') {
    const n = Number(raw.trim())
    row[col.key] = raw.trim() === '' || Number.isNaN(n) ? undefined : n
    return
  }
  if (col.type === 'select' && col.options?.length) {
    const opt = col.options.find(
      (o) => String(o.value) === raw.trim() || o.label === raw.trim(),
    )
    row[col.key] = opt ? opt.value : raw.trim() === '' ? undefined : raw.trim()
    return
  }
  row[col.key] = raw
}

function onTablePaste(ev: ClipboardEvent) {
  const text = ev.clipboardData?.getData('text/plain') ?? ''
  if (!text || (!text.includes('\t') && !text.includes('\n'))) {
    return
  }
  ev.preventDefault()
  const grid = parseClipboardGrid(text)
  const startRow = activeRow.value
  const startCol = activeCol.value

  grid.forEach((line, ri) => {
    const rowIdx = startRow + ri
    if (rowIdx >= props.rows.length) {
      return
    }
    const row = props.rows[rowIdx]
    line.forEach((cell, ci) => {
      const colIdx = startCol + ci
      if (colIdx >= props.columns.length) {
        return
      }
      const col = props.columns[colIdx]
      if (isCellReadonly(col, row)) {
        return
      }
      setCellValue(row, col, cell)
    })
  })
}

function cellDisplay(row: Record<string, unknown>, col: SpreadsheetColumn): string {
  const v = row[col.key]
  if (v === null || v === undefined) {
    return '-'
  }
  if (col.type === 'select' && col.options) {
    const hit = col.options.find((o) => o.value === v)
    return hit?.label ?? String(v)
  }
  return String(v)
}
</script>

<template>
  <div
    ref="wrapRef"
    class="config-spreadsheet"
    :class="{
      'config-spreadsheet--body-height': useBodyHeight,
      'config-spreadsheet--fill': fillParent,
    }"
    @paste="onTablePaste"
  >
    <div class="ss-scroll" :style="scrollStyle">
    <table class="ss-table">
      <thead>
        <tr>
          <th v-if="showRowIndex" class="ss-th ss-th--idx">#</th>
          <th
            v-for="col in columns"
            :key="col.key"
            class="ss-th"
            :style="col.width ? { width: typeof col.width === 'number' ? `${col.width}px` : col.width } : undefined"
          >
            {{ col.title }}
          </th>
          <th class="ss-th ss-th--op">{{ t('configSpreadsheet.colAction') }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, rowIdx) in rows" :key="rowIdx" class="ss-tr">
          <td v-if="showRowIndex" class="ss-td ss-td--idx">{{ rowIdx + 1 }}</td>
          <td
            v-for="(col, colIdx) in columns"
            :key="col.key"
            class="ss-td"
            :class="{ 'ss-td--focused': activeRow === rowIdx && activeCol === colIdx }"
          >
            <template v-if="col.type === 'checkbox'">
              <div class="ss-cell ss-cell--center">
                <a-checkbox
                  :checked="!!row[col.key]"
                  :disabled="isCellReadonly(col, row)"
                  :data-ss-row="rowIdx"
                  :data-ss-col="colIdx"
                  @update:checked="(v: boolean) => (row[col.key] = v)"
                  @focus="onCellFocus(rowIdx, colIdx)"
                  @keydown="onCellKeydown($event, rowIdx, colIdx)"
                />
              </div>
            </template>
            <template v-else-if="isCellReadonly(col, row)">
              <div class="ss-cell ss-cell--readonly">{{ cellDisplay(row, col) }}</div>
            </template>
            <template v-else-if="col.type === 'select'">
              <a-select
                v-model:value="row[col.key]"
                :options="col.options"
                size="small"
                :bordered="false"
                class="ss-input ss-select"
                :placeholder="col.placeholder"
                :data-ss-row="rowIdx"
                :data-ss-col="colIdx"
                @focus="onCellFocus(rowIdx, colIdx)"
                @keydown="onCellKeydown($event, rowIdx, colIdx)"
              />
            </template>
            <template v-else-if="col.type === 'readonly'">
              <div class="ss-cell ss-cell--readonly">{{ cellDisplay(row, col) }}</div>
            </template>
            <template v-else>
              <input
                class="ss-input"
                type="text"
                :value="row[col.key] == null || row[col.key] === undefined ? '' : String(row[col.key])"
                :placeholder="col.placeholder"
                :data-ss-row="rowIdx"
                :data-ss-col="colIdx"
                @input="row[col.key] = ($event.target as HTMLInputElement).value"
                @focus="onCellFocus(rowIdx, colIdx)"
                @keydown="onCellKeydown($event, rowIdx, colIdx)"
              />
            </template>
          </td>
          <td class="ss-td ss-td--op">
            <a-button
              v-if="canDeleteRow(row, rowIdx)"
              type="text"
              danger
              size="small"
              :title="t('configSpreadsheet.deleteRow')"
              @click="emit('deleteRow', rowIdx)"
            >
              <DeleteOutlined />
            </a-button>
            <span v-else class="ss-lock">—</span>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="!rows.length" class="ss-empty">{{ t('configSpreadsheet.empty') }}</p>
    </div>
    <p class="ss-hint">{{ t('configSpreadsheet.pasteHint') }}</p>
  </div>
</template>

<style scoped>
.config-spreadsheet {
  border: 1px solid #c6c6c6;
  border-radius: 2px;
  background: var(--omes-color-bg-container);
  box-shadow: inset 0 0 0 1px var(--omes-color-border-hover);
}

.config-spreadsheet--body-height {
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.config-spreadsheet--fill {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.ss-scroll {
  overflow: auto;
}

.config-spreadsheet--body-height .ss-scroll {
  flex-shrink: 0;
}

.config-spreadsheet--fill .ss-scroll {
  flex: 1;
  min-height: 0;
}

.ss-hint {
  flex-shrink: 0;
}

.ss-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  font-size: 13px;
}

.ss-th {
  position: sticky;
  top: 0;
  z-index: 2;
  padding: 6px 8px;
  border: 1px solid #c6c6c6;
  background: linear-gradient(180deg, #f9f9f9 0%, #ececec 100%);
  font-weight: 600;
  color: #333;
  text-align: center;
  white-space: nowrap;
  user-select: none;
}

.ss-th--idx {
  width: 44px;
  min-width: 44px;
}

.ss-th--op {
  width: 48px;
  min-width: 48px;
}

.ss-tr:nth-child(even) .ss-td:not(.ss-td--idx) {
  background: var(--omes-color-bg-elevated);
}

.ss-tr:hover .ss-td:not(.ss-td--idx) {
  background: #e8f4ff;
}

.ss-td {
  padding: 0;
  border: 1px solid #d4d4d4;
  vertical-align: middle;
  height: 32px;
}

.ss-td--idx {
  width: 44px;
  text-align: center;
  background: #f3f3f3 !important;
  color: #666;
  font-size: 12px;
  user-select: none;
}

.ss-td--focused {
  box-shadow: inset 0 0 0 2px var(--omes-color-primary);
  z-index: 1;
}

.ss-td--op {
  text-align: center;
  background: #f9f9f9 !important;
}

.ss-lock {
  color: var(--omes-color-text-quaternary);
}

.ss-cell {
  min-height: 30px;
  display: flex;
  align-items: center;
  padding: 0 8px;
}

.ss-cell--center {
  justify-content: center;
}

.ss-cell--readonly {
  color: var(--omes-color-primary);
  font-weight: 600;
}

.ss-input {
  width: 100%;
  height: 30px;
  padding: 0 8px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  font-family: inherit;
  box-sizing: border-box;
}

.ss-input:focus {
  background: var(--omes-color-bg-container);
}

.ss-select {
  width: 100%;
}

.ss-select :deep(.ant-select-selector) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  min-height: 30px !important;
  padding: 0 6px !important;
}

.ss-empty {
  margin: 0;
  padding: 24px;
  text-align: center;
  color: var(--omes-color-text-quaternary);
}

.ss-hint {
  margin: 0;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--omes-color-text-quaternary);
  background: var(--omes-color-bg-elevated);
  border-top: 1px solid var(--omes-color-border-hover);
}

.config-spreadsheet--body-height .ss-scroll::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.config-spreadsheet--body-height .ss-scroll::-webkit-scrollbar-thumb {
  background: rgba(22, 119, 255, 0.35);
  border-radius: 5px;
}

.config-spreadsheet--body-height .ss-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(22, 119, 255, 0.55);
}
</style>
