package com.ourexists.mesedge.portal.listener;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;

@EnableBinding(PlcEventSink.class)
public class PlcListener {

    @StreamListener(PlcEventSink.EVENT)  // 指定监听的输入通道
    public void handleMessage(@Payload String message) {
        System.out.println("Received message: " + message);
    }
}
