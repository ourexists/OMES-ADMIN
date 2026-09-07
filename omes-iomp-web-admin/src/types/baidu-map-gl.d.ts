/** 百度地图 WebGL JS API（动态加载 api.map.baidu.com） */
export {}

declare global {
  namespace BMapGL {
    interface Point {
      lng: number
      lat: number
    }

    interface Size {
      width: number
      height: number
    }

    interface IconOptions {
      anchor?: Size
      imageSize?: Size
    }

    interface Icon {
      setImageUrl(url: string): void
    }

    interface MarkerOptions {
      icon?: Icon
    }

    interface MapStyleV2 {
      styleId: string
      logoType?: number
    }

    interface Map {
      centerAndZoom(point: Point, zoom: number): void
      enableScrollWheelZoom(enable: boolean): void
      setMapStyleV2(style: MapStyleV2): void
      setCopyrightOffset?(
        logo: { width?: number; height?: number },
        cpy: { width?: number; height?: number },
      ): void
      removeEventListener?(event: string, handler: () => void): void
      addOverlay(overlay: unknown): void
      removeOverlay(overlay: unknown): void
      openInfoWindow(infoWindow: InfoWindow, point: Point): void
      closeInfoWindow(): void
      addEventListener(event: string, handler: (e?: MapClickEvent) => void): void
      setViewport(points: Point[], opts?: { margins?: number[] }): void
      checkResize(): void
      destroy(): void
    }

    interface MapClickEvent {
      latlng: { lat: number; lng: number }
    }

    interface Marker {
      setPosition(point: Point): void
      setIcon(icon: Icon): void
      addEventListener(event: string, handler: (e?: MapClickEvent) => void): void
    }

    interface InfoWindowOptions {
      width?: number
      height?: number
      title?: string
    }

    interface InfoWindow {
      setContent(content: string): void
      setTitle(title: string): void
    }

    interface GeocoderResult {
      address?: string
    }

    interface Geocoder {
      getLocation(point: Point, callback: (result: GeocoderResult) => void): void
    }

    interface Poi {
      point: Point
      title?: string
      address?: string
    }

    interface LocalSearchResult {
      getCurrentNumPois(): number
      getPoi(index: number): Poi
    }

    interface LocalSearch {
      search(keyword: string): void
    }

    const Map: new (container: string | HTMLElement) => Map
    const Point: new (lng: number, lat: number) => Point
    const Size: new (width: number, height: number) => Size
    const Marker: new (point: Point, opts?: MarkerOptions) => Marker
    const Icon: new (url: string, size: Size, opts?: IconOptions) => Icon
    const InfoWindow: new (content: string, opts?: InfoWindowOptions) => InfoWindow
    const Geocoder: new () => Geocoder
    const LocalSearch: new (
      map: Map,
      opts: { onSearchComplete: (results: LocalSearchResult) => void },
    ) => LocalSearch
  }

  interface Window {
    BMapGL: typeof BMapGL
    __omesBaiduMapReady?: () => void
  }
}
