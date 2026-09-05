OMES 工业设备管理平台
===============

[![AUR](https://img.shields.io/badge/license-AGPL%203.0-blue.svg)]()
[![](https://img.shields.io/badge/Author-ourexists-orange.svg)]()
[![](https://img.shields.io/badge/version-1.0.1-brightgreen.svg)]()

介绍
-----------------------------------

本平台聚焦工业设备管理领域，构建涵盖设备全生命周期管理、统一数据采集与治理、实时分析与建模的技术体系。  
通过场景化建模与业务编排能力，支撑复杂工业场景下的数字化运营与决策优化。  
支持10w+设备接入架构。  


> 项目介绍：https://blog.ourexists.site/2026/01/30/omes/


平台生态
-----------------------------------

* **omes-runner-sas（OMES-SAS）**：统一 API 网关 + OAuth2 认证（默认端口 `9400`）。`/oauth2/**`、`/open/**` 由 SAS 本地处理，其余 API 转发至 Admin；**不托管前端静态资源**。

* **omes-runner-admin**：管理端 API 服务（Spring Boot，应用名 `OMES-ADMIN`，默认内网端口 `10010`），聚合各业务域服务；生产环境建议仅内网暴露。

* **omes-web-admin**：管理端 Vue 前端，独立构建部署（Nginx 等），经 `VITE_SAS_BASE_URL` 访问 SAS 网关。

* **omes-runner-control**：设备控制侧进程（默认端口 `10015`，应用名 `OMES-CONTROL`）。

* **omes-flinker-equip**：设备实时流（Spring Boot 内嵌 Flink），消费 RabbitMQ 并与管理端队列协同。

* **omes-ai / omes-ai-web**：AI 相关能力与前端（详见各子目录说明）。

交流与支持
-----------------------------------

- 微信： m15026681077

- 邮件： 434713950@163.com

版本对应表

| OMES Version     | 变更内容 | Era Framework Version | Layui Version | Spring Boot Version | JAVA Version |
|:-----------------|:---------|:----------------------|:--------------|:--------------------|:-------------|
| 1.0.1-SNAPSHOT   |          | 2024.0.2.0-SNAPSHOT   | 2.9.27        | 3.4.5               | 21           |

![img.png](架构图.png)

使用说明
-----------------------------------

### 1. 环境要求

- **JDK 21**（与版本表一致；Flink 作业模块单独要求 JDK 17，见 `omes-flinker-equip/README.md`）。

- **Maven 3.8+**（构建）。

- **PostgreSQL**（默认激活 `db-postgres`）。

- **Redis**（会话与缓存等）。

- **RabbitMQ**（设备实时流、Spring Cloud Stream 与 Flink 侧需同一套连接与 vhost）。

### 2. 克隆与子模块

首次克隆主仓库后需初始化子模块（含 **omes-sas** 认证服务）：

```bash
git clone https://gitee.com/ourexists/mes-edgev2.git
cd mes-edgev2
git submodule update --init --recursive
```

或一步克隆：`git clone --recurse-submodules https://gitee.com/ourexists/mes-edgev2.git`

`omes-sas` 独立仓库地址：`https://gitee.com/ourexists/omes-sas.git`（需在 Gitee 创建并 `git push` 后，他人方可通过子模块 URL 拉取）。

### 3. 本地配置

仓库根目录下的 **`config/config.properties`** 会被启动类加载（`@PropertySource("file:config/config.properties")`
），用于数据库等关键变量，例如：

- `db_host`、`db_schema`、`db_username`、`db_password`

- 按需覆盖 **Redis**、**RabbitMQ** 等（亦可通过同名环境变量注入，见 `omes-runner-admin/src/main/resources/application.yml`）

**运行时的当前工作目录**需能解析到上述 `config` 路径（一般在仓库根目录启动，或将 `config` 目录拷到与进程一致的位置）。

**认证安全（升级后必做）**：

1. 配置 `config/config.properties`（数据库等）与 `config/era-token.yml`（见 `era-token.yml.example`）。
2. **管理端前端**：在 `omes-web-admin` 执行 `npm run build`，将 `dist/` 部署到 Nginx 等静态服务器；`.env.production` 中配置 `VITE_SAS_BASE_URL` 指向 SAS 网关（如 `http://127.0.0.1:9400`）。
3. 设置环境变量（生产环境请使用强随机值）：
   - `OMES_INTERNAL_SERVICE_KEY` — Admin 与 SAS 内部服务调用密钥（一致）
   - `AI_BRIDGE_INTERNAL_KEY` — AI Bridge 解析密钥
4. 执行 OAuth2 公开客户端迁移（移除前端 `client_secret` 依赖）：

```bash
psql -h <host> -U <user> -d omes -f omes/scripts/oauth2-mes-public-client.sql
```

5. **统一网关（推荐）**：对外仅暴露 **OMES-SAS** 端口（默认 `9400`），Admin 保持内网 `10010`：

```bash
# SAS 网关 + 认证
OMES_GATEWAY_PORT=9400
OMES_ADMIN_URL=http://127.0.0.1:10010
java -jar omes-runner-sas/target/omes-runner-sas-*.jar

# Admin（内网，不对外）
java -jar omes-runner-admin/target/omes-runner-admin-*.jar
```

浏览器访问管理端：**前端静态站点**（Nginx 等）；API 网关：**http://127.0.0.1:9400**。关闭网关转发：`OMES_GATEWAY_ENABLED=false`（SAS 仅认证、Admin 直连）。

### 4. 构建

在仓库根目录执行（需能解析父 POM 与 **Era** 等依赖；若缺少私有制品仓库会构建失败）：

```bash

mvn clean package -DskipTests

```

管理端可执行包路径：`omes-runner-admin/target/omes-runner-admin-*.jar`（具体文件名以构建输出为准）。

### 5. 启动管理端（omes-runner-admin）

- **默认端口**：`10010`（可通过 `-Dserver.port=` 或环境变量覆盖）。

- **主类**：`com.ourexists.omes.AdminApp`。

**IDEA**：运行配置中 Main class 填 `com.ourexists.omes.AdminApp`，**Working directory** 设为**仓库根目录**（保证
`config/config.properties` 可读）。

**命令行示例**（在仓库根目录，保证 `./config` 存在）：

```bash

java -jar omes-runner-admin/target/omes-runner-admin-1.0.1-SNAPSHOT.jar

```

或使用 Spring Boot 插件（同样在仓库根执行便于读取 `config`）：

```bash

mvn -pl omes-runner-admin spring-boot:run

```

管理端页面由 **omes-web-admin** 独立部署；API 网关 **http://127.0.0.1:9400**；Admin API 内网调试 **http://127.0.0.1:10010**。

### 6. Flink 实时作业（可选）

设备实时流由 **`omes-flinker-equip`** 以 Spring Boot 可执行 JAR 运行（内嵌 Flink），与 RabbitMQ、管理端配置的队列名需一致。详见 **`omes-flinker-equip/README.md`**。

### 7. Docker 部署

统一入口：`docker/omes.ps1` / `docker/omes.sh`。Compose：`docker-compose.yml`。

**IDEA Docker 插件（推荐日常调试）**

1. 先起中间件：运行配置 **Compose: omes-infra**（或 `.\docker\omes.ps1 up infra`）
2. 打开 `omes-runner-admin/Dockerfile` 或 `omes-runner-sas/Dockerfile` → 右键 **Run**  
   （已提供 `.run/Dockerfile omes-admin|sas`：自动 Maven package，并挂载 `$PROJECT_DIR$/config` → `/app/config`）
3. 单独跑容器时默认通过 `host.docker.internal` 访问宿主机 Postgres/Redis/RabbitMQ/Admin

**命令行**

```powershell
.\docker\omes.ps1 build
.\docker\omes.ps1 up             # 或 up infra
.\docker\omes.ps1 logs admin
.\docker\omes.ps1 down
```

| 服务 | 默认地址 |
|:-----|:---------|
| Web | http://127.0.0.1:8080 |
| SAS | http://127.0.0.1:9400 |
| Admin（内网） | http://127.0.0.1:10010 |
| RabbitMQ 管理台 | http://127.0.0.1:15672 |

修改 `docker/.env` 中 `VITE_SAS_BASE_URL` 后需重新构建 Web 镜像。

---


> 项目依赖个人私有的框架包，无法下载直接运行。仅供开源功能参考，如需定制可联系我。


