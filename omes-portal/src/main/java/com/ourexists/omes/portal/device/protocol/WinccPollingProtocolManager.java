/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.protocol;

import com.alibaba.fastjson2.JSON;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.portal.device.collect.WinccEquipDataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * WinCC 定时轮询协议管理器（独立连接池，不依赖 WinccApi）：
 * - start：为指定 connect 建立 cron 定时任务，按网关下设备采集配置的 variableName 拉取 WinCC 变量并上报
 * - stop：移除并取消该 connect 的定时任务
 *
 * 注意：该类内部单独维护 scheduler/taskMap 与 HttpClient 连接池，不依赖 WinccApi。
 * HTTP 采用 SSL 连接池：PoolingHttpClientConnectionManager + setValidateAfterInactivity + IdleConnectionEvictor。
 */
@Slf4j
@Component
public class WinccPollingProtocolManager extends AbstractRestPollingProtocolManager {

    @Autowired
    private EquipFeign equipFeign;

    @Autowired
    private WinccEquipDataParser equipDataParser;

    @Override
    public String protocol() {
        return "WINCC";
    }

    @Override
    protected Object requestBody(ProtocolConnect connect) {
        List<String> variableNames = buildVariableNamesFromGatewayConfig(connect.getId());
        if (CollectionUtils.isEmpty(variableNames)) {
            log.debug("WinCC polling skipped: no variable names, connectId={}", connect.getId());
            return null;
        }
        Map<String, List<String>> params = new HashMap<>();
        params.put("variableNames", variableNames);
        return JSON.toJSONString(params);
    }

    @Override
    protected void respHandle(ProtocolConnect connect, String payload) {
        equipDataParser.parse(connect.getId(), payload);
    }

    @Override
    protected String path() {
        return "/tagManagement/Values";
    }


    /**
     * 通过网关 id 查询该网关下设备的采集配置，汇总需要采集的 WinCC 变量名（runMap、attrs[].map、alarms[].map）。
     */
    private List<String> buildVariableNamesFromGatewayConfig(String gwId) {
        Set<String> names = new LinkedHashSet<>();
        try {
            EquipPageQuery query = new EquipPageQuery();
            query.setGwId(gwId);
            query.setQueryConfig(true);
            query.setRequirePage(false);
            List<EquipDto> list = RemoteHandleUtils.getDataFormResponse(equipFeign.selectByPage(query));
            if (list == null) return new ArrayList<>(names);
            for (EquipDto equip : list) {
                GwBindingDto binding = equip.getConfig();
                if (binding == null || binding.getConfig() == null) continue;
                EquipConfigDetail config = binding.getConfig();
                if (StringUtils.hasText(config.getRunMap())) names.add(config.getRunMap());
                if (config.getAttrs() != null) {
                    for (EquipAttr attr : config.getAttrs()) {
                        if (StringUtils.hasText(attr.getMap())) names.add(attr.getMap().trim());
                    }
                }
                if (config.getAlarms() != null) {
                    for (EquipAlarm alarm : config.getAlarms()) {
                        if (StringUtils.hasText(alarm.getMap())) names.add(alarm.getMap().trim());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Load WinCC variable names from gateway device config failed, gwId={}: {}", gwId, e.getMessage());
        }
        return new ArrayList<>(names);
    }
}
