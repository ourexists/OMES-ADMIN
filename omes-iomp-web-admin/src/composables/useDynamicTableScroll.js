import { nextTick, onMounted, onUnmounted, ref } from 'vue'

export function useDynamicTableScroll(minHeight = 160) {
  const listCardRef = ref()
  const tableScrollY = ref(300)
  let resizeObserver = null
  let animationFrame = null

  const getListCardElement = () => listCardRef.value?.$el || listCardRef.value

  const updateTableScrollY = () => {
    if (animationFrame) {
      cancelAnimationFrame(animationFrame)
    }

    nextTick(() => {
      animationFrame = requestAnimationFrame(() => {
        animationFrame = requestAnimationFrame(() => {
          const cardBody = getListCardElement()?.querySelector('.ant-card-body')
          if (!cardBody) return

          const tableWrappers = Array.from(cardBody.querySelectorAll('.ant-table-wrapper'))
          const tableWrapper = tableWrappers.find((item) => {
            const rect = item.getBoundingClientRect()
            return rect.width > 0 && rect.height > 0
          }) || tableWrappers[0]
          const tableHeader = tableWrapper?.querySelector('.ant-table-header')
            || tableWrapper?.querySelector('.ant-table-thead')
          const paginationEl = tableWrapper?.querySelector('.ant-table-pagination')
            || tableWrapper?.querySelector('.ant-pagination')
          if (!tableWrapper || !tableHeader) return

          const wrapperRect = tableWrapper.getBoundingClientRect()
          const headerRect = tableHeader.getBoundingClientRect()
          const paginationRect = paginationEl?.getBoundingClientRect()
          const paginationTop = paginationRect?.top || wrapperRect.bottom
          const availableHeight = paginationTop - headerRect.bottom

          tableScrollY.value = Math.max(minHeight, Math.floor(availableHeight))
        })
      })
    })
  }

  onMounted(() => {
    nextTick(() => {
      const cardBody = getListCardElement()?.querySelector('.ant-card-body')
      if (cardBody) {
        resizeObserver = new ResizeObserver(updateTableScrollY)
        resizeObserver.observe(cardBody)
      }
      window.addEventListener('resize', updateTableScrollY)
      updateTableScrollY()
    })
  })

  onUnmounted(() => {
    if (animationFrame) {
      cancelAnimationFrame(animationFrame)
    }
    resizeObserver?.disconnect()
    window.removeEventListener('resize', updateTableScrollY)
  })

  return {
    listCardRef,
    tableScrollY,
    updateTableScrollY
  }
}
