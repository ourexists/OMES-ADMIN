import request, { get, postForm } from '@/api/request'
import { gatewayApiPath } from '@/config/gateway'

export interface LocalFileEntry {
  name: string
  path: string
  directory: boolean
  size?: number | null
  lastModified?: number | null
  extension?: string
}

const FILE_URL_PREFIX = '/files/'

export function resolveLocalFileUrl(path?: string): string {
  if (!path?.trim()) {
    return ''
  }
  const trimmed = path.trim()
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
    return trimmed
  }
  if (trimmed.startsWith('/')) {
    return gatewayApiPath(trimmed)
  }
  return gatewayApiPath(`${FILE_URL_PREFIX}${trimmed.replace(/^\/+/, '')}`)
}

export function getLocalFileDownloadUrl(path: string): string {
  return gatewayApiPath(`/localFile/download?path=${encodeURIComponent(path)}`)
}

export function fetchLocalFileBrowse(dir?: string) {
  return get<LocalFileEntry[]>('/localFile/browse', dir ? { dir } : undefined)
}

export async function uploadLocalFile(file: File, dir?: string) {
  const formData = new FormData()
  formData.append('file', file)
  if (dir?.trim()) {
    formData.append('dir', dir.trim())
  }
  const { data } = await request.post<string>('/localFile/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return typeof data === 'string' ? data : ''
}

export function mkdirLocalFile(name: string, dir?: string) {
  return postForm<string>('/localFile/mkdir', {
    name,
    ...(dir?.trim() ? { dir: dir.trim() } : {}),
  })
}

export function renameLocalFile(path: string, newName: string) {
  return postForm<string>('/localFile/rename', { path, newName })
}

export function deleteLocalFile(path: string) {
  return postForm<boolean>('/localFile/delete', { path })
}

export function formatFileSize(size?: number | null): string {
  if (size == null || size < 0) {
    return '-'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(size < 10 * 1024 ? 1 : 0)} KB`
  }
  if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(size < 10 * 1024 * 1024 ? 1 : 0)} MB`
  }
  return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

export function isPreviewableImage(entry: LocalFileEntry): boolean {
  if (entry.directory) {
    return false
  }
  return ['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'].includes((entry.extension || '').toLowerCase())
}

export function splitDirPath(dir: string): string[] {
  return dir.split('/').map((part) => part.trim()).filter(Boolean)
}

export function joinDirPath(parts: string[]): string {
  return parts.filter(Boolean).join('/')
}
