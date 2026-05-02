package com.ourexists.omes.stream.equip;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.stream.equip.model.EquipAttrFluctuationWindowEvent;
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
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class EquipRealtimeFlinkJobRunner {
    private static final ReduceFunction<EquipRealtime> PICK_LATEST_EVENT_REDUCER =
            (left, right) -> EquipRealtimeEventTimeUtil.isNewerOrSame(right, left) ? right : left;

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Value("${omes.device.rabbitmq.equip-realtime-queue:omes.equip.realtime}")
    private String equipRealtimeRabbitQueue;

    @Value("${omes.device.rabbitmq.equip-notify-create-queue:omes.equip.notify.create}")
    private String equipNotifyCreateQueue;

    @Value("${omes.device.rabbitmq.equip-stream-persist-queue:omes.equip.stream.persist}")
    private String equipStreamPersistQueue;

    @Value("${omes.device.flink.checkpoint-interval-ms:10000}")
    private long flinkRmqCheckpointIntervalMs;

    @Value("${omes.device.flink.enable-checkpointing:false}")
    private boolean flinkEnableCheckpointing;

    @Value("${omes.device.flink.checkpoint-timeout-ms:120000}")
    private long flinkCheckpointTimeoutMs;

    @Value("${omes.device.flink.rmq-prefetch:100}")
    private int flinkRmqPrefetch;

    @Value("${omes.device.offline-timeout-ms:30000}")
    private long offlineTimeoutMs;

    @Value("${omes.device.snapshot-interval-ms:30000}")
    private long snapshotIntervalMs;

    @Value("${omes.device.attr-fluctuation-window-ms}")
    private long attrFluctuationWindowMs;

    @Value("${omes.device.attr-fluctuation-slide-ms}")
    private long attrFluctuationSlideMs;

    private final AtomicBoolean flinkStarted = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void startFlinkJobAfterApplicationReady() {
        startFlinkJob();
    }

    private void startFlinkJob() {
        if (!flinkStarted.compareAndSet(false, true)) {
            return;
        }
        Thread flinkThread = new Thread(this::runFlinkJob, "equip-flink-window-thread");
        // 非守护线程：避免仅剩守护线程时 JVM 在 Spring 上下文已就绪后仍提前退出（表现为「启动后中断」）
        flinkThread.setDaemon(false);
        flinkThread.setUncaughtExceptionHandler(
                (t, ex) -> log.error("equip-flink-window-thread died before/during runFlinkJob try/catch", ex));
        flinkThread.start();
        log.info("equip-flink-window-thread started (user thread, daemon=false)");
    }

    @SuppressWarnings("deprecation")
    private void runFlinkJob() {
        try {
            Configuration flinkConfig = new Configuration();
            flinkConfig.setString("classloader.resolve-order", "parent-first");
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
            env.setParallelism(4);
            env.setRestartStrategy(RestartStrategies.fixedDelayRestart(10, org.apache.flink.api.common.time.Time.seconds(5)));
            if (flinkEnableCheckpointing) {
                env.enableCheckpointing(flinkRmqCheckpointIntervalMs, CheckpointingMode.AT_LEAST_ONCE);
                env.getCheckpointConfig().setCheckpointTimeout(flinkCheckpointTimeoutMs);
                env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500L);
                env.getCheckpointConfig().setTolerableCheckpointFailureNumber(5);
                log.info(
                        "Flink checkpoint ON for RMQSource (RabbitMQ transactional consume; ack follows checkpoint). interval={}ms timeout={}ms",
                        flinkRmqCheckpointIntervalMs,
                        flinkCheckpointTimeoutMs);
            } else {
                log.warn(
                        "Flink checkpoint OFF for RMQSource — RabbitMQ uses auto-ack; queue backlog clears without waiting for checkpoint. "
                                + "Set omes.device.flink.enable-checkpointing=true only when running in a proper Flink cluster with working checkpoints.");
            }

            if (!StringUtils.hasText(equipRealtimeRabbitQueue)) {
                log.error("omes.device.rabbitmq.equip-realtime-queue is empty; Flink RMQSource not started");
                flinkStarted.set(false);
                return;
            }
            RMQConnectionConfig rmqConfig = buildRmqConnectionConfigAlignedWithSpring();
            if (!StringUtils.hasText(equipNotifyCreateQueue)) {
                log.warn("omes.device.rabbitmq.equip-notify-create-queue is empty; alarm notify sink disabled");
            }

            LoggedEquipRealtimeRmqSource rmqSource = new LoggedEquipRealtimeRmqSource(
                    rmqConfig,
                    equipRealtimeRabbitQueue,
                    false,
                    new EquipRealtimeAmqpDeserializationSchema());

            DataStream<EquipRealtime> source = env
                    .addSource(rmqSource)
                    .setParallelism(1)
                    .name("equip-realtime-rmq-source");
            DataStream<EquipRealtime> validStream = source
                    .filter(e -> e != null && StringUtils.hasText(e.getSelfCode()))
                    .name("equip-realtime-filter");

            DataStream<EquipRealtime> windowStream = validStream
                    .keyBy(EquipRealtime::getSelfCode)
                    .window(SlidingProcessingTimeWindows.of(Duration.ofSeconds(30), Duration.ofSeconds(5)))
                    .reduce(PICK_LATEST_EVENT_REDUCER)
                    .name("equip-realtime-window-30s-5s");

            DataStream<EquipRealtime> offlineStream = validStream
                    .keyBy(EquipRealtime::getSelfCode)
                    .process(new EquipOfflineDetectProcessFunction(offlineTimeoutMs))
                    .name("equip-realtime-offline-detect");

            DataStream<EquipAttrFluctuationWindowEvent> fluctuationWindowStream = validStream
                    .keyBy(EquipRealtime::getSelfCode)
                    .window(SlidingProcessingTimeWindows.of(
                            Duration.ofMillis(attrFluctuationWindowMs),
                            Duration.ofMillis(attrFluctuationSlideMs)
                    ))
                    .process(new EquipAttrFluctuationWindowProcessFunction())
                    .name("equip-realtime-attr-fluctuation-window");
            DataStream<EquipRealtime> fluctuationAlarmStream = fluctuationWindowStream
                    .keyBy(event -> event.getSelfCode() + "|" + event.getAttrName())
                    .process(new EquipAttrFluctuationConsecutiveProcessFunction())
                    .name("equip-realtime-attr-fluctuation-consecutive");

            DataStream<EquipRealtimeChangeEvent> changeStream = windowStream
                    .union(offlineStream)
                    .union(fluctuationAlarmStream)
                    .keyBy(EquipRealtime::getSelfCode)
                    .process(new EquipRealtimeChangeDetectProcessFunction())
                    .name("equip-realtime-change-detect");

            DataStream<EquipStateSnapshotEvent> snapshotStream = validStream
                    .keyBy(EquipRealtime::getSelfCode)
                    .process(new EquipStateSnapshotProcessFunction(snapshotIntervalMs))
                    .name("equip-realtime-snapshot-timed");
            DataStream<EquipCollectSnapshotEvent> collectSnapshotStream = validStream
                    .keyBy(EquipRealtime::getSelfCode)
                    .process(new EquipCollectSnapshotProcessFunction(snapshotIntervalMs))
                    .name("equip-collect-snapshot-timed");

            changeStream
                    .addSink(new EquipRecordChangeBridgeSink(rmqConfig, equipStreamPersistQueue))
                    .name("equip-realtime-change-persist-bridge-rmq-sink")
                    .setParallelism(1);
            changeStream
                    .addSink(new EquipAlarmNotifySink(rmqConfig, equipNotifyCreateQueue))
                    .name("equip-realtime-alarm-notify-sink")
                    .setParallelism(1);
            snapshotStream
                    .addSink(new EquipStateSnapshotBridgeSink(rmqConfig, equipStreamPersistQueue))
                    .name("equip-realtime-snapshot-persist-bridge-rmq-sink")
                    .setParallelism(1);
            collectSnapshotStream
                    .addSink(new EquipCollectSnapshotBridgeSink(rmqConfig, equipStreamPersistQueue))
                    .name("equip-collect-snapshot-persist-bridge-rmq-sink")
                    .setParallelism(1);
            log.info(
                    "Submitting Flink job equip-realtime-job (blocking in env.execute); queue={} persistQueue={}",
                    equipRealtimeRabbitQueue,
                    equipStreamPersistQueue);
            env.execute("equip-realtime-job");
        } catch (Throwable e) {
            log.error("Equip realtime flink job stopped unexpectedly (RMQSource open/execute failure — check broker, vhost, queue name, SSL)", e);
            flinkStarted.set(false);
        }
    }

    private RMQConnectionConfig buildRmqConnectionConfigAlignedWithSpring() {
        if (rabbitConnectionFactory instanceof CachingConnectionFactory ccf) {
            com.rabbitmq.client.ConnectionFactory rcf = ccf.getRabbitConnectionFactory();
            String vhost = rcf.getVirtualHost();
            return new RMQConnectionConfig.Builder()
                    .setHost(rcf.getHost())
                    .setPort(rcf.getPort())
                    .setVirtualHost(vhost != null ? vhost : "/")
                    .setUserName(rcf.getUsername())
                    .setPassword(rcf.getPassword())
                    .setAutomaticRecovery(true)
                    .setTopologyRecoveryEnabled(true)
                    .setNetworkRecoveryInterval((int) TimeUnit.SECONDS.toMillis(5))
                    .setPrefetchCount(flinkRmqPrefetch)
                    .build();
        }
        throw new IllegalStateException(
                "Unsupported ConnectionFactory type for Flink RMQSource: " + rabbitConnectionFactory.getClass().getName());
    }
}
