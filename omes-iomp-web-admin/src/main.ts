import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import App from './App.vue'
import { i18n, setupI18n } from '@/i18n'
import { setupPermissionDirective } from './directives/permission'
import { setupAntdModalGlobal } from './plugins/antd-modal-global'
import { setupAdminComponents } from './plugins/admin-components'
import router from './router'
import pinia from './stores'
import { readStoredThemeVariant, applyThemeVariant } from '@/theme'
import 'ant-design-vue/dist/reset.css'
import './styles/tokens.css'
import './styles/index.css'
import './styles/equip-status.css'
import './styles/table-scroll.css'
import './styles/admin-search-toolbar.css'
import './styles/inspect-module.css'
import './styles/system-module.css'
import './styles/production-module.css'
import './styles/process-module.css'
import './styles/overview-page.css'
import './styles/admin-card-head.css'
import './styles/baidu-map-hide-logo.css'
import './styles/fluent-overrides.css'

applyThemeVariant(readStoredThemeVariant())

async function bootstrap() {
  const app = createApp(App)

  app.use(pinia)
  app.use(i18n)
  app.use(router)
  app.use(Antd)
  setupAntdModalGlobal(app)
  setupAdminComponents(app)
  setupPermissionDirective(app)

  await setupI18n()
  await router.isReady()
  app.mount('#app')
}

bootstrap()
