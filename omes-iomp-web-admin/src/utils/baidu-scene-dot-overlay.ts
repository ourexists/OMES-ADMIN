import {
  SCENE_DOT_RIPPLE_PERIOD_MS,
  SCENE_DOT_RIPPLE_PHASE_OFFSET,
} from '@/utils/equip-screen-map'

export interface SceneDotOverlayOptions {
  color: string
  radius: number
  ripple: boolean
  onClick?: () => void
}

export interface SceneDotOverlayHandle {
  overlay: unknown
  setPoint: (point: BMapGL.Point) => void
  setStyle: (color: string, radius: number, ripple: boolean) => void
}

export function applySceneDotStyle(el: HTMLElement, color: string, radius: number, ripple: boolean) {
  el.style.setProperty('--dot-color', color)
  el.style.setProperty('--dot-r', `${radius}px`)
  el.style.setProperty('--dot-ripple-period', `${SCENE_DOT_RIPPLE_PERIOD_MS}ms`)
  el.style.setProperty(
    '--dot-ripple-delay-b',
    `-${SCENE_DOT_RIPPLE_PERIOD_MS * SCENE_DOT_RIPPLE_PHASE_OFFSET}ms`,
  )
  el.classList.toggle('screen-scene-dot--ripple', ripple)
}

function rebuildRippleLayers(div: HTMLElement, ripple: boolean) {
  div.querySelectorAll('.screen-scene-dot__ripple').forEach((node) => node.remove())
  if (!ripple) {
    return
  }
  const halo = div.querySelector('.screen-scene-dot__halo')
  const rippleB = document.createElement('span')
  rippleB.className = 'screen-scene-dot__ripple screen-scene-dot__ripple--b'
  const rippleA = document.createElement('span')
  rippleA.className = 'screen-scene-dot__ripple screen-scene-dot__ripple--a'
  if (halo) {
    div.insertBefore(rippleB, halo)
    div.insertBefore(rippleA, halo)
  } else {
    div.prepend(rippleA)
    div.prepend(rippleB)
  }
}

export function buildSceneDotElement(color: string, radius: number, ripple: boolean): HTMLElement {
  const root = document.createElement('div')
  root.className = 'screen-scene-dot'
  applySceneDotStyle(root, color, radius, ripple)

  if (ripple) {
    const rippleB = document.createElement('span')
    rippleB.className = 'screen-scene-dot__ripple screen-scene-dot__ripple--b'
    root.appendChild(rippleB)

    const rippleA = document.createElement('span')
    rippleA.className = 'screen-scene-dot__ripple screen-scene-dot__ripple--a'
    root.appendChild(rippleA)
  }

  const halo = document.createElement('span')
  halo.className = 'screen-scene-dot__halo'
  root.appendChild(halo)

  const core = document.createElement('span')
  core.className = 'screen-scene-dot__core'
  root.appendChild(core)

  return root
}

type OverlayMap = BMapGL.Map & {
  getPanes?: () => { markerPane?: HTMLElement }
  getContainer?: () => HTMLElement
  pointToOverlayPixel?: (point: BMapGL.Point) => { x: number; y: number }
  pointToPixel?: (point: BMapGL.Point) => { x: number; y: number }
}

interface SceneDotOverlayInstance {
  _point: BMapGL.Point
  _map: OverlayMap | null
  _div: HTMLElement | null
  initialize(map: OverlayMap): HTMLElement
  draw(): void
}

type SceneDotOverlayCtor = new (center: BMapGL.Point) => SceneDotOverlayInstance

export function createSceneDotOverlay(
  point: BMapGL.Point,
  options: SceneDotOverlayOptions,
): SceneDotOverlayHandle {
  const style = {
    color: options.color,
    radius: options.radius,
    ripple: options.ripple,
  }

  const OverlayBase = (window.BMapGL as unknown as { Overlay?: new () => object }).Overlay
  if (typeof OverlayBase !== 'function') {
    throw new Error('BMapGL.Overlay is not available')
  }

  function SceneDotOverlay(this: SceneDotOverlayInstance, center: BMapGL.Point) {
    this._point = center
    this._map = null
    this._div = null
  }

  SceneDotOverlay.prototype = new OverlayBase() as SceneDotOverlayInstance

  SceneDotOverlay.prototype.initialize = function initialize(map: OverlayMap) {
    this._map = map
    const div = buildSceneDotElement(style.color, style.radius, style.ripple)
    div.addEventListener('click', (event) => {
      event.stopPropagation()
      options.onClick?.()
    })
    const host = map.getPanes?.()?.markerPane ?? map.getContainer?.()
    if (host) {
      host.appendChild(div)
    }
    this._div = div
    return div
  }

  SceneDotOverlay.prototype.draw = function draw() {
    const map = this._map
    const div = this._div
    if (!map || !div) {
      return
    }
    const pixel = map.pointToOverlayPixel?.(this._point) ?? map.pointToPixel?.(this._point)
    if (!pixel) {
      return
    }
    div.style.left = `${pixel.x}px`
    div.style.top = `${pixel.y}px`
  }

  const overlay = new (SceneDotOverlay as unknown as SceneDotOverlayCtor)(point)

  return {
    overlay,
    setPoint(nextPoint: BMapGL.Point) {
      const current = overlay._point
      if (current.lng === nextPoint.lng && current.lat === nextPoint.lat) {
        return
      }
      overlay._point = nextPoint
      overlay.draw()
    },
    setStyle(color: string, radius: number, ripple: boolean) {
      if (style.color === color && style.radius === radius && style.ripple === ripple) {
        return
      }
      style.color = color
      style.radius = radius
      style.ripple = ripple
      const div = overlay._div
      if (!div) {
        return
      }
      applySceneDotStyle(div, color, radius, ripple)
      rebuildRippleLayers(div, ripple)
    },
  }
}
