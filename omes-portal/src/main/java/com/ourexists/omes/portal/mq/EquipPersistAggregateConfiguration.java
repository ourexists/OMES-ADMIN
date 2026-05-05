package com.ourexists.omes.portal.mq;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

/**
 * Flink 桥接 persist 消息在门户内攒批
 * 状态快照 / 采集快照 走 {@link EquipPersistBatchWriter}。与 Spring Cloud Stream 解耦。
 */
@Configuration
@EnableIntegration
public class EquipPersistAggregateConfiguration {

    public static final String EQUIP_PERSIST_CHANGE_INPUT = "equipPersistChangeInputChannel";
    public static final String EQUIP_PERSIST_STATE_SNAPSHOT_INPUT = "equipPersistStateSnapshotInputChannel";
    public static final String EQUIP_PERSIST_COLLECT_SNAPSHOT_INPUT = "equipPersistCollectSnapshotInputChannel";

    @Bean(name = EQUIP_PERSIST_CHANGE_INPUT)
    public MessageChannel equipPersistChangeInputChannel() {
        return new DirectChannel();
    }

    @Bean(name = EQUIP_PERSIST_STATE_SNAPSHOT_INPUT)
    public MessageChannel equipPersistStateSnapshotInputChannel() {
        return new DirectChannel();
    }

    @Bean(name = EQUIP_PERSIST_COLLECT_SNAPSHOT_INPUT)
    public MessageChannel equipPersistCollectSnapshotInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow equipPersistChangeAggregateFlow(
            @Qualifier(EQUIP_PERSIST_CHANGE_INPUT) MessageChannel equipPersistChangeInputChannel,
            @Value("${omes.equip.stream-persist-batch-size:32}") int batchSize,
            @Value("${omes.equip.stream-persist-batch-flush-ms:200}") long batchFlushMs,
            EquipPersistBatchWriter batchWriter) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(equipPersistChangeInputChannel)
                .aggregate(a -> a
                        .correlationStrategy(message -> "equip-persist-change")
                        .releaseStrategy(group -> group.size() >= effectiveBatch)
                        .groupTimeout(effectiveFlushMs)
                        .sendPartialResultOnExpiry(true)
                        .expireGroupsUponCompletion(true))
                .handle(batchWriter, "writeAggregatedBatch")
                .get();
    }

    @Bean
    public IntegrationFlow equipPersistStateSnapshotAggregateFlow(
            @Qualifier(EQUIP_PERSIST_STATE_SNAPSHOT_INPUT) MessageChannel equipPersistStateSnapshotInputChannel,
            @Value("${omes.equip.stream-persist-batch-size:32}") int batchSize,
            @Value("${omes.equip.stream-persist-batch-flush-ms:200}") long batchFlushMs,
            EquipPersistBatchWriter snapshotBatchWriter) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(equipPersistStateSnapshotInputChannel)
                .aggregate(a -> a
                        .correlationStrategy(message -> "equip-persist-state-snapshot")
                        .releaseStrategy(group -> group.size() >= effectiveBatch)
                        .groupTimeout(effectiveFlushMs)
                        .sendPartialResultOnExpiry(true)
                        .expireGroupsUponCompletion(true))
                .handle(snapshotBatchWriter, "writeStateSnapshotBatch")
                .get();
    }

    @Bean
    public IntegrationFlow equipPersistCollectSnapshotAggregateFlow(
            @Qualifier(EQUIP_PERSIST_COLLECT_SNAPSHOT_INPUT) MessageChannel equipPersistCollectSnapshotInputChannel,
            @Value("${omes.equip.stream-persist-batch-size:32}") int batchSize,
            @Value("${omes.equip.stream-persist-batch-flush-ms:200}") long batchFlushMs,
            EquipPersistBatchWriter snapshotBatchWriter) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(equipPersistCollectSnapshotInputChannel)
                .aggregate(a -> a
                        .correlationStrategy(message -> "equip-persist-collect-snapshot")
                        .releaseStrategy(group -> group.size() >= effectiveBatch)
                        .groupTimeout(effectiveFlushMs)
                        .sendPartialResultOnExpiry(true)
                        .expireGroupsUponCompletion(true))
                .handle(snapshotBatchWriter, "writeCollectSnapshotBatch")
                .get();
    }
}
