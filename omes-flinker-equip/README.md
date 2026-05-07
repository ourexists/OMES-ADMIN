# omes-flinker-equip

设备实时流 Flink 作业：消费 RabbitMQ 设备实时队列，完成离线检测、变化检测、快照与属性波动告警，并回写 RabbitMQ。无 Web 进程，仅通过
**Flink 提交**运行。

## 构建

在仓库根目录执行（`omes-stream` 已内联原 `omes-device-base` / `omes-device-model` / `omes-message-model` 中与作业相关的类；若门户侧 DTO 变更，需手动同步本模块内对应源码）：

```bash
mvn -pl omes-flinker-equip clean package -DskipTests
```

产物：

- `target/omes-stream-<version>.jar` — 薄 JAR（类与资源，不含依赖）
- `target/omes-stream-<version>-flink.jar` — **提交到集群的 fat JAR**（`Main-Class` 已指向作业入口）

默认 `flink-streaming-java`、`flink-clients` 为 **provided**，由 Flink 集群提供，须与 `pom.xml` 中 **`flink.version`（当前
1.20.1，对应 Flink 1.20 线）** 一致。

本模块 **`maven.compiler.release` 为 17**（字节码 major 61），与 **Flink 1.20 官方要求的 Java 17** 一致；集群 JobManager /
TaskManager 请使用 **JDK 17**。

RabbitMQ Connector 版本见属性 **`flink.connector.rabbitmq.version`**；若 Central 上出现针对 1.20 的正式后缀版本，可只改该属性后重打包。

若需要把 Flink 一并打进 JAR（例如本地 `java -jar` 试跑），使用：

```bash
mvn -pl omes-flinker-equip clean package -DskipTests -Pflink-with-dependencies
```

## 提交作业

入口类：

```
com.ourexists.omes.stream.equip.EquipRealtimeFlinkJob
```

```bash
flink run -c com.ourexists.omes.stream.equip.EquipRealtimeFlinkJob /path/to/omes-flinker-equip-*-flink.jar
```

Flink Web UI：上传 `*-flink.jar`，Main Class 填上述全限定名。

## 本地调试（嵌入式 MiniCluster）

用于 IDE 断点或本机 `java -jar` 试跑：开启 **本地模式** 后使用嵌入式 Flink，而不是连接 `flink run` 提交到的远程集群。

1. 打包时带上 Flink 依赖（**不要**把该 profile 用于提交到生产集群的 fat JAR，以免与集群 Flink 重复打包核心类）：

   ```bash
   mvn -pl omes-flinker-equip clean package -DskipTests -Pflink-with-dependencies
   ```

2. 打开本地模式（任选其一）：

   - 程序参数：`--local true` 或 `--omes.device.flink.local true`
   - 环境变量：`OMES_FLINK_LOCAL=true`
   - JVM：`-Domes.device.flink.local=true`

3. 可选参数：

   | 变量 / 点分键 | 说明 | 默认 |
   |---------------|------|------|
   | `OMES_FLINK_LOCAL_PARALLELISM` / `omes.device.flink.local.parallelism` | 并行度 | `1` |
   | `OMES_FLINK_LOCAL_WEBUI` / `omes.device.flink.local.webui` | 是否起本地 Flink Dashboard；`false` 时用无 UI 的 `createLocalEnvironment` | `true` |

4. **IntelliJ IDEA（Java 17）**：在运行配置的 **VM options** 里加入 `--add-opens`（与官方 Flink 脚本一致；缺省时 Kryo 序列化可能报 `InaccessibleObjectException: ... java.util.Arrays$ArrayList`）：

   ```
   --add-opens java.base/java.lang=ALL-UNNAMED
   --add-opens java.base/java.lang.invoke=ALL-UNNAMED
   --add-opens java.base/java.util=ALL-UNNAMED
   --add-opens java.base/java.io=ALL-UNNAMED
   --add-opens java.base/java.net=ALL-UNNAMED
   --add-opens java.base/java.nio=ALL-UNNAMED
   --add-opens java.base/java.text=ALL-UNNAMED
   --add-opens java.base/java.time=ALL-UNNAMED
   --add-opens java.base/java.util.concurrent=ALL-UNNAMED
   --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED
   --add-opens java.base/java.util.concurrent.locks=ALL-UNNAMED
   --add-opens java.base/sun.nio.ch=ALL-UNNAMED
   ```

   **Program arguments** 示例：`--local true`。Maven 侧勾选 profile **`flink-with-dependencies`**，使 Flink 依赖进入模块 classpath。

5. **日志**：若出现 `log4j:WARN No appenders`，属于 Flink 直连 log4j 1.x；模块内已提供 `log4j.properties` 输出到控制台。应用侧 SLF4J 仍由 `logback.xml` 处理。

6. 在 IDE 中 **Run/Debug** 主类 `EquipRealtimeFlinkJob`。本地调试建议保持 **`OMES_FLINK_ENABLE_CHECKPOINTING=false`**（默认），与 README 中 RMQ checkpoint 说明一致；仍需可连通的 RabbitMQ 与队列配置。

## 配置说明

配置由 Flink `ParameterTool` 合并，优先级：**程序参数** → **JVM `-D` 系统属性** → **环境变量**。

### RabbitMQ

| 变量                  | 说明                         | 默认           |
|---------------------|----------------------------|--------------|
| `RABBITMQ_HOST`     | 主机                         | `127.0.0.1`  |
| `RABBITMQ_PORT`     | 端口                         | `5672`       |
| `RABBITMQ_USERNAME` | 用户名                        | `admin`      |
| `RABBITMQ_PASSWORD` | 密码（生产务必覆盖）                 | `TyY6Df3bZe` |
| `RABBITMQ_VHOST`    | 虚拟主机                       | `/`          |

### 队列与 Flink

| 变量                                              | 说明                                                                 | 默认 |
|-------------------------------------------------|--------------------------------------------------------------------|------|
| `OMES_EQUIP_REALTIME_QUEUE`                     | 设备实时消费队列                                                           | `omes.equip.realtime` |
| `OMES_EQUIP_NOTIFY_CREATE_QUEUE`                | 告警通知队列；设为空字符串则告警 Sink 不发送                                        | `omes.notify.create` |
| `OMES_EQUIP_STREAM_PERSIST_CHANGE_QUEUE`        | 运行/报警/在线变更桥接队列                                                    | `omes.equip.stream.persist.change` |
| `OMES_EQUIP_STREAM_PERSIST_STATE_QUEUE`         | 状态周期快照桥接队列                                                         | `omes.equip.stream.persist.state` |
| `OMES_EQUIP_STREAM_PERSIST_COLLECT_QUEUE`       | 采集周期快照桥接队列                                                         | `omes.equip.stream.persist.collect` |
| `OMES_FLINK_ENABLE_CHECKPOINTING`               | 是否开启 checkpoint（`true`/`false`）                                    | `false` |
| `OMES_FLINK_CHECKPOINT_MS`                      | checkpoint 间隔（毫秒）                                                  | `10000` |
| `OMES_FLINK_CHECKPOINT_TIMEOUT_MS`              | checkpoint 超时（毫秒）                                                  | `120000` |
| `OMES_FLINK_UNALIGNED_CHECKPOINT`               | 非对齐 checkpoint（减轻 barrier 对齐在背压下的阻塞）                             | `true` |
| `OMES_FLINK_ALIGNED_CHECKPOINT_TIMEOUT_MS`      | 先尝试对齐 barrier，超过该时间（毫秒）后切到非对齐；`0` 表示不设                         | `30000` |
| `OMES_FLINK_LOCAL`                              | `true`：嵌入式本地 Flink（调试）；见上文「本地调试」                                 | `false` |
| `OMES_FLINK_LOCAL_PARALLELISM`                  | 本地模式并行度                                                            | `1` |
| `OMES_FLINK_LOCAL_WEBUI`                        | 本地模式是否启用 Flink Web UI                                             | `true` |
| `OMES_FLINK_RMQ_PREFETCH`                       | RMQ prefetch                                                       | `100` |
| `OMES_FLINK_STATE_TTL_MINUTES_OFFLINE_DETECT`   | 离线检测算子 keyed state TTL：`-1` 不启用，正整数为空闲保留 **分钟**                  | `-1` |
| `OMES_FLINK_STATE_TTL_MINUTES_CHANGE_DETECT`    | 变更检测算子（同上）                                                        | `-1` |
| `OMES_FLINK_STATE_TTL_MINUTES_ATTR_FLUCTUATION` | 属性波动 MapState（同上）                                                  | `-1` |
| `OMES_FLINK_STATE_TTL_MINUTES_STATE_SNAPSHOT`   | 状态周期快照算子（同上）                                                      | `-1` |
| `OMES_FLINK_STATE_TTL_MINUTES_COLLECT_SNAPSHOT` | 采集周期快照算子（同上）                                                      | `-1` |

亦支持点分键名传参，例如：`omes.device.flink.enable-checkpointing`、`omes.device.flink.state-ttl-minutes.change-detect`。

点分键与上表对应示例：

- `omes.device.flink.change-detect-ingress-windowed` ← `OMES_FLINK_CHANGE_DETECT_INGRESS_WINDOWED`
- `omes.device.flink.change-detect-ingress-window-ms` ← `OMES_FLINK_CHANGE_DETECT_INGRESS_WINDOW_MS`
- `omes.device.flink.change-detect-ingress-slide-ms` ← `OMES_FLINK_CHANGE_DETECT_INGRESS_SLIDE_MS`

### 变化检测入口（可选窗口）

| 变量 | 说明 | 默认 |
|------|------|------|
| `OMES_FLINK_CHANGE_DETECT_INGRESS_WINDOWED` | `true`：变化检测入口先经 **处理时间滑动窗口 + reduce** 再进 `EquipRealtimeChangeDetectProcessFunction`，可压低上游频率，代价是端到端延迟约可达一个 slide；`false`：与离线检测 union 后逐条进入变化检测（与原先一致） | `false` |
| `OMES_FLINK_CHANGE_DETECT_INGRESS_WINDOW_MS` | 窗口长度（毫秒），`windowed=true` 时必须为正 | `60000` |
| `OMES_FLINK_CHANGE_DETECT_INGRESS_SLIDE_MS` | 滑动步长（毫秒），`windowed=true` 时必须为正且 **不大于** `WINDOW_MS` | `60000` |


### 业务参数

| 变量                                      | 说明                    | 默认 |
|-----------------------------------------|-----------------------|------|
| `OMES_EQUIP_OFFLINE_TIMEOUT_MS`         | 离线判定：无新设备数据入站的时长阈值（毫秒） | `90000` |
| `OMES_EQUIP_SNAPSHOT_INTERVAL_MS`       | 快照周期（毫秒）              | `30000` |
| `OMES_EQUIP_ATTR_FLUCTUATION_WINDOW_MS` | 属性波动窗口（毫秒）            | `90000` |
| `OMES_EQUIP_ATTR_FLUCTUATION_SLIDE_MS`  | 属性波动滑动步长（毫秒）          | `5000` |


## 常见问题

1. **集群缺 Flink 类 / UnsupportedClassVersionError** — 集群须为 **Flink 1.20.x + Java 17**，与本模块 `flink.version`、
   `maven.compiler.release` 一致；提交 `*-flink.jar`。
2. **DTO 与设备缓存模型不一致** — 内联类路径见 `src/main/java/com/ourexists/omes/device/` 与 `.../message/`；与
   `omes-device`、`omes-message` 仓库同源字段变更时请两边对齐。
3. **Keyed state TTL** — 各算子**分别**配置：默认 **`-1`** 表示**不启用** State TTL；设为正整数表示空闲保留分钟数，见
   `EquipStreamStateTtl`。
