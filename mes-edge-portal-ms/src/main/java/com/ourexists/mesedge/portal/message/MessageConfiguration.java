package com.ourexists.mesedge.portal.message;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.TenantInfo;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.message.core.NotifyMsg;
import com.ourexists.mesedge.message.feign.MessageFeign;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.message.model.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.util.List;

@Slf4j
@Configuration
public class MessageConfiguration {

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private SinkManager sinkManager;

    @Bean
    public MessageChannel notifyInputChannel() {
        return new QueueChannel(100);
    }

    @Bean
    public GenericHandler<String> genericHandler() {
        return (payload, headers) -> {
            List<NotifyMsg> notifyMsgs = JSONArray.parseArray(payload, NotifyMsg.class);
            for (NotifyMsg notifyMsg : notifyMsgs) {
                MessageDto dto = new MessageDto();
                BeanUtils.copyProperties(notifyMsg, dto, "id");
                dto.setSourceId(notifyMsg.getId());
                UserContext.setTenant(new TenantInfo().setTenantId(notifyMsg.getTenantId()));
                try {
                    MessageVo messageVo = RemoteHandleUtils.getDataFormResponse(messageFeign.addOrUpdate(dto));
                    sinkManager.sendToClient(notifyMsg.getPlatform(), JSON.toJSONString(messageVo));
                } catch (EraCommonException e) {
                    log.error(e.getMessage(), e);
                    return null;
                }
            }
            return null;
        };
    }

    @Bean
    public IntegrationFlow integrationFlow() {
        return IntegrationFlow.from(notifyInputChannel())
                .handle(genericHandler())
                .get();
    }
}
