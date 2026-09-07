# omes-iomp-app

基于 uni-app x 的移动端，面向小程序和 Android。公共后端环境见 [项目总览](../readme.md)，浏览器管理端见 [omes-iomp-web-admin](../omes-iomp-web-admin/README.md)。

## 开发与运行

1. 使用支持 uni-app x 的 HBuilderX 打开本目录。
2. 在本目录执行 `npm install` 安装项目依赖。
3. 修改接口配置后，通过 HBuilderX 选择目标平台运行或发行；小程序调试需配合对应开发者工具。

当前 [package.json](package.json) 的 `scripts` 为空，未提供 `npm run dev` 或 `npm run build` 命令。项目中的 Vite 配置依赖 uni-app 工具链。

## 接口配置

- [config/proxy.ts](config/proxy.ts)：`dev` / `prod` 的 `target` 和 `authTarget`。当前开发地址均为 `http://127.0.0.1:9400`，生产地址需按部署环境修改。
- [config/index.ts](config/index.ts)：按运行环境生成 `baseUrl` 和 `authBaseUrl`，并配置名称、语言、平台标识等。
- [core/service/index.ts](core/service/index.ts)：OAuth2、验证码使用 `authBaseUrl`，业务请求使用 `baseUrl`；默认均连接 SAS 统一网关。
- [vite.config.ts](vite.config.ts)：H5 开发端口 9990，`/api` 去掉前缀后代理至 `http://127.0.0.1:9400`。当前接口配置使用绝对网关地址；需要走开发代理时，将开发接口地址对应设为 `http://127.0.0.1:9990/api`。

真机或小程序运行时，应使用设备可访问的网关地址；`127.0.0.1` 指向运行设备自身。

## 主要文件

| 路径 | 用途 |
| --- | --- |
| [manifest.json](manifest.json) | 应用标识与平台配置 |
| [pages.json](pages.json) | 页面与导航 |
| [theme.json](theme.json) | 主题配置 |
| `core/` | API、服务和公共逻辑 |
| `uni_modules/` | uni-app 扩展模块 |
| `unpackage/` | 工具链生成的编译产物，已忽略 |
