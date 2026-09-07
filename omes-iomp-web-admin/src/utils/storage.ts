export function getItem(key: string): string | null {
  return localStorage.getItem(key)
}

export function setItem(key: string, value: string): void {
  localStorage.setItem(key, value)
}

export function removeItem(key: string): void {
  localStorage.removeItem(key)
}

export function getJson<T>(key: string): T | null {
  const raw = getItem(key)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setJson(key: string, value: unknown): void {
  setItem(key, JSON.stringify(value))
}
