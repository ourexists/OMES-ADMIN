package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

@Getter
public class WorkshopPersistMqEvent {

    private final String eventId;
    private final JsonNode root;

    public WorkshopPersistMqEvent(String eventId, JsonNode root) {
        this.eventId = eventId;
        this.root = root;
    }
}
