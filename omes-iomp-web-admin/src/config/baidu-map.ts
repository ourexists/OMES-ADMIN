/** 百度地图 AK，在 https://lbsyun.baidu.com/ 申请（浏览器端） */
let runtimeAk = ''

export function setBaiduMapRuntimeAk(ak?: string | null): void {
  runtimeAk = ak?.trim() ?? ''
}

export function getBaiduMapAk(): string {
  const fromEnv = (import.meta.env.VITE_BAIDU_MAP_AK as string | undefined)?.trim() ?? ''
  return runtimeAk || fromEnv
}

export function hasBaiduMapAk(): boolean {
  return getBaiduMapAk().length > 0
}

/** OMES 统一百度地图个性化样式（与 static/map/baiduMap.js 一致） */
export const BAIDU_MAP_STYLE_ID = '68689e65b1c93202c41642c4fafae1e9'

/** 设备大屏地图专用样式 */
export const BAIDU_MAP_SCREEN_STYLE_ID = '8b22e0149ff57c684b4d1831646ab583'

export function applyBaiduMapStyle(map: BMapGL.Map, styleId: string = BAIDU_MAP_STYLE_ID): void {
  try {
    map.setMapStyleV2({ styleId })
  } catch {
    /* ignore invalid or unsupported style */
  }
}

/**
 * 安全销毁 BMapGL：destroy() 内部偶发空对象抛错；
 * 同时清理未完成的 jsapi_log，避免回调时命名空间为空。
 */
export function safeDestroyBaiduMap(map: BMapGL.Map | null | undefined, containerId?: string): void {
  neutralizeBaiduLogCallbacks()
  if (!map) {
    return
  }
  try {
    map.closeInfoWindow?.()
  } catch {
    /* ignore */
  }
  try {
    map.clearOverlays?.()
  } catch {
    /* ignore */
  }
  try {
    map.destroy()
  } catch {
    /* BMapGL destroy 内部偶发空对象，忽略即可 */
  }
  if (containerId) {
    const host = document.getElementById(containerId)
    if (host) {
      host.replaceChildren()
    }
  }
  neutralizeBaiduLogCallbacks()
}

/** 移除未完成的埋点脚本，并把已有 logCbk* 置为 noop */
function neutralizeBaiduLogCallbacks(): void {
  if (typeof document !== 'undefined') {
    document.querySelectorAll('script[src*="jsapi_log"]').forEach((el) => {
      el.remove()
    })
  }
  const gl = window.BMapGL as unknown as Record<string, unknown> | null | undefined
  if (!gl || typeof gl !== 'object') {
    // 仅在命名空间已丢时放一个最小 stub，供迟到的 JSONP 调用，不包装真实 API
    if (window.BMapGL == null) {
      window.BMapGL = createLogCallbackStub() as unknown as typeof window.BMapGL
    }
    return
  }
  for (const key of Object.keys(gl)) {
    if (key.startsWith('logCbk') && typeof gl[key] === 'function') {
      gl[key] = () => undefined
    }
  }
}

function createLogCallbackStub(): Record<string, unknown> {
  return new Proxy(
    {},
    {
      get(target, prop) {
        if (typeof prop === 'string' && prop.startsWith('logCbk')) {
          return () => undefined
        }
        return (target as Record<PropertyKey, unknown>)[prop]
      },
      set(target, prop, value) {
        ;(target as Record<PropertyKey, unknown>)[prop] = value
        return true
      },
    },
  )
}

const ATTRIBUTION_HIDE_SELECTORS = [
  '.anchorBL',
  '.BMap_cpyCtrl',
  '[class*="copyright"]',
  '[class*="Copyright"]',
  '[class*="logo-text"]',
  'a[href*="map.baidu.com"]',
  'a[href*="api.map.baidu.com"]',
] as const

type MapWithCopyright = BMapGL.Map & {
  setCopyrightOffset?: (
    logo: { width?: number; height?: number },
    cpy: { width?: number; height?: number },
  ) => void
}

/** 在地图容器内隐藏百度 Logo / 版权 DOM */
export function hideBaiduMapAttribution(container: HTMLElement | string): void {
  const host = typeof container === 'string' ? document.getElementById(container) : container
  if (!host) {
    return
  }
  for (const selector of ATTRIBUTION_HIDE_SELECTORS) {
    host.querySelectorAll<HTMLElement>(selector).forEach((el) => {
      el.style.setProperty('display', 'none', 'important')
      el.style.setProperty('visibility', 'hidden', 'important')
      el.style.setProperty('opacity', '0', 'important')
      el.style.setProperty('pointer-events', 'none', 'important')
    })
  }
}

const attributionObserverByContainer = new Map<string, MutationObserver>()

/** 地图初始化后绑定：API 偏移 + DOM 隐藏 + 动态节点监听 */
export function bindBaiduMapAttributionHide(map: BMapGL.Map, containerId: string): () => void {
  const mapExt = map as MapWithCopyright
  try {
    mapExt.setCopyrightOffset?.({ width: 0, height: 0 }, { width: 0, height: 0 })
  } catch {
    /* ignore unsupported API */
  }

  const run = () => hideBaiduMapAttribution(containerId)
  run()
  map.addEventListener('tilesloaded', run)

  let observer = attributionObserverByContainer.get(containerId)
  if (!observer) {
    const host = document.getElementById(containerId)
    if (host) {
      observer = new MutationObserver(run)
      observer.observe(host, { childList: true, subtree: true })
      attributionObserverByContainer.set(containerId, observer)
    }
  }

  window.requestAnimationFrame(run)
  window.setTimeout(run, 120)
  window.setTimeout(run, 480)

  return () => {
    map.removeEventListener?.('tilesloaded', run)
    const obs = attributionObserverByContainer.get(containerId)
    if (obs) {
      obs.disconnect()
      attributionObserverByContainer.delete(containerId)
    }
  }
}

const SCRIPT_ID = 'baidu-map-gl-api'
/** 百度 GL API 官方要求的全局回调名 */
const CALLBACK_NAME = '__omesBaiduMapReady'

let loadPromise: Promise<void> | null = null

/** 引导脚本会先写入空对象，须等 Map 等核心类就绪后再初始化地图 */
export function isBMapGLReady(): boolean {
  return typeof window.BMapGL?.Map === 'function' && typeof window.BMapGL?.Point === 'function'
}

function resetBaiduMapLoader(): void {
  loadPromise = null
  document.getElementById(SCRIPT_ID)?.remove()
  neutralizeBaiduLogCallbacks()
  if (!isBMapGLReady()) {
    if (window.BMapGL == null) {
      window.BMapGL = createLogCallbackStub() as unknown as typeof window.BMapGL
    }
    delete window.__omesBaiduMapReady
  }
}

function waitForBMapGL(timeoutMs: number): Promise<void> {
  return new Promise((resolve, reject) => {
    if (isBMapGLReady()) {
      resolve()
      return
    }
    const started = Date.now()
    const timer = window.setInterval(() => {
      if (isBMapGLReady()) {
        window.clearInterval(timer)
        resolve()
        return
      }
      if (Date.now() - started >= timeoutMs) {
        window.clearInterval(timer)
        resetBaiduMapLoader()
        reject(new Error('BMapGL load timeout'))
      }
    }, 50)
  })
}

function attachScript(ak: string, resolve: () => void, reject: (err: Error) => void) {
  const prev = window.__omesBaiduMapReady
  window.__omesBaiduMapReady = () => {
    if (typeof prev === 'function') {
      prev()
    }
    if (isBMapGLReady()) {
      resolve()
      return
    }
    resetBaiduMapLoader()
    reject(new Error('BMapGL missing after callback'))
  }

  const script = document.createElement('script')
  script.id = SCRIPT_ID
  script.type = 'text/javascript'
  script.src = `https://api.map.baidu.com/api?v=1.0&type=webgl&ak=${encodeURIComponent(ak)}&callback=${CALLBACK_NAME}`
  script.onerror = () => {
    resetBaiduMapLoader()
    reject(new Error('baidu map script error'))
  }
  ;(document.head || document.body).appendChild(script)
}

/** 动态加载 BMapGL（须带 callback，见官方异步加载示例） */
export function loadBaiduMapGl(): Promise<void> {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('not in browser'))
  }
  if (isBMapGLReady()) {
    return Promise.resolve()
  }
  if (loadPromise) {
    return loadPromise
  }

  const ak = getBaiduMapAk()
  if (!ak) {
    return Promise.reject(new Error('baidu map AK missing'))
  }

  loadPromise = new Promise((resolve, reject) => {
    const existing = document.getElementById(SCRIPT_ID)
    if (existing) {
      waitForBMapGL(15000).then(resolve).catch(reject)
      return
    }

    attachScript(ak, resolve, reject)
  })

  return loadPromise.catch((err) => {
    loadPromise = null
    throw err
  })
}
