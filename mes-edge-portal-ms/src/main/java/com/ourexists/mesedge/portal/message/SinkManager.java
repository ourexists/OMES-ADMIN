package com.ourexists.mesedge.portal.message;

import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SinkManager {

    private final Map<String, Sinks.Many<String>> clientSinkMap = new ConcurrentHashMap<>();

    public Sinks.Many<String> getSink(String clientId) {
        return clientSinkMap.computeIfAbsent(clientId,
                k -> Sinks.many().multicast().onBackpressureBuffer());
    }

    public void sendToClient(String clientId, String msg) {
        Sinks.Many<String> sink = clientSinkMap.get(clientId);
        if (sink != null) {
            sink.tryEmitNext(msg);
        }
    }

    public void broadcast(String msg) {
        clientSinkMap.values().forEach(s -> s.tryEmitNext(msg));
    }
}
