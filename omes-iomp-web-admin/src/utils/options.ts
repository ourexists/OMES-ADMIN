export function mapOptions(source?: Record<string, string>) {
  if (!source) {
    return []
  }
  return Object.entries(source).map(([value, label]) => ({
    value: Number.isNaN(Number(value)) ? value : Number(value),
    label,
  }))
}
