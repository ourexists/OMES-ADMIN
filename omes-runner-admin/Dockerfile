# IDEA Docker：对本文件右键 Run；Build context = 本模块目录
# 运行前先 mvn package；并挂载仓库根目录 config → /app/config
# 单独跑容器时默认连宿主机中间件（host.docker.internal）；Compose 内会被服务名覆盖

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/omes-runner-admin-*.jar /app/app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m" \
    TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    db_host=host.docker.internal:5432 \
    db_schema=omes \
    db_username=postgres \
    db_password=postgres \
    REDIS_HOST=host.docker.internal \
    REDIS_PORT=6379 \
    REDIS_PASSWORD=waqwertyuiop! \
    RABBITMQ_HOST=host.docker.internal \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USERNAME=admin \
    RABBITMQ_PASSWORD=TyY6Df3bZe \
    RABBITMQ_VHOST=/ \
    OMES_INTERNAL_SERVICE_KEY=dev-internal-change-me \
    AI_BRIDGE_INTERNAL_KEY=dev-bridge-change-me

EXPOSE 10010

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:10010/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=GMT+08 -jar /app/app.jar"]
