# IDEA Docker：对本文件右键 Run；Build context = 仓库根目录
# 运行前先 mvn package；并挂载仓库根目录 config → /app/config
# 单独跑容器时默认连宿主机中间件与 Admin（host.docker.internal）

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY omes-iomp-runner-sas/target/omes-iomp-runner-sas-*.jar /app/app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    OMES_GATEWAY_PORT=9400 \
    OMES_ADMIN_URL=http://host.docker.internal:10010 \
    OMES_GATEWAY_ENABLED=true \
    db_host=host.docker.internal:5432 \
    db_schema=omes \
    db_username=postgres \
    db_password=postgres \
    REDIS_HOST=host.docker.internal \
    REDIS_PORT=6379 \
    REDIS_PASSWORD=waqwertyuiop! \
    OMES_INTERNAL_SERVICE_KEY=dev-internal-change-me \
    AI_BRIDGE_INTERNAL_KEY=dev-bridge-change-me

EXPOSE 9400

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://127.0.0.1:9400/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=GMT+08 -jar /app/app.jar"]
