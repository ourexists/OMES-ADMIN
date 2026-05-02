package com.ourexists.omes.portal.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.message.feign.NotifyFeign;
import com.ourexists.omes.message.model.NotifyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** 消费 stream 侧设备报警 {@link NotifyDto} JSON，调用 {@link NotifyFeign#createAndStart}。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipNotifyCreateMessageListener {

    private final ObjectMapper objectMapper;
    private final NotifyFeign notifyFeign;

    @RabbitListener(queues = "${omes.device.rabbitmq.equip-notify-create-queue:omes.equip.notify.create}")
    public void onEquipAlarmNotify(String body) {
        try {
            UserContext.defaultTenant();
            NotifyDto dto = objectMapper.readValue(body, NotifyDto.class);
            RemoteHandleUtils.getDataFormResponse(notifyFeign.createAndStart(dto));
        } catch (Exception e) {
            log.error("Equip alarm notify MQ: createAndStart failed, payload={}", body, e);
        }
    }
}
