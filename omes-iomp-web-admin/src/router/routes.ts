import type { RouteRecordRaw } from 'vue-router'
import { buildConstantStandaloneViewRoutes, buildConstantViewRoutes } from './view-map'

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/view/login',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true },
  },
  // 独立全屏页需在根上预注册，不能挂 BasicLayout 子路由
  ...buildConstantStandaloneViewRoutes(),
  {
    path: '/',
    name: 'LayoutRoot',
    component: () => import('@/layouts/BasicLayout.vue'),
    redirect: '/view/overview',
    children: [
      ...buildConstantViewRoutes(),
      {
        path: '403',
        name: 'Forbidden',
        component: () => import('@/views/error/403.vue'),
        meta: { title: '无权限', public: true },
      },
    ],
  },
]
