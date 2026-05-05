package com.ourexists.omes.stream.equip;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.model.EquipCollectSnapshotEvent;
import com.ourexists.omes.stream.equip.model.EquipRealtimeChangeEvent;
import com.ourexists.omes.stream.equip.model.EquipStateSnapshotEvent;
import com.ourexists.omes.stream.equip.process.*;
import com.ourexists.omes.stream.equip.rabbitmq.EquipRealtimeAmqpDeserializationSchema;
import com.ourexists.omes.stream.equip.rabbitmq.LoggedEquipRealtimeRmqSource;
import com.ourexists.omes.stream.equip.sink.EquipAlarmNotifySink;
import com.ourexists.omes.stream.equip.sink.bridge.EquipCollectSnapshotBridgeSink;
import com.ourexists.omes.stream.equip.sink.bridge.EquipRecordChangeBridgeSink;
import com.ourexists.omes.stream.equip.sink.bridge.EquipStateSnapshotBridgeSink;
import com.ourexists.omes.stream.equip.support.EquipRealtimeEventTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

@Slf4j
public final class EquipRealtimeFlinkGraph {

    private static final ReduceFunction<EquipRealtime> PICK_LATEST_EVENT_REDUCER =
            (left, right) -> EquipRealtimeEventTimeUtil.isNewerOrSame(right, left) ? right : left;

    private EquipRealtimeFlinkGraph() {}

    public static StreamExecutionEnvironment createExecutionEnvironment() {
        Configuration flinkConfig = new Configuration();
        flinkConfig.setString("classloader.resolve-order", "parent-first");
        return StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
    }

    public static void configureExecutionEnvironment(StreamExecutionEnvironment env, EquipRealtimeFlinkJobConfig cfg) {
        env.setParallelism(cfg.parallelism());
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(10, org.apache.flink.api.common.time.Time.seconds(5)));
        if (cfg.flinkEnableCheckpointing()) {
            env.enableCheckpointing(cfg.flinkRmqCheckpointIntervalMs(), CheckpointingMode.AT_LEAST_ONCE);
            env.getCheckpointConfig().setCheckpointTimeout(cfg.flinkCheckpointTimeoutMs());
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500L);
            env.getCheckpointConfig().setTolerableCheckpointFailureNumber(5);
            log.info(
                    "Flink checkpoint ON for RMQSource (RabbitMQ transactional consume; ack follows checkpoint). interval={}ms timeout={}ms",
                    cfg.flinkRmqCheckpointIntervalMs(),
                    cfg.flinkCheckpointTimeoutMs());
        } else {
            log.warn(
                    "Flink checkpoint OFF for RMQSource — RabbitMQ uses auto-ack; queue backlog clears without waiting for checkpoint. "
                            + "Set omes.device.flink.enable-checkpointing=true only when running in a proper Flink cluster with working checkpoints.");
        }
    }

    public static void buildAndExecute(StreamExecutionEnvironment env, RMQConnectionConfig rmq, EquipRealtimeFlinkJobConfig cfg)
            throws Exception {
        if (StringUtils.isBlank(cfg.equipRealtimeRabbitQueue())) {
            log.error("omes.device.rabbitmq.equip-realtime-queue is empty; Flink RMQSource not started");
            return;
        }
        if (StringUtils.isBlank(cfg.equipNotifyCreateQueue())) {
            log.warn("omes.device.rabbitmq.equip-notify-create-queue is empty; alarm notify sink disabled");
        }

        LoggedEquipRealtimeRmqSource rmqSource = new LoggedEquipRealtimeRmqSource(
                rmq,
                cfg.equipRealtimeRabbitQueue(),
                false,
                new EquipRealtimeAmqpDeserializationSchema());

        DataStream<EquipRealtime> source = env
                .addSource(rmqSource)
                .setParallelism(cfg.parallelism())
                .name("equip-realtime-rmq-source");
        DataStream<EquipRealtime> validStream = source
                .filter(e -> e != null && StringUtils.isNotBlank(e.getSelfCode()))
                .name("equip-realtime-filter");

        DataStream<EquipRealtime> windowStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .window(SlidingProcessingTimeWindows.of(Duration.ofSeconds(30), Duration.ofSeconds(5)))
                .reduce(PICK_LATEST_EVENT_REDUCER)
                .name("equip-realtime-window-30s-5s");

        DataStream<EquipRealtime> offlineStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipOfflineDetectProcessFunction(cfg.offlineTimeoutMs()))
                .name("equip-realtime-offline-detect");

        DataStream<EquipRealtimeChangeEvent> fluctuationChangeStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .window(SlidingProcessingTimeWindows.of(
                        Duration.ofMillis(cfg.attrFluctuationWindowMs()),
                        Duration.ofMillis(cfg.attrFluctuationSlideMs())))
                .process(new EquipAttrFluctuationProcessFunction())
                .name("equip-realtime-attr-fluctuation");

        DataStream<EquipRealtimeChangeEvent> changeStream = windowStream
                .union(offlineStream)
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipRealtimeChangeDetectProcessFunction())
                .name("equip-realtime-change-detect");

        DataStream<EquipStateSnapshotEvent> snapshotStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipStateSnapshotProcessFunction(cfg.snapshotIntervalMs()))
                .name("equip-realtime-snapshot-timed");
        DataStream<EquipCollectSnapshotEvent> collectSnapshotStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipCollectSnapshotProcessFunction(cfg.snapshotIntervalMs()))
                .name("equip-collect-snapshot-timed");

        changeStream
                .addSink(new EquipRecordChangeBridgeSink(rmq, cfg.equipStreamPersistChangeQueue()))
                .name("equip-realtime-change-persist-bridge-rmq-sink")
                .setParallelism(1);
        changeStream
                .addSink(new EquipAlarmNotifySink(rmq, cfg.equipNotifyCreateQueue()))
                .name("equip-realtime-alarm-notify-sink")
                .setParallelism(1);
        fluctuationChangeStream
                .addSink(new EquipRecordChangeBridgeSink(rmq, cfg.equipStreamPersistChangeQueue()))
                .name("equip-attr-fluctuation-persist-bridge-rmq-sink")
                .setParallelism(1);
        fluctuationChangeStream
                .addSink(new EquipAlarmNotifySink(rmq, cfg.equipNotifyCreateQueue()))
                .name("equip-attr-fluctuation-alarm-notify-sink")
                .setParallelism(1);
        snapshotStream
                .addSink(new EquipStateSnapshotBridgeSink(rmq, cfg.equipStreamPersistStateQueue()))
                .name("equip-realtime-snapshot-persist-bridge-rmq-sink")
                .setParallelism(1);
        collectSnapshotStream
                .addSink(new EquipCollectSnapshotBridgeSink(rmq, cfg.equipStreamPersistCollectQueue()))
                .name("equip-collect-snapshot-persist-bridge-rmq-sink")
                .setParallelism(1);
        log.info(
                "Submitting Flink job equip-realtime-job (blocking in env.execute); queue={} persistChange={} persistState={} persistCollect={}",
                cfg.equipRealtimeRabbitQueue(),
                cfg.equipStreamPersistChangeQueue(),
                cfg.equipStreamPersistStateQueue(),
                cfg.equipStreamPersistCollectQueue());
        env.execute("equip-realtime-job");
    }
}
