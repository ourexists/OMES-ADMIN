/** Parse TSV/plain text from Excel clipboard */
export function parseClipboardGrid(text: string): string[][] {
  return text
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .split('\n')
    .filter((line) => line.length > 0)
    .map((line) => line.split('\t'))
}

export function parseClipboardBool(raw: string): boolean | undefined {
  const s = raw.trim().toLowerCase()
  if (['true', '1', 'yes', 'y', '是', '√'].includes(s)) {
    return true
  }
  if (['false', '0', 'no', 'n', '否', '×'].includes(s)) {
    return false
  }
  return undefined
}
