package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.message.feign.NotifyFeign;
import com.ourexists.omes.message.model.NotifyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

/** 入站 persist 三条队列对应 Flink 桥接 sink；{@code change} / 状态快照 / 采集快照 均走 Integration Aggregator 批入库。 */
@Slf4j
@Configuration
public class EquipStreamInputBindings {

    @Bean
    public Consumer<String> equipNotifyCreate(ObjectMapper objectMapper, NotifyFeign notifyFeign) {
        return body -> {
            try {
                UserContext.defaultTenant();
                NotifyDto dto = objectMapper.readValue(body, NotifyDto.class);
                RemoteHandleUtils.getDataFormResponse(notifyFeign.createAndStart(dto));
            } catch (Exception e) {
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
                equipPersistChangeInputChannel.send(MessageBuilder.withPayload(root).build());
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
                equipPersistStateSnapshotInputChannel.send(MessageBuilder.withPayload(root).build());
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
                equipPersistCollectSnapshotInputChannel.send(MessageBuilder.withPayload(root).build());
            } catch (Exception e) {
                log.error("Equip stream persist collect snapshot MQ: handle failed, payload={}", body, e);
            }
        };
    }
}
