package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.idempotent.DuplicateRequestException;
import com.ourexists.era.framework.idempotent.IdempotentSupport;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Flink 桥接 persist 消息在门户内攒批；入 Aggregator 前用
 * {@link IdempotentSupport#readIdempotentId(Object, String)} 读取 {@code eventId}，再
 * {@link IdempotentSupport#executeIfIdPresent} 做 Redis 幂等。
 */
@Slf4j
@Configuration
@EnableIntegration
public class EquipPersistAggregateConfiguration {

    private static final String IDEMPOTENT_FIELD = "eventId";
    private static final String NS_CHANGE = "equip-persist-change";
    private static final String NS_STATE_SNAPSHOT = "equip-persist-state-snapshot";
    private static final String NS_COLLECT_SNAPSHOT = "equip-persist-collect-snapshot";

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
            @Value("${omes.equip.stream-persist-idempotent-ttl-hours:168}") long ttlHours,
            IdempotentSupport idempotentSupport,
            EquipPersistBatchWriter batchWriter,
            ObjectMapper objectMapper,
            EquipRealtimeManager equipRealtimeManager) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(equipPersistChangeInputChannel)
                .<EquipPersistMqEvent>handle((p, h) -> dedup(idempotentSupport, NS_CHANGE, ttlHours, p))
                .<EquipPersistMqEvent>filter(Objects::nonNull)
                .<EquipPersistMqEvent>handle((p, h) -> realtime(objectMapper, equipRealtimeManager, p))
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
            @Value("${omes.equip.stream-persist-idempotent-ttl-hours:168}") long ttlHours,
            IdempotentSupport idempotentSupport,
            EquipPersistBatchWriter snapshotBatchWriter) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(equipPersistStateSnapshotInputChannel)
                .<EquipPersistMqEvent>handle((p, h) -> dedup(idempotentSupport, NS_STATE_SNAPSHOT, ttlHours, p))
                .<EquipPersistMqEvent>filter(Objects::nonNull)
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
            @Value("${omes.equip.stream-persist-idempotent-ttl-hours:168}") long ttlHours,
            IdempotentSupport idempotentSupport,
            EquipPersistBatchWriter snapshotBatchWriter) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(equipPersistCollectSnapshotInputChannel)
                .<EquipPersistMqEvent>handle((p, h) -> dedup(idempotentSupport, NS_COLLECT_SNAPSHOT, ttlHours, p))
                .<EquipPersistMqEvent>filter(Objects::nonNull)
                .aggregate(a -> a
                        .correlationStrategy(message -> "equip-persist-collect-snapshot")
                        .releaseStrategy(group -> group.size() >= effectiveBatch)
                        .groupTimeout(effectiveFlushMs)
                        .sendPartialResultOnExpiry(true)
                        .expireGroupsUponCompletion(true))
                .handle(snapshotBatchWriter, "writeCollectSnapshotBatch")
                .get();
    }

    private EquipPersistMqEvent realtime(ObjectMapper objectMapper,
                                         EquipRealtimeManager equipRealtimeManager,
                                         EquipPersistMqEvent evt) {
        JsonNode jsonNode = evt.getRoot();
        try {
            if (jsonNode.hasNonNull("realtime")) {
                EquipRealtime rt = objectMapper.treeToValue(jsonNode.get("realtime"), EquipRealtime.class);
                UserContext.defaultTenant();
                UserContext.getTenant().setTenantId(rt.getTenantId());
                equipRealtimeManager.addOrUpdate(rt);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //nothing
        }
        return evt;
    }


    private EquipPersistMqEvent dedup(
            IdempotentSupport idempotentSupport,
            String namespace,
            long ttlHours,
            EquipPersistMqEvent evt) {
        if (evt == null) {
            return null;
        }
        try {
            Optional<String> idOpt = idempotentSupport.readIdempotentId(evt, IDEMPOTENT_FIELD);
            return idempotentSupport.executeIfIdPresent(
                    namespace,
                    idOpt.orElse(null),
                    ttlHours,
                    TimeUnit.HOURS,
                    () -> evt);
        } catch (DuplicateRequestException e) {
            log.debug("Equip stream persist dedup: skip duplicate namespace={} eventId={}", namespace, e.getIdempotentId());
            return null;
        } catch (Throwable t) {
            log.error("Equip stream persist dedup failed namespace={}", namespace, t);
            return null;
        }
    }
}
