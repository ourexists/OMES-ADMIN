import type { Router, RouteRecordRaw } from 'vue-router'
import { translateText } from '@/i18n'
import { constantViewRouteName } from './view-map'

const LAYOUT_ROUTE_NAME = 'LayoutRoot'
const dynamicRouteNames = new Set<string | symbol>()
const standaloneRouteNames = new Set<string | symbol>()

export function resetDynamicRoutes(router: Router) {
  dynamicRouteNames.forEach((name) => {
    router.removeRoute(name)
  })
  dynamicRouteNames.clear()
  standaloneRouteNames.forEach((name) => {
    router.removeRoute(name)
  })
  standaloneRouteNames.clear()
}

export function addDynamicRoutes(router: Router, routes: RouteRecordRaw[]) {
  routes.forEach((route) => {
    const fullPath = route.path.startsWith('/') ? route.path : `/${route.path}`
    const placeholderName = constantViewRouteName(fullPath)
    if (router.hasRoute(placeholderName)) {
      router.removeRoute(placeholderName)
    }
    router.addRoute(LAYOUT_ROUTE_NAME, route)
    if (route.name) {
      dynamicRouteNames.add(route.name)
    }
  })
}

export function addStandaloneRoutes(router: Router, routes: RouteRecordRaw[]) {
  routes.forEach((route) => {
    // 常量预注册的独立页已存在，勿重复 add / 勿纳入 reset 移除
    if (route.name && router.hasRoute(route.name)) {
      return
    }
    router.addRoute(route)
    if (route.name) {
      standaloneRouteNames.add(route.name)
    }
  })
}

export function addNotFoundRoute(router: Router) {
  if (dynamicRouteNames.has('NotFound')) {
    return
  }
  router.addRoute(LAYOUT_ROUTE_NAME, {
    path: ':pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: translateText('error.pageNotFound', '页面不存在'), public: true },
  })
  dynamicRouteNames.add('NotFound')
}
