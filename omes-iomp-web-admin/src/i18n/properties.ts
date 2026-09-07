export function parseProperties(text: string): Record<string, string> {
  const result: Record<string, string> = {}

  for (const rawLine of text.split('\n')) {
    const line = rawLine.trim()
    if (!line || line.startsWith('#')) {
      continue
    }

    const separatorIndex = line.indexOf('=')
    if (separatorIndex <= 0) {
      continue
    }

    const key = line.slice(0, separatorIndex).trim()
    const value = line.slice(separatorIndex + 1).trim()
    if (key) {
      result[key] = value
    }
  }

  return result
}

export function nestPropertyMessages(flat: Record<string, string>): Record<string, unknown> {
  const result: Record<string, unknown> = {}

  for (const [key, value] of Object.entries(flat)) {
    const parts = key.split('.')
    let current = result

    for (let index = 0; index < parts.length - 1; index += 1) {
      const part = parts[index]
      if (!current[part] || typeof current[part] !== 'object') {
        current[part] = {}
      }
      current = current[part] as Record<string, unknown>
    }

    current[parts[parts.length - 1]] = value
  }

  return result
}

export function deepMergeMessages(
  target: Record<string, unknown>,
  source: Record<string, unknown>,
): Record<string, unknown> {
  const merged = { ...target }

  for (const [key, value] of Object.entries(source)) {
    const existing = merged[key]
    if (
      value &&
      typeof value === 'object' &&
      !Array.isArray(value) &&
      existing &&
      typeof existing === 'object' &&
      !Array.isArray(existing)
    ) {
      merged[key] = deepMergeMessages(existing as Record<string, unknown>, value as Record<string, unknown>)
      continue
    }
    merged[key] = value
  }

  return merged
}
