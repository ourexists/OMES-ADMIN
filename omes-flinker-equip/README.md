# omes-stream

设备实时流 Flink 作业：消费 RabbitMQ 设备实时队列，完成离线检测、变化检测、快照与属性波动告警，并回写 RabbitMQ。无 Web 进程，仅通过 **Flink 提交**运行。

## 构建

在仓库根目录执行（`omes-stream` 已内联原 `omes-device-base` / `omes-device-model` / `omes-message-model` 中与作业相关的类，**不再依赖**上述三个 JAR；若门户侧 DTO 变更，需手动同步本模块内对应源码）：

```bash
mvn -pl omes-flinker-equip clean package -DskipTests
```

产物：

- `target/omes-stream-<version>.jar` — 薄 JAR（类与资源，不含依赖）
- `target/omes-stream-<version>-flink.jar` — **提交到集群的 fat JAR**（`Main-Class` 已指向作业入口）

默认 `flink-streaming-java`、`flink-clients` 为 **provided**，由 Flink 集群提供，须与 `pom.xml` 中 **`flink.version`（当前 1.20.1，对应 Flink 1.20 线）** 一致。

本模块 **`maven.compiler.release` 为 17**（字节码 major 61），与 **Flink 1.20 官方要求的 Java 17** 一致；集群 JobManager / TaskManager 请使用 **JDK 17**。

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

## 配置说明

配置由 Flink `ParameterTool` 合并，优先级：**程序参数** → **JVM `-D` 系统属性** → **环境变量**。

### RabbitMQ

| 变量 | 说明 | 默认（开发参考） |
|------|------|------------------|
| `RABBITMQ_HOST` | 主机 | `127.0.0.1` |
| `RABBITMQ_PORT` | 端口 | `5672` |
| `RABBITMQ_USERNAME` | 用户名 | `admin` |
| `RABBITMQ_PASSWORD` | 密码 | （见 `EquipRealtimeFlinkRmqConfig` 内默认值，生产务必覆盖） |
| `RABBITMQ_VHOST` | 虚拟主机 | `/` |

### 队列与 Flink

| 变量 | 说明 |
|------|------|
| `OMES_EQUIP_REALTIME_QUEUE` | 设备实时消费队列 |
| `OMES_EQUIP_NOTIFY_CREATE_QUEUE` | 告警通知队列（可空，空则告警 Sink 不发送） |
| `OMES_EQUIP_STREAM_PERSIST_CHANGE_QUEUE` | 运行/报警/在线变更桥接队列 |
| `OMES_EQUIP_STREAM_PERSIST_STATE_QUEUE` | 状态周期快照桥接队列 |
| `OMES_EQUIP_STREAM_PERSIST_COLLECT_QUEUE` | 采集周期快照桥接队列 |
| `OMES_FLINK_ENABLE_CHECKPOINTING` | 是否开启 checkpoint（`true`/`false`） |
| `OMES_FLINK_CHECKPOINT_MS` | checkpoint 间隔（毫秒） |
| `OMES_FLINK_CHECKPOINT_TIMEOUT_MS` | checkpoint 超时（毫秒） |
| `OMES_FLINK_UNALIGNED_CHECKPOINT` | 非对齐 checkpoint（减轻 barrier 对齐在背压下的阻塞；默认 `true`） |
| `OMES_FLINK_ALIGNED_CHECKPOINT_TIMEOUT_MS` | 先尝试对齐 barrier，超过该时间（毫秒）后切到非对齐；`0` 表示不设（默认 `30000`） |
| `OMES_FLINK_RMQ_PREFETCH` | RMQ prefetch |
| `OMES_FLINK_STATE_TTL_MINUTES_OFFLINE_DETECT` | 离线检测算子 keyed state：`-1` 不启用 TTL（默认），正整数为空闲保留 **分钟** |
| `OMES_FLINK_STATE_TTL_MINUTES_CHANGE_DETECT` | 变更检测算子（同上） |
| `OMES_FLINK_STATE_TTL_MINUTES_ATTR_FLUCTUATION` | 属性波动 MapState（同上） |
| `OMES_FLINK_STATE_TTL_MINUTES_STATE_SNAPSHOT` | 状态周期快照算子（同上） |
| `OMES_FLINK_STATE_TTL_MINUTES_COLLECT_SNAPSHOT` | 采集周期快照算子（同上） |

亦支持点分键名传参，例如：`omes.device.flink.enable-checkpointing`、`omes.device.flink.state-ttl-minutes.change-detect`。

### 业务参数

| 变量 | 说明                         |
|------|----------------------------|
| `OMES_EQUIP_OFFLINE_TIMEOUT_MS` | 离线判定：无新设备数据入站的时长阈值（默认 90s） |
| `OMES_EQUIP_SNAPSHOT_INTERVAL_MS` | 快照周期                       |
| `OMES_EQUIP_ATTR_FLUCTUATION_WINDOW_MS` | 属性波动窗口                     |
| `OMES_EQUIP_ATTR_FLUCTUATION_SLIDE_MS` | 属性波动滑动步长                   |

## 日志

使用 classpath 下 `logback.xml`（控制台）。

## 常见问题

1. **集群缺 Flink 类 / UnsupportedClassVersionError** — 集群须为 **Flink 1.20.x + Java 17**，与本模块 `flink.version`、`maven.compiler.release` 一致；提交 `*-flink.jar`。  
2. **DTO 与设备缓存模型不一致** — 内联类路径见 `src/main/java/com/ourexists/omes/device/` 与 `.../message/`；与 `omes-device`、`omes-message` 仓库同源字段变更时请两边对齐。  
3. **Keyed state TTL** — 各算子**分别**配置：默认 **`-1`** 表示**不启用** State TTL；设为正整数表示空闲保留分钟数，见 `EquipStreamStateTtl`。
