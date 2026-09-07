import { nextTick, onMounted, onUnmounted, ref, watch, type Ref } from 'vue'

export interface UseTableScrollOptions {
  minHeight?: number
  bottomGap?: number
  /** Shrink table body to content when rows do not fill the available height */
  fitContent?: boolean
}

export function useTableScroll(options: UseTableScrollOptions = {}) {
  const minHeight = options.minHeight ?? 200
  const bottomGap = options.bottomGap ?? 8
  const fitContent = options.fitContent ?? false

  const wrapRef = ref<HTMLElement | null>(null)
  /** Undefined until first layout measure — avoids wrong scroll.y breaking header/body column sync. */
  const scrollY = ref<number>()
  /** True after the first successful height measurement (avoids applying a wrong scroll.y on open). */
  const scrollReady = ref(false)

  let resizeObserver: ResizeObserver | null = null
  let bodyResizeObserver: ResizeObserver | null = null
  let observedBody: HTMLElement | null = null
  let rafId = 0

  /** Sync header cell-scrollbar width with the body’s actual vertical scrollbar. */
  function syncScrollbarGutter() {
    const wrap = wrapRef.value
    if (!wrap) {
      return
    }
    const body = wrap.querySelector('.ant-table-body') as HTMLElement | null
    if (!body) {
      return
    }
    if (body !== observedBody) {
      bodyResizeObserver?.disconnect()
      observedBody = body
      bodyResizeObserver = new ResizeObserver(() => scheduleScrollUpdate())
      bodyResizeObserver.observe(body)
    }
    const gutter = Math.max(0, body.offsetWidth - body.clientWidth)
    if (gutter > 0) {
      wrap.style.setProperty('--omes-table-scrollbar-size', `${gutter}px`)
    } else {
      wrap.style.removeProperty('--omes-table-scrollbar-size')
    }
  }

  function updateScrollY() {
    const wrap = wrapRef.value
    if (!wrap || wrap.clientHeight < 80) {
      return
    }
    const paginationEl = wrap.querySelector('.ant-pagination, .table-pagination-bar') as HTMLElement | null
    const theadEl = wrap.querySelector('.ant-table-thead') as HTMLElement | null
    const tbodyEl = wrap.querySelector('.ant-table-tbody') as HTMLElement | null
    const paginationHeight = paginationEl?.offsetHeight ?? 0
    const theadHeight = theadEl?.offsetHeight ?? 47
    const contentHeight = tbodyEl?.scrollHeight ?? 0
    const rawAvailable = Math.max(0, wrap.clientHeight - paginationHeight - theadHeight - bottomGap)
    let nextY = Math.min(rawAvailable, Math.max(minHeight, rawAvailable))
    if (fitContent && contentHeight > 0) {
      nextY = Math.min(nextY, contentHeight)
    }

    if (scrollY.value == null || Math.abs(nextY - scrollY.value) > 1) {
      scrollY.value = nextY
    }
    scrollReady.value = true
    requestAnimationFrame(() => {
      syncScrollbarGutter()
      requestAnimationFrame(() => {
        syncScrollbarGutter()
        window.dispatchEvent(new Event('resize'))
      })
    })
  }

  function scheduleScrollUpdate() {
    if (rafId) {
      cancelAnimationFrame(rafId)
    }
    rafId = requestAnimationFrame(() => {
      rafId = 0
      updateScrollY()
    })
  }

  async function refreshScroll() {
    await nextTick()
    scheduleScrollUpdate()
  }

  watch(scrollY, async (value) => {
    if (value == null) {
      return
    }
    await nextTick()
    requestAnimationFrame(() => {
      syncScrollbarGutter()
      requestAnimationFrame(() => {
        syncScrollbarGutter()
        window.dispatchEvent(new Event('resize'))
      })
    })
  })

  onMounted(async () => {
    await nextTick()
    resizeObserver = new ResizeObserver(() => scheduleScrollUpdate())
    if (wrapRef.value) {
      resizeObserver.observe(wrapRef.value)
    }
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        scheduleScrollUpdate()
      })
    })
  })

  onUnmounted(() => {
    if (rafId) {
      cancelAnimationFrame(rafId)
    }
    resizeObserver?.disconnect()
    resizeObserver = null
    bodyResizeObserver?.disconnect()
    bodyResizeObserver = null
    observedBody = null
  })

  return {
    wrapRef,
    scrollY,
    scrollReady,
    refreshScroll,
    scheduleScrollUpdate,
    syncScrollbarGutter,
  } satisfies {
    wrapRef: Ref<HTMLElement | null>
    scrollY: Ref<number | undefined>
    scrollReady: Ref<boolean>
    refreshScroll: () => Promise<void>
    scheduleScrollUpdate: () => void
    syncScrollbarGutter: () => void
  }
}
