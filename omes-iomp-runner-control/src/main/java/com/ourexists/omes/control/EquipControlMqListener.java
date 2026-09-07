//package com.ourexists.omes.control;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * 共享队列消费；按设备编号串行化执行，保证同一设备控制指令顺序与 MQ 投递顺序一致。
// */
//@Slf4j
//@Component
//public class EquipControlMqListener {
//
//    private final ObjectMapper objectMapper;
//    private final EquipControlWriteService equipControlWriteService;
//
//    private final ConcurrentHashMap<String, Object> deviceLocks = new ConcurrentHashMap<>();
//
//    @Autowired
//    public EquipControlMqListener(ObjectMapper objectMapper, EquipControlWriteService equipControlWriteService) {
//        this.objectMapper = objectMapper;
//        this.equipControlWriteService = equipControlWriteService;
//    }
//
//    @RabbitListener(queues = EquipControlMqNames.QUEUE, concurrency = "${omes.rabbitmq.equip_control_concurrency:4}")
//    public void onMessage(String body) {
//        EquipControlMqMessage msg;
//        try {
//            msg = objectMapper.readValue(body, EquipControlMqMessage.class);
//        } catch (JsonProcessingException e) {
//            log.error("Invalid equip control JSON: {}", body, e);
//            throw new IllegalStateException("bad equip control message", e);
//        }
//        String sn = msg.getEquipSelfCode();
//        if (!StringUtils.hasText(sn)) {
//            throw new IllegalStateException("equipSelfCode missing in control message");
//        }
//        Object lock = deviceLocks.computeIfAbsent(sn, k -> new Object());
//        synchronized (lock) {
//            equipControlWriteService.execute(msg);
//        }
//    }
//}
