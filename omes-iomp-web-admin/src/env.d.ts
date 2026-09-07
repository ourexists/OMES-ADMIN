/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_GATEWAY_PORT: string
  readonly VITE_SAS_BASE_URL?: string
  readonly VITE_BAIDU_MAP_AK?: string
  readonly VITE_ENABLE_REMOTE_I18N?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

export {}
