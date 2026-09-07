package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class WorkshopStreamInputBindings {

    @Bean
    public Consumer<String> workshopStreamPersistCollectSnapshot(
            ObjectMapper objectMapper,
            @Qualifier(WorkshopPersistAggregateConfiguration.WORKSHOP_PERSIST_COLLECT_SNAPSHOT_INPUT)
            MessageChannel workshopPersistCollectSnapshotInputChannel) {
        return body -> {
            try {
                UserContext.defaultTenant();
                JsonNode root = objectMapper.readTree(body);
                workshopPersistCollectSnapshotInputChannel.send(MessageBuilder.withPayload(toPersistEvent(root)).build());
            } catch (Exception e) {
                log.error("Workshop stream persist collect snapshot MQ: handle failed, payload={}", body, e);
            }
        };
    }

    private static WorkshopPersistMqEvent toPersistEvent(JsonNode root) {
        if (root == null || root.isNull()) {
            return new WorkshopPersistMqEvent(null, root);
        }
        JsonNode idNode = root.get("eventId");
        String eventId = idNode != null && idNode.isTextual() ? idNode.asText() : null;
        if (eventId != null && eventId.isBlank()) {
            eventId = null;
        }
        return new WorkshopPersistMqEvent(eventId, root);
    }
}
