<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ApiOutlined,
  ClusterOutlined,
  GlobalOutlined,
  LineChartOutlined,
  LockOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import SliderCaptcha from '@/components/auth/SliderCaptcha.vue'
import { APP_ICON_ALT, APP_ICON_SRC } from '@/config/brand'
import { useLocale } from '@/composables/useLocale'
import type { AppLocale } from '@/i18n/locale'
import { reloadDynamicRoutes } from '@/router/dynamic'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t, locale, localeOptions, setLocale } = useLocale()

const loading = ref(false)
const captchaUuid = ref(String(Date.now()))
const captchaCode = ref('')
const formState = reactive({
  username: '',
  password: '',
})

const highlights = [
  { icon: ClusterOutlined, textKey: 'login.featureEquip' },
  { icon: LineChartOutlined, textKey: 'login.featureData' },
  { icon: ApiOutlined, textKey: 'login.featureOps' },
] as const

function refreshCaptcha() {
  captchaUuid.value = String(Date.now())
  captchaCode.value = ''
}

function onCaptchaSuccess(code: string) {
  captchaCode.value = code
}

function onCaptchaReset() {
  captchaCode.value = ''
}

function resolveRedirect(): string {
  const redirect = route.query.redirect
  if (typeof redirect !== 'string' || !redirect.startsWith('/')) {
    return '/view/overview'
  }
  return redirect
}

async function handleSubmit() {
  if (!formState.username || !formState.password) {
    message.warning(t('login.fillAll'))
    return
  }
  if (!captchaCode.value) {
    message.warning(t('login.sliderRequired'))
    return
  }

  loading.value = true
  try {
    await userStore.login(
      formState.username,
      formState.password,
      `${captchaUuid.value}-${captchaCode.value}`,
    )
    await reloadDynamicRoutes(router)
    message.success(t('login.success'))
    await router.replace(resolveRedirect())
  } catch (error) {
    const errMsg = error instanceof Error ? error.message : t('login.failed')
    message.error(errMsg)
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  userStore.logout()
})

function onLocaleChange({ key }: { key: string }) {
  setLocale(key as AppLocale)
}
</script>

<template>
  <div class="login-page">
    <div class="login-page__bg" aria-hidden="true">
      <div class="login-page__grid" />
      <div class="login-page__glow login-page__glow--primary" />
      <div class="login-page__glow login-page__glow--cyan" />
    </div>

    <header class="login-header">
      <a-dropdown placement="bottomRight">
        <button type="button" class="login-lang-btn">
          <GlobalOutlined />
          <span>{{ localeOptions.find((item) => item.value === locale)?.label }}</span>
        </button>
        <template #overlay>
          <a-menu :selected-keys="[locale]" @click="onLocaleChange">
            <a-menu-item v-for="item in localeOptions" :key="item.value">
              {{ item.label }}
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </header>

    <main class="login-main">
      <section class="login-brand">
        <div class="login-brand__logo-wrap">
          <img class="login-brand__logo" :src="APP_ICON_SRC" :alt="APP_ICON_ALT" />
        </div>
        <h1 class="login-brand__title">{{ t('app.systemName') }}</h1>
        <p class="login-brand__tagline">{{ t('app.tagline') }}</p>
        <p class="login-brand__desc">{{ t('app.desc') }}</p>
        <ul class="login-brand__highlights">
          <li v-for="item in highlights" :key="item.textKey" class="login-brand__highlight">
            <span class="login-brand__highlight-icon">
              <component :is="item.icon" />
            </span>
            <span>{{ t(item.textKey) }}</span>
          </li>
        </ul>
      </section>

      <section class="login-panel">
        <div class="login-card">
          <div class="login-card__head">
            <h2>{{ t('login.title') }}</h2>
            <p>{{ t('login.subtitle') }}</p>
          </div>

          <a-form
            class="login-form"
            layout="vertical"
            :model="formState"
            :required-mark="false"
            @finish="handleSubmit"
          >
            <a-form-item
              name="username"
              :rules="[{ required: true, message: t('login.usernameRequired') }]"
            >
              <a-input
                v-model:value="formState.username"
                size="large"
                :placeholder="t('login.username')"
                autocomplete="username"
              >
                <template #prefix><UserOutlined class="login-form__prefix" /></template>
              </a-input>
            </a-form-item>

            <a-form-item
              name="password"
              :rules="[{ required: true, message: t('login.passwordRequired') }]"
            >
              <a-input-password
                v-model:value="formState.password"
                size="large"
                :placeholder="t('login.password')"
                autocomplete="current-password"
              >
                <template #prefix><LockOutlined class="login-form__prefix" /></template>
              </a-input-password>
            </a-form-item>

            <a-form-item class="login-form__captcha">
              <div class="login-form__captcha-label">{{ t('login.captcha') }}</div>
              <SliderCaptcha
                :key="captchaUuid"
                :uuid="captchaUuid"
                @success="onCaptchaSuccess"
                @reset="onCaptchaReset"
              />
            </a-form-item>

            <a-button
              class="login-form__submit"
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
            >
              {{ t('login.submit') }}
            </a-button>
          </a-form>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: var(--omes-color-bg-standalone);
  color: #fff;
}

.login-page__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.login-page__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 80% 70% at 30% 40%, #000 20%, transparent 75%);
}

.login-page__glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
}

.login-page__glow--primary {
  top: -120px;
  left: -80px;
  width: 420px;
  height: 420px;
  background: rgba(22, 119, 255, 0.35);
}

.login-page__glow--cyan {
  right: -60px;
  bottom: -100px;
  width: 360px;
  height: 360px;
  background: rgba(19, 194, 194, 0.22);
}

.login-header {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: flex-end;
  padding: 20px 24px 0;
}

.login-lang-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.45);
  color: #e2e8f0;
  font-size: 13px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  transition: border-color 0.2s ease, background 0.2s ease;
}

.login-lang-btn:hover {
  border-color: rgba(22, 119, 255, 0.55);
  background: rgba(22, 119, 255, 0.12);
  color: #fff;
}

.login-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 56px;
  min-height: calc(100vh - 72px);
  padding: 24px 48px 48px;
}

.login-brand {
  flex: 1.15;
  max-width: 560px;
}

.login-brand__logo-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  margin-bottom: 28px;
  border-radius: 22px;
  background: linear-gradient(145deg, rgba(22, 119, 255, 0.22), rgba(19, 194, 194, 0.12));
  box-shadow:
    0 0 0 1px rgba(148, 163, 184, 0.16),
    0 16px 40px rgba(0, 0, 0, 0.28);
}

.login-brand__logo {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  object-fit: cover;
}

.login-brand__title {
  margin: 0 0 10px;
  font-size: clamp(28px, 4vw, 40px);
  font-weight: 700;
  letter-spacing: 0.02em;
  line-height: 1.2;
}

.login-brand__tagline {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 500;
  color: #94a3b8;
}

.login-brand__desc {
  margin: 0;
  max-width: 480px;
  line-height: 1.85;
  color: #cbd5e1;
  font-size: 15px;
}

.login-brand__highlights {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 32px 0 0;
  padding: 0;
  list-style: none;
}

.login-brand__highlight {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.42);
  color: #e2e8f0;
  font-size: 13px;
  backdrop-filter: blur(6px);
}

.login-brand__highlight-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(22, 119, 255, 0.18);
  color: #69b1ff;
  font-size: 13px;
}

.login-panel {
  flex: 0 1 440px;
  width: 100%;
  max-width: 440px;
}

.login-card {
  padding: 36px 32px 32px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow:
    0 24px 64px rgba(0, 0, 0, 0.28),
    0 0 0 1px rgba(255, 255, 255, 0.04) inset;
  color: var(--omes-color-text);
}

.login-card__head {
  margin-bottom: 28px;
  text-align: center;
}

.login-card__head h2 {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: var(--omes-color-text-heading);
}

.login-card__head p {
  margin: 0;
  color: var(--omes-color-text-quaternary);
  font-size: 14px;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.ant-input-affix-wrapper),
.login-form :deep(.ant-input) {
  border-radius: var(--omes-radius-md);
}

.login-form :deep(.ant-input-affix-wrapper-lg) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.login-form__prefix {
  color: var(--omes-color-text-quaternary);
}

.login-form__captcha {
  margin-bottom: 8px;
}

.login-form__captcha-label {
  margin-bottom: 8px;
  color: var(--omes-color-text-label);
  font-size: 14px;
}

.login-form__submit {
  height: 46px;
  margin-top: 8px;
  border: none;
  border-radius: var(--omes-radius-md);
  font-size: 16px;
  font-weight: 600;
  box-shadow: var(--omes-shadow-primary-icon);
}

.login-form__submit:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.35);
}

@media (max-width: 1024px) {
  .login-main {
    flex-direction: column;
    gap: 32px;
    padding: 16px 20px 40px;
    min-height: calc(100vh - 64px);
  }

  .login-brand {
    max-width: 100%;
    text-align: center;
  }

  .login-brand__desc {
    max-width: none;
  }

  .login-brand__highlights {
    justify-content: center;
  }

  .login-panel {
    flex: none;
    max-width: 460px;
  }
}

@media (max-width: 640px) {
  .login-header {
    padding: 16px 16px 0;
  }

  .login-main {
    padding: 12px 16px 32px;
  }

  .login-brand__logo-wrap {
    width: 72px;
    height: 72px;
    margin-bottom: 20px;
  }

  .login-brand__logo {
    width: 52px;
    height: 52px;
  }

  .login-brand__highlights {
    gap: 8px;
  }

  .login-brand__highlight {
    padding: 8px 12px;
    font-size: 12px;
  }

  .login-card {
    padding: 28px 20px 24px;
    border-radius: 16px;
  }

  .login-card__head h2 {
    font-size: 22px;
  }
}
</style>
