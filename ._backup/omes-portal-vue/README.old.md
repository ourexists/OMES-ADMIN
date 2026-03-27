# omes-portal-vue

Vue3 + Element Plus + Vite 重构的 OMES Portal 前端，与原有 `omes-portal` 模板并存，新入口为 `/vue/`。

## 开发

```bash
npm install
npm run dev
```

### 前后端联调 / Proxy

开发时前端运行在 Vite 的 dev server（默认 `http://localhost:5173`），所有以 `/open`、`/oauth2`、`/acc`、`/permission`、`/mat` 等开头的请求会通过 **Vite 代理** 转发到后端，避免跨域并保证登录、验证码、菜单等接口走同一后端。

- **后端地址**：在 `.env.development` 中配置 `VITE_PROXY_TARGET`，默认 `http://localhost:10010`（与 `omes-portal` 的 `server.port` 一致）。
- **修改后端端口**：若后端端口不是 10010，只需改 `.env.development` 中的 `VITE_PROXY_TARGET`，例如 `VITE_PROXY_TARGET=http://localhost:8080`。
- **代理路径**：见 `vite.config.ts` 中的 `API_PREFIXES`，与 `omes-portal/static/js/global.js` 中的 router 前缀一致。

联调前请先启动 `omes-portal` 后端，再执行 `npm run dev`。

## 构建与部署

```bash
npm run build
```

构建产物输出到 `omes-portal/src/main/resources/static/vue/`，由 Spring Boot 提供静态资源，访问 `/vue/` 进入新前端。

---

Vue 3 + TypeScript + Vite 模板说明见 [Vue Docs TypeScript Guide](https://vuejs.org/guide/typescript/overview.html#project-setup)。
