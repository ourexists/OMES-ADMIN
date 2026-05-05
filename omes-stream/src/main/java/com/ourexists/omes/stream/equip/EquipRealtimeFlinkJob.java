package com.ourexists.omes.stream.equip;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * Entry point for submitting this module to a Flink cluster ({@code flink run} or Web UI JAR upload).
 * <p>
 * Configuration: Flink {@code ParameterTool} merges program args, {@code -D} system properties, and environment
 * variables (see module {@code README.md} for {@code OMES_*} / {@code RABBITMQ_*} and dotted property keys).
 */
@Slf4j
public final class EquipRealtimeFlinkJob {

    public static void main(String[] args) throws Exception {
        ParameterTool pt = ParameterTool.fromArgs(args)
                .mergeWith(ParameterTool.fromSystemProperties())
                .mergeWith(ParameterTool.fromMap(System.getenv()));
        EquipRealtimeFlinkJobConfig cfg = EquipRealtimeFlinkJobProperties.from(pt);
        RMQConnectionConfig rmq = EquipRealtimeFlinkRmqConfig.from(pt, cfg.flinkRmqPrefetch());
        StreamExecutionEnvironment env = EquipRealtimeFlinkGraph.createExecutionEnvironment();
        EquipRealtimeFlinkGraph.configureExecutionEnvironment(env, cfg);
        log.info("Equip realtime Flink job starting (JAR / cluster mode), parallelism={}", cfg.parallelism());
        EquipRealtimeFlinkGraph.buildAndExecute(env, rmq, cfg);
    }

    private EquipRealtimeFlinkJob() {}
}
