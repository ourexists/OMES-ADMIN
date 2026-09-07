import { getLocalFileDownloadUrl } from '@/api/local-file'
import { gatewayApiPath } from '@/config/gateway'

export function isFileStorageRef(stored: unknown): boolean {
  if (!stored) return false
  if (Array.isArray(stored)) return stored.some(isFileStorageRef)
  const value = String(stored).trim()
  if (
    value.startsWith('data:') ||
    value.startsWith('blob:') ||
    value.startsWith('http://') ||
    value.startsWith('https://')
  ) {
    return false
  }
  if (value.startsWith('/localFile/download') || value.includes('localFile/download?path=')) {
    return true
  }
  return !value.includes('..') && !value.startsWith('/') && value.length <= 512
}

export function resolveFileAccessUrl(stored: unknown): string {
  if (!isFileStorageRef(stored)) return ''
  const value = String(stored).trim()
  if (value.includes('localFile/download') || value.startsWith('http')) {
    return value.startsWith('http') ? value : gatewayApiPath(value)
  }
  return getLocalFileDownloadUrl(value)
}

export function pickFileAccessUrl(stored: unknown, accessUrl?: unknown): string {
  if (Array.isArray(accessUrl)) {
    const hit = accessUrl.find(Boolean)
    return hit ? String(hit).trim() : resolveFileAccessUrl(stored)
  }
  if (accessUrl) {
    const url = String(accessUrl).trim()
    return url.startsWith('http') || url.startsWith('/') ? gatewayApiPath(url) : url
  }
  return resolveFileAccessUrl(stored)
}

export function splitFileStorageRefs(stored: unknown): string[] {
  if (!stored) return []
  if (Array.isArray(stored)) {
    return stored.map((item) => String(item || '').trim()).filter(isFileStorageRef)
  }
  return String(stored)
    .split(/[,，]/)
    .map((item) => item.trim())
    .filter(isFileStorageRef)
}
