package com.ourexists.mesedge.portal.message;

import com.alibaba.fastjson2.JSON;
import com.ourexists.mesedge.message.core.NotifyMsg;
import com.ourexists.mesedge.message.core.NotifyPusher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageManager implements NotifyPusher {

    @Autowired
    private MessageChannel notifyInputChannel;


    @Override
    public void push(List<NotifyMsg> notifyMsgs) {
        Message<String> message = MessageBuilder.withPayload(JSON.toJSONString(notifyMsgs)).build();
        notifyInputChannel.send(message);
    }
}
