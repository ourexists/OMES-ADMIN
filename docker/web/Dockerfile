# IDEA / Compose：仅托管已构建的 dist（先在宿主机执行 npx vite build）
# Nginx 反代 /oauth2、/open 等 API 到 compose 服务 sas:9400
FROM nginx:1.27-alpine

COPY docker/web/nginx.conf /etc/nginx/conf.d/default.conf
COPY omes-iomp-web-admin/dist/ /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
