const CSS_HREF = '/static/vendor/meta2d/meta2d.css'
const JS_SRC = '/static/vendor/meta2d/meta2d.min.js'

let loadPromise: Promise<void> | null = null

function appendStylesheet(href: string) {
  if (document.querySelector(`link[href="${href}"]`)) {
    return
  }
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = href
  document.head.appendChild(link)
}

export function loadMeta2dAssets(): Promise<void> {
  if (typeof window !== 'undefined' && window.Meta2d) {
    return Promise.resolve()
  }
  if (!loadPromise) {
    loadPromise = new Promise((resolve, reject) => {
      appendStylesheet(CSS_HREF)
      const script = document.createElement('script')
      script.src = JS_SRC
      script.async = true
      script.onload = () => {
        if (window.Meta2d) {
          resolve()
        } else {
          reject(new Error('Meta2d script loaded but Meta2d is undefined (stub or invalid dist)'))
        }
      }
      script.onerror = () => reject(new Error('Failed to load Meta2d script'))
      document.head.appendChild(script)
    })
  }
  return loadPromise
}

export function isMeta2dReady(): boolean {
  return typeof window !== 'undefined' && Boolean(window.Meta2d)
}
