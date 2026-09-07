package com.ourexists.omes.stream.workshop;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * 场景实时流：消费 {@code omes.workshop.realtime}，按周期输出采集快照至桥接队列，由门户攒批调用
 * {@code WorkshopCollectFeign#addBatch}。
 * <p>
 * 集群提交使用 {@code flink run}；本地断点调试可开启嵌入式 MiniCluster（{@code omes.workshop.flink.local=true} /
 * {@code --local true} 等，见模块 README）。
 */
@Slf4j
public final class WorkshopRealtimeFlinkJob {

    public static void main(String[] args) throws Exception {
        ParameterTool pt = ParameterTool.fromArgs(args)
                .mergeWith(ParameterTool.fromSystemProperties())
                .mergeWith(ParameterTool.fromMap(System.getenv()));
        WorkshopRealtimeFlinkJobConfig cfg = WorkshopRealtimeFlinkJobProperties.from(pt);
        RMQConnectionConfig rmq = WorkshopRealtimeFlinkRmqConfig.from(pt, cfg.flinkRmqPrefetch());
        StreamExecutionEnvironment env = WorkshopRealtimeFlinkGraph.createExecutionEnvironment(cfg);
        WorkshopRealtimeFlinkGraph.configureExecutionEnvironment(env, cfg);
        WorkshopRealtimeFlinkGraph.buildAndExecute(env, rmq, cfg);
    }

    private WorkshopRealtimeFlinkJob() {}
}
