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
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.execution.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.time.Duration;

@Slf4j
public final class EquipRealtimeFlinkGraph {

    private static final ReduceFunction<EquipRealtime> PICK_LATEST_EVENT_REDUCER =
            EquipRealtimeEventTimeUtil::pickLatestForWindowReduce;

    private EquipRealtimeFlinkGraph() {
    }

    public static StreamExecutionEnvironment createExecutionEnvironment() {
        Configuration flinkConfig = new Configuration();
        flinkConfig.setString("classloader.resolve-order", "parent-first");
        return StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
    }

    public static void configureExecutionEnvironment(StreamExecutionEnvironment env, EquipRealtimeFlinkJobConfig cfg) {
        //作业失败重启
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(10, Duration.ofSeconds(5)));
        if (cfg.flinkEnableCheckpointing()) {
            //checkpoint快照间隔
            env.enableCheckpointing(cfg.flinkCheckpointIntervalMs(), CheckpointingMode.AT_LEAST_ONCE);
            //一个 checkpoint 最大允许执行时间
            env.getCheckpointConfig().setCheckpointTimeout(cfg.flinkCheckpointTimeoutMs());
            //两次 checkpoint 最少间隔 500ms
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500L);
            //checkpoint 失败最多允许 5 次
            env.getCheckpointConfig().setTolerableCheckpointFailureNumber(5);
            if (cfg.flinkUnalignedCheckpoint()) {
                env.getCheckpointConfig().enableUnalignedCheckpoints();
                if (cfg.flinkAlignedCheckpointTimeoutMs() > 0L) {
                    env.getCheckpointConfig()
                            .setAlignedCheckpointTimeout(Duration.ofMillis(cfg.flinkAlignedCheckpointTimeoutMs()));
                }
            }
            log.info(
                    "Flink checkpoint ON for RMQSource (RabbitMQ transactional consume; ack follows checkpoint). interval={}ms timeout={}ms unaligned={} alignedCheckpointTimeoutMs={}",
                    cfg.flinkCheckpointIntervalMs(),
                    cfg.flinkCheckpointTimeoutMs(),
                    cfg.flinkUnalignedCheckpoint(),
                    cfg.flinkUnalignedCheckpoint() ? cfg.flinkAlignedCheckpointTimeoutMs() : 0L);
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
                .process(new EquipOfflineDetectProcessFunction(cfg.offlineTimeoutMs(), cfg.stateTtlMinutesOfflineDetect()))
                .name("equip-realtime-offline-detect");

        DataStream<EquipRealtimeChangeEvent> fluctuationChangeStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .window(SlidingProcessingTimeWindows.of(
                        Duration.ofMillis(cfg.attrFluctuationWindowMs()),
                        Duration.ofMillis(cfg.attrFluctuationSlideMs())))
                .process(new EquipAttrFluctuationProcessFunction(cfg.stateTtlMinutesAttrFluctuation()))
                .name("equip-realtime-attr-fluctuation");

        DataStream<EquipRealtimeChangeEvent> changeStream = windowStream
                .union(offlineStream)
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipRealtimeChangeDetectProcessFunction(cfg.stateTtlMinutesChangeDetect()))
                .name("equip-realtime-change-detect");

        DataStream<EquipStateSnapshotEvent> snapshotStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipStateSnapshotProcessFunction(cfg.snapshotIntervalMs(), cfg.stateTtlMinutesStateSnapshot()))
                .name("equip-realtime-snapshot-timed");
        DataStream<EquipCollectSnapshotEvent> collectSnapshotStream = validStream
                .keyBy(EquipRealtime::getSelfCode)
                .process(new EquipCollectSnapshotProcessFunction(cfg.snapshotIntervalMs(), cfg.stateTtlMinutesCollectSnapshot()))
                .name("equip-collect-snapshot-timed");

        DataStream<EquipRealtimeChangeEvent> changeStreamValid = changeStream
                .filter(
                        e -> e != null
                                && e.getTarget() != null
                                && StringUtils.isNotBlank(e.getTarget().getSelfCode()))
                .name("equip-change-stream-valid");
        DataStream<EquipRealtimeChangeEvent> fluctuationValid = fluctuationChangeStream
                .filter(
                        e -> e != null
                                && e.getTarget() != null
                                && StringUtils.isNotBlank(e.getTarget().getSelfCode()))
                .name("equip-fluctuation-stream-valid");
        SingleOutputStreamOperator<EquipRealtimeChangeEvent> changeAlarmDeduped = changeStreamValid
                .keyBy(e -> e.getTarget().getSelfCode())
                .process(new EquipAlarmChangeDedupeProcessFunction(cfg.stateTtlMinutesAlarmNotifyDedupe()))
                .name("equip-alarm-fingerprint-dedupe");
        changeAlarmDeduped
                .addSink(new EquipRecordChangeBridgeSink(rmq, cfg.equipStreamPersistChangeQueue()))
                .name("equip-persist-alarm-dedupe-bridge-rmq-sink");
        changeAlarmDeduped
                .addSink(new EquipAlarmNotifySink(rmq, cfg.equipNotifyCreateQueue()))
                .name("equip-alarm-notify-sink");
        fluctuationValid
                .addSink(new EquipRecordChangeBridgeSink(rmq, cfg.equipStreamPersistChangeQueue()))
                .name("equip-attr-fluctuation-persist-bridge-rmq-sink");
        fluctuationValid
                .addSink(new EquipAlarmNotifySink(rmq, cfg.equipNotifyCreateQueue()))
                .name("equip-attr-fluctuation-notify-sink");
        snapshotStream
                .addSink(new EquipStateSnapshotBridgeSink(rmq, cfg.equipStreamPersistStateQueue()))
                .name("equip-realtime-snapshot-persist-bridge-rmq-sink");
        collectSnapshotStream
                .addSink(new EquipCollectSnapshotBridgeSink(rmq, cfg.equipStreamPersistCollectQueue()))
                .name("equip-collect-snapshot-persist-bridge-rmq-sink");
        log.info(
                "Submitting Flink job equip-realtime-job (blocking in env.execute); queue={} persistChange={} persistState={} persistCollect={}",
                cfg.equipRealtimeRabbitQueue(),
                cfg.equipStreamPersistChangeQueue(),
                cfg.equipStreamPersistStateQueue(),
                cfg.equipStreamPersistCollectQueue());
        env.execute("equip-realtime-job");
    }
}
