package com.ourexists.omes.stream.equip;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * Entry point for submitting this module to a Flink cluster ({@code flink run} or Web UI JAR upload), or for local
 * debugging via embedded mini-cluster ({@code omes.device.flink.local=true} / {@code --local true}; see README).
 * <p>
 * Configuration: Flink {@code ParameterTool} merges program args, {@code -D} system properties, and environment
 * variables (see module {@code README.md} for {@code OMES_*} / {@code RABBITMQ_*} and dotted property keys).
 * <p>
 *  messaging 仍为 Flink 连接器 + RabbitMQ Java 客户端：Flink 非 Spring 容器，无法直接使用 Spring Cloud Stream；
 *  与门户侧的 Stream 拓扑通过共用队列名、vhost（及实时队列上并行绑定 TopicExchange）对齐。
 */
@Slf4j
public final class EquipRealtimeFlinkJob {

    public static void main(String[] args) throws Exception {
        ParameterTool pt = ParameterTool.fromArgs(args)
                .mergeWith(ParameterTool.fromSystemProperties())
                .mergeWith(ParameterTool.fromMap(System.getenv()));
        EquipRealtimeFlinkJobConfig cfg = EquipRealtimeFlinkJobProperties.from(pt);
        RMQConnectionConfig rmq = EquipRealtimeFlinkRmqConfig.from(pt, cfg.flinkRmqPrefetch());
        StreamExecutionEnvironment env = EquipRealtimeFlinkGraph.createExecutionEnvironment(cfg);
        EquipRealtimeFlinkGraph.configureExecutionEnvironment(env, cfg);
        EquipRealtimeFlinkGraph.buildAndExecute(env, rmq, cfg);
    }

    private EquipRealtimeFlinkJob() {
    }
}
