/** Exit fullscreen if active; ignores errors when the document is inactive (e.g. route unmount). */
export function safeExitFullscreen(element?: Element | null): void {
  const current = document.fullscreenElement
  if (!current) {
    return
  }
  if (element != null && current !== element) {
    return
  }
  void document.exitFullscreen().catch(() => {})
}
