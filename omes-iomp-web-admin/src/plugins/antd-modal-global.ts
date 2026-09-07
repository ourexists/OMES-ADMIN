import type { App, SetupContext } from 'vue'
import { h, mergeProps } from 'vue'
import { Modal } from 'ant-design-vue'
import type { ModalFuncProps } from 'ant-design-vue/es/modal/Modal'
import AntdModal from 'ant-design-vue/es/modal/Modal'

/** 全局弹窗：禁止点击遮罩关闭 */
export const GLOBAL_MODAL_MASK_CLOSABLE = false

/** 全局弹窗：垂直居中打开 */
export const GLOBAL_MODAL_CENTERED = true

const DRAG_BOUND_ATTR = 'data-omes-modal-drag'

type ModalApiName = 'info' | 'success' | 'error' | 'warning' | 'warn' | 'confirm'

function patchModalApi() {
  const modalApi = Modal as Record<ModalApiName, (config: ModalFuncProps) => ReturnType<typeof Modal.confirm>>
  const names: ModalApiName[] = ['info', 'success', 'error', 'warning', 'warn', 'confirm']
  for (const name of names) {
    const original = modalApi[name]
    if (typeof original !== 'function') {
      continue
    }
    modalApi[name] = (config: ModalFuncProps) =>
      original({
        ...config,
        maskClosable: GLOBAL_MODAL_MASK_CLOSABLE,
        centered: config.centered ?? GLOBAL_MODAL_CENTERED,
      })
  }
}

function resolveModalCentered(attrs: Record<string, unknown>): boolean {
  if ('centered' in attrs) {
    return attrs.centered !== false && attrs.centered !== 'false'
  }
  return GLOBAL_MODAL_CENTERED
}

function registerGlobalModalComponent(app: App) {
  const WrappedModal = {
    name: 'AModal',
    inheritAttrs: false,
    props: AntdModal.props,
    setup(props: Record<string, unknown>, { attrs, slots }: SetupContext) {
      return () =>
        h(
          AntdModal,
          mergeProps(attrs, props, {
            maskClosable: GLOBAL_MODAL_MASK_CLOSABLE,
            centered: resolveModalCentered(attrs),
          }),
          slots,
        )
    },
  }

  // Ant Design Vue 已在 app.use(Antd) 时注册 AModal，直接覆盖内部注册表避免重复注册警告
  type AppWithContext = App & { _context: { components: Record<string, unknown> } }
  const { components } = (app as AppWithContext)._context
  components.AModal = WrappedModal
  components['a-modal'] = WrappedModal
}

function getDragHandle(wrap: HTMLElement): HTMLElement | null {
  return (
    wrap.querySelector<HTMLElement>('.ant-modal-header')
    || wrap.querySelector<HTMLElement>('.ant-modal-confirm-title')
    || wrap.querySelector<HTMLElement>('.ant-modal-title')
  )
}

function bindModalDrag(wrap: HTMLElement) {
  if (wrap.getAttribute(DRAG_BOUND_ATTR)) {
    return
  }

  const modal = wrap.querySelector<HTMLElement>('.ant-modal')
  const handle = getDragHandle(wrap)
  if (!modal || !handle) {
    return
  }

  wrap.setAttribute(DRAG_BOUND_ATTR, '1')
  handle.classList.add('omes-modal-drag-handle')

  let dragging = false
  let startX = 0
  let startY = 0
  let startLeft = 0
  let startTop = 0

  const onMouseMove = (e: MouseEvent) => {
    if (!dragging) {
      return
    }
    modal.style.left = `${startLeft + e.clientX - startX}px`
    modal.style.top = `${startTop + e.clientY - startY}px`
  }

  const onMouseUp = () => {
    dragging = false
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  const onMouseDown = (e: MouseEvent) => {
    if (e.button !== 0) {
      return
    }
    if ((e.target as HTMLElement).closest('.ant-modal-close')) {
      return
    }

    const wrapRect = wrap.getBoundingClientRect()
    const modalRect = modal.getBoundingClientRect()
    dragging = true
    startX = e.clientX
    startY = e.clientY
    startLeft = modalRect.left - wrapRect.left
    startTop = modalRect.top - wrapRect.top

    modal.style.margin = '0'
    modal.style.position = 'absolute'
    modal.style.left = `${startLeft}px`
    modal.style.top = `${startTop}px`
    modal.style.transform = 'none'
    modal.style.paddingBottom = '0'

    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
    e.preventDefault()
  }

  handle.addEventListener('mousedown', onMouseDown)
}

function scanModalWraps() {
  document.querySelectorAll<HTMLElement>('.ant-modal-wrap').forEach((wrap) => {
    if (wrap.style.display === 'none') {
      return
    }
    bindModalDrag(wrap)
  })
}

function setupModalDragObserver() {
  if (typeof document === 'undefined') {
    return
  }
  const observer = new MutationObserver(() => {
    scanModalWraps()
  })
  observer.observe(document.body, { childList: true, subtree: true })
  scanModalWraps()
}

/**
 * 全局 Ant Design Vue 弹窗行为（在 app.use(Antd) 之后调用）：
 * - 所有 a-modal 与 Modal.xxx API 默认 maskClosable=false、centered=true
 * - 已打开的弹窗可通过标题栏拖拽位置
 */
export function setupAntdModalGlobal(app: App) {
  registerGlobalModalComponent(app)
  patchModalApi()
  setupModalDragObserver()
}
