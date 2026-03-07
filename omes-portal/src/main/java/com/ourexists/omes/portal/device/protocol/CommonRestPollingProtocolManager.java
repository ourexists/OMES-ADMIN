/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.protocol;

import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.portal.device.collect.JSONEquipDataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REST 定时轮询协议管理器：
 * - start：为指定 connect 建立 cron 定时任务
 * - stop：移除并取消该 connect 的定时任务
 *
 * 注意：该类内部单独维护 scheduler/taskMap，不依赖全局任务管理器。
 * HTTP 采用连接池复用：基于 Apache HttpClient5 PoolingHttpClientConnectionManager，多请求复用同一 host 的 TCP 连接。
 * 连接超时/断开重建：setValidateAfterInactivity 复用前校验空闲连接；IdleConnectionEvictor 定期回收过期/空闲连接。
 */
@Slf4j
@Component
public class CommonRestPollingProtocolManager extends AbstractRestPollingProtocolManager {


    @Autowired
    private JSONEquipDataParser equipDataParser;

    @Override
    public String protocol() {
        return "Rest";
    }

    @Override
    protected Object requestBody(ProtocolConnect connect) {
        return null;
    }

    @Override
    protected void respHandle(ProtocolConnect connect, String payload) {
        equipDataParser.parse(connect.getId(), payload);
    }

    @Override
    protected String path() {
        return "";
    }


}

