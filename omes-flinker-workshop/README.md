# omes-flinker-workshop

场景（车间）实时流 Flink 作业：消费 RabbitMQ `omes.workshop.realtime`（与门户 `workshopRealtime-out-0` 对齐），按可配置周期对 `needCollect` 属性做**采集快照**，写入桥接队列 `omes.workshop.stream.persist.collect`，由门户 Spring Cloud Stream 消费、攒批后调用 `WorkshopCollectFeign#addBatch`。逻辑对齐 `omes-flinker-equip` 中的 `EquipCollectSnapshotProcessFunction` + `EquipCollectSnapshotBridgeSink`。

定时任务 `WorkshopCollectSnapshot` 与 Flink 二选一：部署本作业后请将 `omes.workshop.collect-snapshot-timer-enabled=false`，避免重复入库。

## 构建

```bash
mvn -pl omes-flinker-workshop clean package -DskipTests
```

产物：`target/omes-flinker-workshop-*-flink.jar`（`Main-Class`: `com.ourexists.omes.stream.workshop.WorkshopRealtimeFlinkJob`）。

本地试跑带 Flink 依赖：

```bash
mvn -pl omes-flinker-workshop clean package -DskipTests -Pflink-with-dependencies
```

## 提交

```bash
flink run -c com.ourexists.omes.stream.workshop.WorkshopRealtimeFlinkJob /path/to/omes-flinker-workshop-*-flink.jar
```

## 配置（ParameterTool：参数 → `-D` → 环境变量）

| 变量 / 点分键 | 说明 | 默认 |
|---------------|------|------|
| `OMES_WORKSHOP_REALTIME_QUEUE` / `omes.workshop.rabbitmq.workshop-realtime-queue` | 场景实时消费队列 | `omes.workshop.realtime` |
| `OMES_WORKSHOP_STREAM_PERSIST_COLLECT_QUEUE` / `omes.workshop.rabbitmq.workshop-stream-persist-collect-queue` | 采集快照桥接队列 | `omes.workshop.stream.persist.collect` |
| `OMES_WORKSHOP_SNAPSHOT_INTERVAL_MS` / `omes.workshop.snapshot-interval-ms` | 快照周期（毫秒） | `30000` |
| `RABBITMQ_*` | 与 equip 作业相同 | 见 `WorkshopRealtimeFlinkRmqConfig` |
| `OMES_FLINK_ENABLE_CHECKPOINTING` 等 | 与 equip 相同含义 | 见 `WorkshopRealtimeFlinkJobProperties` |
| `OMES_FLINK_STATE_TTL_MINUTES_WORKSHOP_COLLECT_SNAPSHOT` | 采集快照算子 state TTL（分钟，`-1` 关闭） | `-1` |

内联模型路径与 `omes-device-base` / `omes-device-model` 同源字段变更时请手动同步本模块 `com.ourexists.omes.device.*` 下源码。
