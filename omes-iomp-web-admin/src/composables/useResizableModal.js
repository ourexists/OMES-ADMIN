import { onBeforeUnmount, ref } from 'vue'

const clamp = (value, min, max) => Math.min(max, Math.max(min, value))

export function useResizableModal(options = {}) {
  const {
    defaultWidth = 820,
    defaultHeight = 560,
    minWidth = 640,
    maxWidth = 1280,
    minHeight = 420,
    maxHeight = 920,
    storageKey = null
  } = options

  const modalWidth = ref(defaultWidth)
  const modalHeight = ref(defaultHeight)
  const resizing = ref(false)

  if (storageKey) {
    try {
      const raw = localStorage.getItem(storageKey)
      if (raw) {
        const saved = JSON.parse(raw)
        if (saved?.width) modalWidth.value = clamp(saved.width, minWidth, maxWidth)
        if (saved?.height) modalHeight.value = clamp(saved.height, minHeight, maxHeight)
      }
    } catch {
      // ignore invalid cache
    }
  }

  let removeListeners = null

  function persistSize() {
    if (!storageKey) return
    try {
      localStorage.setItem(storageKey, JSON.stringify({
        width: modalWidth.value,
        height: modalHeight.value
      }))
    } catch {
      // ignore quota errors
    }
  }

  function resetSize() {
    modalWidth.value = defaultWidth
    modalHeight.value = defaultHeight
    persistSize()
  }

  function startResize(event) {
    if (event.button !== 0) return
    resizing.value = true
    const startX = event.clientX
    const startY = event.clientY
    const startWidth = modalWidth.value
    const startHeight = modalHeight.value

    const onMouseMove = (moveEvent) => {
      modalWidth.value = clamp(startWidth + moveEvent.clientX - startX, minWidth, maxWidth)
      modalHeight.value = clamp(startHeight + moveEvent.clientY - startY, minHeight, maxHeight)
    }

    const onMouseUp = () => {
      resizing.value = false
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
      removeListeners?.()
      removeListeners = null
      persistSize()
    }

    document.body.style.cursor = 'nwse-resize'
    document.body.style.userSelect = 'none'
    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
    removeListeners = () => {
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
    }
  }

  onBeforeUnmount(() => {
    removeListeners?.()
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  })

  return {
    modalWidth,
    modalHeight,
    resizing,
    startResize,
    resetSize
  }
}
