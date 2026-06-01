package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.era.framework.idempotent.DuplicateRequestException;
import com.ourexists.era.framework.idempotent.IdempotentSupport;
import com.ourexists.omes.message.feign.NotifyFeign;
import com.ourexists.omes.message.model.NotifyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** 入站 persist 三条队列对应 Flink 桥接 sink；{@code change} / 状态快照 / 采集快照 均走 Integration Aggregator 批入库。 */
@Slf4j
@Configuration
public class EquipStreamInputBindings {

    private static final String EQUIP_NOTIFY_CREATE_IDEMPOTENT_NS = "equip-notify-create";
    private static final String EQUIP_NOTIFY_CREATE_IDEMPOTENT_FIELD = "eventId";

    @Bean
    public Consumer<String> equipNotifyCreate(
            ObjectMapper objectMapper,
            NotifyFeign notifyFeign,
            IdempotentSupport idempotentSupport,
            @Value("${omes.equip.stream-persist-idempotent-ttl-hours:168}") long idempotentTtlHours) {
        return body -> {
            try {
                UserContext.defaultTenant();
                NotifyDto dto = objectMapper.readValue(body, NotifyDto.class);
                Optional<String> idOpt = idempotentSupport.readIdempotentId(dto, EQUIP_NOTIFY_CREATE_IDEMPOTENT_FIELD);
                idempotentSupport.executeIfIdPresent(
                        EQUIP_NOTIFY_CREATE_IDEMPOTENT_NS,
                        idOpt.orElse(null),
                        idempotentTtlHours,
                        TimeUnit.HOURS,
                        () -> {
                            RemoteHandleUtils.getDataFormResponse(notifyFeign.createAndStart(dto));
                            return Boolean.TRUE;
                        });
            } catch (DuplicateRequestException e) {
                log.debug("Equip alarm notify MQ: skip duplicate eventId={}", e.getIdempotentId());
            } catch (Throwable e) {
                log.error("Equip alarm notify MQ: createAndStart failed, payload={}", body, e);
            }
        };
    }

    @Bean
    public Consumer<String> equipStreamPersistChange(
            ObjectMapper objectMapper,
            @Qualifier(EquipPersistAggregateConfiguration.EQUIP_PERSIST_CHANGE_INPUT)
            MessageChannel equipPersistChangeInputChannel) {
        return body -> {
            try {
                UserContext.defaultTenant();
                JsonNode root = objectMapper.readTree(body);
                equipPersistChangeInputChannel.send(MessageBuilder.withPayload(toPersistEvent(root)).build());
            } catch (Exception e) {
                log.error("Equip stream persist change MQ: handle failed, payload={}", body, e);
            }
        };
    }

    @Bean
    public Consumer<String> equipStreamPersistStateSnapshot(
            ObjectMapper objectMapper,
            @Qualifier(EquipPersistAggregateConfiguration.EQUIP_PERSIST_STATE_SNAPSHOT_INPUT)
            MessageChannel equipPersistStateSnapshotInputChannel) {
        return body -> {
            try {
                UserContext.defaultTenant();
                JsonNode root = objectMapper.readTree(body);
                equipPersistStateSnapshotInputChannel.send(MessageBuilder.withPayload(toPersistEvent(root)).build());
            } catch (Exception e) {
                log.error("Equip stream persist state snapshot MQ: handle failed, payload={}", body, e);
            }
        };
    }

    @Bean
    public Consumer<String> equipStreamPersistCollectSnapshot(
            ObjectMapper objectMapper,
            @Qualifier(EquipPersistAggregateConfiguration.EQUIP_PERSIST_COLLECT_SNAPSHOT_INPUT)
            MessageChannel equipPersistCollectSnapshotInputChannel) {
        return body -> {
            try {
                UserContext.defaultTenant();
                JsonNode root = objectMapper.readTree(body);
                equipPersistCollectSnapshotInputChannel.send(MessageBuilder.withPayload(toPersistEvent(root)).build());
            } catch (Exception e) {
                log.error("Equip stream persist collect snapshot MQ: handle failed, payload={}", body, e);
            }
        };
    }

    private static EquipPersistMqEvent toPersistEvent(JsonNode root) {
        if (root == null || root.isNull()) {
            return new EquipPersistMqEvent(null, root);
        }
        JsonNode idNode = root.get("eventId");
        String eventId = idNode != null && idNode.isTextual() ? idNode.asText() : null;
        if (eventId != null && eventId.isBlank()) {
            eventId = null;
        }
        return new EquipPersistMqEvent(eventId, root);
    }
}
