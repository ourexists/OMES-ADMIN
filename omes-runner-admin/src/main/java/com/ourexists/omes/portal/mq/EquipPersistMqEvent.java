package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

/**
 * 持久化攒批前的载荷：须含 Java 字段 {@code eventId}，供
 * {@link com.ourexists.era.framework.idempotent.IdempotentSupport#readIdempotentId(Object, String)} 反射读取；
 * 实际业务 JSON 在 {@link #root}。
 */
@Getter
public class EquipPersistMqEvent {

    private final String eventId;
    private final JsonNode root;

    public EquipPersistMqEvent(String eventId, JsonNode root) {
        this.eventId = eventId;
        this.root = root;
    }
}
