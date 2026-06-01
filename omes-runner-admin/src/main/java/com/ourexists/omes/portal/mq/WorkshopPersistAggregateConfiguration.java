package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.idempotent.DuplicateRequestException;
import com.ourexists.era.framework.idempotent.IdempotentSupport;
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

@Slf4j
@Configuration
@EnableIntegration
public class WorkshopPersistAggregateConfiguration {

    private static final String IDEMPOTENT_FIELD = "eventId";
    private static final String NS_COLLECT_SNAPSHOT = "workshop-persist-collect-snapshot";

    public static final String WORKSHOP_PERSIST_COLLECT_SNAPSHOT_INPUT = "workshopPersistCollectSnapshotInputChannel";

    @Bean(name = WORKSHOP_PERSIST_COLLECT_SNAPSHOT_INPUT)
    public MessageChannel workshopPersistCollectSnapshotInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow workshopPersistCollectSnapshotAggregateFlow(
            @Qualifier(WORKSHOP_PERSIST_COLLECT_SNAPSHOT_INPUT) MessageChannel workshopPersistCollectSnapshotInputChannel,
            @Value("${omes.workshop.stream-persist-batch-size:32}") int batchSize,
            @Value("${omes.workshop.stream-persist-batch-flush-ms:200}") long batchFlushMs,
            @Value("${omes.equip.stream-persist-idempotent-ttl-hours:168}") long ttlHours,
            IdempotentSupport idempotentSupport,
            WorkshopPersistBatchWriter workshopPersistBatchWriter) {
        int effectiveBatch = Math.max(1, batchSize);
        long effectiveFlushMs = Math.max(20L, batchFlushMs);
        return IntegrationFlow.from(workshopPersistCollectSnapshotInputChannel)
                .<WorkshopPersistMqEvent>handle((p, h) -> dedup(idempotentSupport, NS_COLLECT_SNAPSHOT, ttlHours, p))
                .<WorkshopPersistMqEvent>filter(Objects::nonNull)
                .aggregate(a -> a
                        .correlationStrategy(message -> "workshop-persist-collect-snapshot")
                        .releaseStrategy(group -> group.size() >= effectiveBatch)
                        .groupTimeout(effectiveFlushMs)
                        .sendPartialResultOnExpiry(true)
                        .expireGroupsUponCompletion(true))
                .handle(workshopPersistBatchWriter, "writeWorkshopCollectSnapshotBatch")
                .get();
    }

    private WorkshopPersistMqEvent dedup(
            IdempotentSupport idempotentSupport,
            String namespace,
            long ttlHours,
            WorkshopPersistMqEvent evt) {
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
            log.debug("Workshop stream persist dedup: skip duplicate namespace={} eventId={}", namespace, e.getIdempotentId());
            return null;
        } catch (Throwable t) {
            log.error("Workshop stream persist dedup failed namespace={}", namespace, t);
            return null;
        }
    }
}
